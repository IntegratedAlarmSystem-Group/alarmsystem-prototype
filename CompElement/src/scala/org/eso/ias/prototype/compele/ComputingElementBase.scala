package org.eso.ias.prototype.compele

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmValue
import scala.util.control.NonFatal
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
import org.eso.ias.prototype.utils.ISO8601Helper

/**
 * The Integrated Alarm System Computing Element (ASCE) 
 * is the basic unit of the IAS. This  base class 
 * allows to implement stackable modifications.
 * 
 * The ASCE is composed of an output, generated by the user provided
 * script applied to the inputs (i.e. monitor points and/or
 * other alarms).
 * The AlarmSystemComponent change its state when the input changes
 * (for example the value of a monitor point changes) or when the operators 
 * acknowledge or shelve the alarm (if the output is an alarm).
 * 
 * The output of a ASCE is normally an alarm generated by digesting the values
 * of the inputs to the component itself. But sometimes, 
 * the output is a value of a given type (for example an integer) 
 * to implement what we called synthetic parameters. 
 * 
 * Objects of this class are mutable.
 * 
 * The id of the ASCE does not change over time unless the ASCE is relocated.
 * In such case a new ASCE must be built to correctly initialize
 * the classes implementing the transfer function. For the same reason,  
 * if the transfer function, implemented by the user changes, 
 * then a new ASCE must be built.
 * 
 * @param id: The unique ID of this Component
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
    final val id: Identifier,
    var output: HeteroInOut,
    requiredInputs: List[String],
    final val inputs: MutableMap[String,HeteroInOut],
    final val tfSetting: TransferFunctionSetting,
    val newInputs: MutableMap[String, HeteroInOut])
    extends Runnable {
  require(requiredInputs!=None && !requiredInputs.isEmpty,"Invalid (empty or null) list of required inputs to the component")
  require(requiredInputs.size==inputs.size,"Inconsistent size of lists of inputs")
  
  /**
   * The point in time when this objects (i.e. the snapshot) has been
   * modified.
   */
  protected[compele] val timestamp = System.currentTimeMillis()
  
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
   * The state of the ASCE
   */
  @volatile protected[compele] var state: ComputingElementState =  new ComputingElementState
  
  /**
   * true if the this ASCE has been shutdown, false otherwise  
   */
  @volatile protected[compele] var terminated = false;
  
  /**
   * The thread factory to async. run the 
   * initialize() and shutdown() methods of the TF implementations.
   */
  val threadFactory = new CompEleThreadFactory(id.runningID)
  
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
    
    if (state.canRunTF() && newInputs.synchronized{!newInputs.isEmpty}) {
      println("Running the TF")
      
      // Prepare the list of the inputs by replacing the ones in the 
      // inputs with those in the newInputs
      mixInputs(inputs,newInputs)
      
      // We pass around a immutable map to avoid that the user implementation
      // of the transfer function messes up the data
      val immutableMapOfInputs: Map[String, HeteroInOut] = Map.empty++inputs
      
      val runTransferFunction = Try[HeteroInOut] {
        val startedAt=System.currentTimeMillis()
        val ret = transfer(immutableMapOfInputs,id,output.asInstanceOf[HeteroInOut])
        val endedAt=System.currentTimeMillis()
        if (endedAt-startedAt>TransferFunctionSetting.MaxTolerableTFTime) {
          state=ComputingElementState.transition(state, new Slow())
        } else {
          state=ComputingElementState.transition(state, new Normal())
        }
        ret
      }
      runTransferFunction match {
        case Failure(v) =>
          println("TF inhibited for the time being: caught exception while running the user defined TF for input "+id.runningID+": "+v.getMessage)
          v.printStackTrace()
          // Change the state so that the TF is never executed again
          state=ComputingElementState.transition(state, new Broken())
        case Success(v) => 
          val newOutput=runTransferFunction.get
          if (newOutput!=output) {
            output=newOutput
            lastModificationTime=System.currentTimeMillis()
          }
      }
    } else {
      println("ACSE "+id.runningID+" TF inhibited or no new HIOs: actual state "+state.toString())
      if (state.actualState==AsceStates.Initing && tfSetting.initialized) state=ComputingElementState.transition(state, new Initialized())
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
   * @return The new output
   */
  def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut) : HeteroInOut = {
    
    updateOutputWithValidity(inputs,actualOutput)
  }
  
  /**
   * Update the validity of the passed actualOutput  from the validity
   * of its inputs.
   * 
   * @param theInputs: The inputs, sorted by their IDs 
   * @param actualOutput: the actual output
   * @return The new output with the validity updated
   */
  private[this] def updateOutputWithValidity(
      theInputs: Map[String, HeteroInOut], 
      actualOutput: HeteroInOut) : HeteroInOut = {
    println("Updating validity")
    val valitiesSet = MutableSet[Validity.Value]()
    for ( hio <- theInputs.values ) valitiesSet += hio.validity
    val newValidity = Validity.min(valitiesSet.toList) 
    
    output.updateValidity(newValidity).asInstanceOf[HeteroInOut]
  }
  
  override def toString() = {
    val outStr: StringBuilder = new StringBuilder("State of component ")
    outStr.append(id.toString())
    outStr.append(" build at ")
    outStr.append(ISO8601Helper.getTimestamp(timestamp))
    outStr.append("\n>Output<\n")
    outStr.append(output.toString())
    outStr.append("\n>Inputs<\n")
    for (mp <- inputs) outStr.append(mp.toString())
    outStr.append("\n>Script<\n")
    outStr.append(tfSetting)
    outStr.append("\n>Health<\t")
    outStr.append(state)
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
    state = ComputingElementState.transition(state, new Initialized())
  }
  
  /**
   * <code>shutdown>/code> must be executed to free the resources of a <code>ComputingElement</code>
   * that later on can be cleanly destroyed.
   * 
   * One of the tasks of this method is to stop the timer thread
   * to update the output i.e. to execute the transfer function.
   */
  def shutdown(): Unit = {
    state = ComputingElementState.transition(state, new Shutdown())
    this.synchronized {
      terminated=true
      if (threadExecutor.isDefined) threadExecutor.get.remove(this)
    }
    tfSetting.shutdown()
    state = ComputingElementState.transition(state, new Close())
  }
}


