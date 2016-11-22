package org.eso.ias.prototype.compele

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmValue
import scala.util.control.NonFatal
import org.eso.ias.prototype.transfer.JavaConverter
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.AckState
import org.eso.ias.prototype.transfer.JavaTransfer
import scala.collection.mutable.{Set => MutableSet, Map => MutableMap}
import org.eso.ias.prototype.input.java.IASTypes
import java.util.Properties
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import org.eso.ias.prototype.transfer.TransferFunctionSetting

/**
 * The healthiness of the ASCE.
 * 
 * The purpose is mainly to show to the external world what 
 * this ASCE is doing
 */
object AcseHealth extends Enumeration {
  val Initing = Value // Initializing
  val Healthy = Value // Everything OK
  val TFBroken = Value // The transfer function is too broken
  val TFSlow = Value // The transfer function is slow
  val ShuttingDown = Value // The ASCE is shutting down
  val Closed = Value // Closed i.e. shutdown complete
  
  /**
   * When the state of the ASC is one of those in
   * this list, the transfer function is not executed
   */
  val inhibitorStates = Set(Initing, TFBroken, ShuttingDown, Closed)
}

/**
 * The Integrated Alarm System Computing Element (ASCE) 
 * is the basic unit of the IAS. This  base class 
 * allows to implement stackable modifications
 * 
 * @param ident: The unique ID of this Component
 * @param out: The the output generated by this Component
 *             after applying the script to the inputs
 *             It can or cannot be an AlarmValue
 * @param requiredInputs: The IDs of the inputs that this component
 *                        needs to generate the output. The list does not change
 *                        during the life time of the component.
 * @param actualInputs: The list of monitor points in input that generated the actual output
 * @param script: The script that manipulated the inputs and generate the output
 * @param newInputs: the map with the value of the monitor points in input
 *                   received after the last update of the output  
 * @see AlarmSystemComponent, ComputingElementState
 * @author acaproni
 */
abstract class ComputingElementBase (
    ident: Identifier,
    out: HeteroInOut,
    requiredInputs: List[String],
    actualInputs: MutableMap[String,HeteroInOut],
    tfSetting: TransferFunctionSetting,
    val newInputs: MutableMap[String, HeteroInOut])
    extends ComputingElementState(ident,out,actualInputs,tfSetting)
    with Runnable {
  require(requiredInputs!=None && !requiredInputs.isEmpty,"Invalid (empty or null) list of required inputs to the component")
  require(requiredInputs.size==actualInputs.size,"Inconsistent size of lists of inputs")
  
  /**
   * The point in time when the output of this computing element has been
   * modified for the last time.
   * 
   * Note that a modification can be in the setting or raising of the alarm 
   * in output (or its value if it is a synthetic parameter) but
   * also shelving or acknowledging an alarm is a modification
   */
  protected[compele] var lastModificationTime = timestamp
  
  /**
   * The thread executor to run the transfer function
   */
  protected[compele] var threadExecutor: Option[ScheduledThreadPoolExecutor] = None
  
  /**
   * The healthiness of this ASCE
   */
  @volatile protected[compele] var health: AcseHealth.Value =  AcseHealth.Initing
  
  /**
   * true if the this ASCE has been shutdown, false otherwise  
   */
  @volatile protected[compele] var terminated = false;
  
  /**
   * The thread factory mainly used to async. run the transfer function
   * initialize() and tearDown()
   */
  val threadFactory = new CompEleThreadFactory(ident.runningID)
  
  /**
   * Update the output by running the user provided script/class against the inputs.
   * This is actually the core of the ASCE.
   * 
   * A change of the inputs means a change in at least one of
   * the inputs of the list. 
   * A change, in turn, can be a change  of 
   * - the value (or alarm) 
   * - validity
   * - mode 
   * The change triggers a recalculation of the Validity.
   * 
   * The number of inputs of a ASCE does not change during the
   * life span of a component, what changes are the values,
   * validity or mode of the inputs.
   * 
   * In case of an alarm, being ACK or shelved does not trigger
   * a  recalculation of the output.
   * 
   * The calculation of the input is delegated to the overloaded 
   * #transfer(...) that generated the output by stackable modifications.
   * The method provided here, updates the Validity.
   * 
   * @see transfer(...)
   */
  def transfer(): Unit = {
    
    mixInputs(inputs,newInputs)
    
    if (AcseHealth.inhibitorStates.contains(health)) {
      println("ACSE "+id.runningID+" TF inhibited: actual state "+health)
    } else {
      // Prepare the list of the inputs by replacing the ones in the 
      // inputs with those in the newInputs
      val immutableMapOfInputs: Map[String, HeteroInOut] = Map.empty++inputs
      
      val runTransferFunction = Try[HeteroInOut] { 
        transfer(immutableMapOfInputs,id,output.asInstanceOf[HeteroInOut],System.getProperties)
      }
      runTransferFunction match {
        case Failure(v) =>
          println("Caught exception while running the user defined transfer function for input "+id.runningID+": "+v.getMessage)
          v.printStackTrace()
        case Success(v) => 
          val newOutput=runTransferFunction.get
          if (newOutput!=output) {
            output=newOutput
            lastModificationTime=System.currentTimeMillis()
          }
      }
    }
  }
  
  /**
   * Flush the received inputs into the actual inputs.
   * 
   * @param oldInput: The inputs that generated the old output of the Component
   * @param receivedInputs: the inputs that have been updated since the refresh
   *                        of the output of the component
   */
  private def mixInputs(
      oldInputs: MutableMap[String, HeteroInOut], 
      receivedInputs: MutableMap[String, HeteroInOut] ) = {
    
    val len= inputs.size
    receivedInputs.synchronized {
      receivedInputs.keys.foreach { id => oldInputs(id)=receivedInputs(id) }
      receivedInputs.clear()
    }
    assert(len==inputs.size,"The map of inputs increased!")
  } 
  
  /**
   * Update the output by running the user provided script/class against the inputs
   * by stackable modifications (@see the classes mixed in the {@link AlarmSystemComponent}
   * class)
   * 
   * This method sets the validity of the output from the validity of its inputs.
   * 
   * @param theInputs: The inputs, sorted by their IDs 
   * @param id: the ID of this computing element
   * @param actualOutput: the actual output
   * @param pros: properties to pass to the implementors
   * @return The new output
   */
  def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut,
      props: Properties) : HeteroInOut = {
    
    val valitiesSet = MutableSet[Validity.Value]()
    for ( monitorPoint <- inputs.values ) valitiesSet += monitorPoint.validity
    val newValidity = Validity.min(valitiesSet.toList) 
    
    output.updateValidity(newValidity).asInstanceOf[HeteroInOut]
  }
  
  override def toString() = {
    val outStr: StringBuilder = new StringBuilder(super.toString())
    outStr.append("\n>Health<\t")
    outStr.append(health)
    outStr.append("\n>ID of inputs<\n")
    outStr.append(requiredInputs.mkString(", "))
    outStr.append("\n>Not yet processed inputs<\n")
    newInputs.synchronized( { outStr.append(newInputs.values.mkString(", "))})
    outStr.toString()
  }
  
  /**
   * The thread that periodically executes the transfer function
   */
  def run(): Unit = {
    if (!terminated) transfer()
  }
  
  /**
   * Initialize the object.
   * 
   * This method is executed once.
   * 
   * One of the tasks of this method is to run the timer thread
   * to update the output i.e. to execute the transfer function.
   */
  def initialize(stpe: ScheduledThreadPoolExecutor): Unit = {
    require(Option[ScheduledThreadPoolExecutor](stpe).isDefined)
    
    tfSetting.initialize(threadFactory,id.id.get, id.runningID, new Properties())
    
    this.synchronized {
      threadExecutor=Some[ScheduledThreadPoolExecutor](stpe)
    }
    // Start the thread to refresh the output by running the
    // transfer function
    threadExecutor.get.scheduleAtFixedRate(this, 2000, output.refreshRate, TimeUnit.MILLISECONDS)
  }
  
  /**
   * <code>shutdown>/code> must be executed to free the resources of a <code>ComputingElement</code>
   * that later on can be cleanly destroyed.
   * 
   * One of the tasks of this method is to stop the timer thread
   * to update the output i.e. to execute the transfer function.
   */
  def shutdown(): Unit = {
    this.synchronized {
      terminated=true
      if (threadExecutor.isDefined) threadExecutor.get.remove(this)
    }
  }
}
