package org.eso.ias.prototype.compele

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.InOut
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
import org.eso.ias.prototype.transfer.TransferFunctionLanguage

/**
 * The Integrated Alarm System Computing Element (ASCE) 
 * is the basic unit of the IAS. 
 * 
 * The ASCE is composed of an output, generated by the user provided
 * script applied to the inputs (i.e. monitor points and/or
 * other alarms).
 * The AlarmSystemComponent change its output when the input changes
 * (for example the value of a monitor point changes) or when the operators 
 * acknowledge or shelve the alarm (if the output is an alarm).
 * 
 * The output of a ASCE is normally an alarm generated by digesting the values
 * of the inputs to the component itself. But sometimes, 
 * the output is a value of a given type (for example an integer) 
 * to implement what we called synthetic parameters. 
 * 
 * The user must provide a JVM class in one of the supported languages
 * to digest the inputs and produce the output. The transfer method of the object
 * is invoked at regular time intervals when the state of the inputs
 * changed. Which programming language the user wrote the object with
 * is stored in the configuration database; programmatically it is 
 * visible in the {@link TransferFunctionSetting} object.
 * Depending on the programming language, there might be some 
 * preparatory step before running the TF.
 * The class is abstract because the implementation of the transfer function 
 * depends on the programiong language and must be mixed when instantiating the 
 * object.
 * 
 * The ASCE is a state machine (@see ComputingElementState) whose
 * state changes during the life time of the ASCE for example
 * after initialization or shutdown but also if the TF executor
 * reports errors or is too slow.
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
 * @param output: The output generated by this Component after applying the script to the inputs
 *                It can or cannot be an AlarmValue
 * @param requiredInputs: The IDs of the inputs that this component
 *                        needs to generate the output. The list does not change
 *                        during the life time of the component.
 * @param inputs: The list of monitor points in input that generated the actual output
 * @param tfSetting: The definition of the implementation of the transfer function
 *                   that manipulates the inputs to produce the new output
 * @param props: the properties to pass to this component and the TF
 * 
 * @see AlarmSystemComponent
 * @author acaproni
 */
abstract class ComputingElement[T](
    val id: Identifier,
    var output: InOut[T],
    val requiredInputs: List[String],
    val inputs: MutableMap[String,InOut[_]],
    val tfSetting: TransferFunctionSetting,
    val props: Some[Properties] = Some(new Properties())) 
    extends Runnable {
  
  require(requiredInputs!=None && !requiredInputs.isEmpty,"Invalid (empty or null) list of required inputs to the component")
  require(requiredInputs.size==inputs.size,"Inconsistent size of lists of inputs")
  
  /**
   * The programming language of this TF is abstract
   * because it depends on the of the transfer mixed in
   * when building objects of this class
   */
  val tfLanguage: TransferFunctionLanguage.Value
  
  /**
   * The point in time when this object has been created.
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
   * The map with the value of the monitor points in input
   * received after the last update of the output
   */
  protected[compele] final val newInputs: MutableMap[String, InOut[_]] =  new HashMap[String,InOut[_]]()
  
  /**
   * The thread factory to async. run the 
   * initialize() and shutdown() methods of the TF implementations.
   */
  val threadFactory = new CompEleThreadFactory(id.runningID)
  
  /**
   * <code>true</code> if this component produces 
   * a synthetic parameter instead of an alarm
   */
  lazy val isSyntheticParameterComponent = output.iasType!=IASTypes.ALARM
  
  /**
   * <code>true</code> if this component generates an alarm
   */
  lazy val isAlarmComponent = !isSyntheticParameterComponent
  
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
   * The calculation of the input is ultimately delegated to the abstract 
   * #transfer(...) method whose implementation is provided by the user.
   * 
   * @see transfer(...)
   */
  def transfer(): Unit = {
    
    if (state.canRunTF() && newInputs.synchronized{!newInputs.isEmpty}) {
      
      // Prepare the list of the inputs by replacing the ones in the 
      // inputs with those in the newInputs
      mixInputs(inputs,newInputs)
      
      // We pass around a immutable map to avoid that the user implementation
      // of the transfer function messes up the data
      val immutableMapOfInputs: Map[String, InOut[_]] = Map.empty++inputs
      
      val startedAt=System.currentTimeMillis()
      val ret = try {
        transfer(immutableMapOfInputs,id,output)
      } catch { case t: Throwable => Left(t) }
      val endedAt=System.currentTimeMillis()
      if (endedAt-startedAt>TransferFunctionSetting.MaxTolerableTFTime) {
        state=ComputingElementState.transition(state, new Slow())
      } else {
        state=ComputingElementState.transition(state, new Normal())
      }
      ret match {
       
        case Left(ex) =>
          println("TF inhibited for the time being: caught exception while running the user defined TF for input "+id.runningID+": "+ex.getMessage)
          ex.printStackTrace()
          // Change the state so that the TF is never executed again
          state=ComputingElementState.transition(state, new Broken())
        case Right(v) => 
          if (v!=output) {
            output=v
            lastModificationTime=System.currentTimeMillis()
          }
      }
    } else {
      println("ACSE "+id.runningID+" TF inhibited or no new HIOs: actual state "+state.toString())
      if (state.actualState==AsceStates.Initing && tfSetting.initialized) state=ComputingElementState.transition(state, new Initialized())
    }
    // Validity must always be updated
    output=updateTheValidity(inputs,output)
  }
  
  /**
   * Flush the received inputs into the actual inputs.
   * 
   * @param oldInput: The inputs that generated the old output of the Component
   * @param receivedInputs: the inputs that have been updated since the refresh
   *                        of the output of the component
   */
  private def mixInputs(
      oldInputs: MutableMap[String, InOut[_]], 
      receivedInputs: MutableMap[String, InOut[_]] ) = {
    
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
      inputs: Map[String, InOut[_]], 
      id: Identifier,
      actualOutput: InOut[T]) : Either[Exception,InOut[T]]
  
  /**
   * Update the validity of the passed actualOutput  from the validity
   * of its inputs.
   * 
   * @param theInputs: The inputs, sorted by their IDs 
   * @param actualOutput: the actual output
   * @return The new output with the validity updated
   */
  private[this] def updateTheValidity(
      theInputs: MutableMap[String, InOut[_]], 
      actualOutput: InOut[T]) : InOut[T] = {
    System.out.println("ComputingElementBase.updateOutputWithValidity(...)")
    val newValidity = Validity.min(theInputs.values.map(_.validity).toList)
    actualOutput.updateValidity(newValidity)
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
    if (!terminated) try {
      transfer()
    } catch  { case t: Exception => state=ComputingElementState.transition(state, new Broken()) }
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
    
    tfSetting.initialize(id.id.get, id.runningID, props.get)
    
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
  
  /**
   * Expose the actual state of this ASCE
   * 
   * @return The state of the ASCE
   */
  def getState(): AsceStates.State = state.actualState
  
  /**
   * A input changed: it is stored in the map
   * ready to be evaluated when refreshing the value
   * of the output 
   * 
   * @param hio: The new value of the input
   */
  def inputChanged(hio: Some[InOut[_]]) {
    if (!requiredInputs.contains(hio.get.id.id.get)) {
      throw new IllegalStateException("Trying to pass a MP to a component that does not want it: "+hio.get.id.id.get+" not in "+requiredInputs.mkString(", "))
    }
    newInputs.synchronized {
      // Check if actualInputs already contains this HIO and it they matches
      // We do not want to add twice the same HIO unless it changed, of course
      val actualHIO = inputs.get(hio.get.id.id.get)
      if (actualHIO.isDefined && actualHIO.get!=hio.get) {
        newInputs(hio.get.id.id.get)=hio.get
      }
    }
  }
  
  /**
   * Get the alarm value of AlarmValue of the output.
   * The value is None if the if the value of the output is None 
   * or there is a type mismatch.
   */
  private def getOutAlarmValue: Option[AlarmValue] = {
    // Check if the value of the output is None i.e. the output has not yet 
    // been initialized.
    //
    // Having a output with a value of None should never happen because its value is
    // updated depending on the value of the inputs
    if (isSyntheticParameterComponent || output.actualValue.value.isEmpty) None
    else Some(output.actualValue.value.get.asInstanceOf[AlarmValue])
  }
  
  /**
   * Shelve the output in response to an operator action.
   * 
   * This action is possible only if the output is a MonitorPoint[AlarmValue].
   * It is not needed to recalculate the output from the inputs when shelving an alarm.
   * 
   * @param newShelveState: True to shelve the alarm; false otherwise 
   * @return The ASCE shelved
   */
  def shelve(newShelveState: Boolean) {
    
    val value: Option[AlarmValue] = getOutAlarmValue
    value match {
      case Some(alarm) => this.output=this.output.updateValue(Some(alarm.shelve(newShelveState).asInstanceOf[T]))
      case _ => ;
    }
  }
  
  /**
   * Acknowledge the output in response to an operator action.
   * 
   * This action is possible only if the output is a MonitorPoint[AlarmValue].
   * It is not needed to recalculate the output from the inputs when shelving an alarm.
   * 
   * @return The ASCE acknowledged
   */
  def ack() = {
    
    val value: Option[AlarmValue] = getOutAlarmValue
    value match {
      case Some(alarm) => this.output=this.output.updateValue(Some(alarm.acknowledge().asInstanceOf[T]))
      case _ => ;
    }
  }
  
}


