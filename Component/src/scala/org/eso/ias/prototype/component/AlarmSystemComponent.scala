package org.eso.ias.prototype.component

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmValue
import scala.util.control.NonFatal
import org.eso.ias.prototype.input.typedmp.TypedMonitorPoint
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * The immutable Integrated Alarm System Component (ASC) 
 * is the basic unit of the IAS.
 * 
 * It consists of a state (<code>ASCState</code>) and methods
 * to proceed to the next state.
 * 
 * The output is updated each time one of the input is updated:
 * a script checks the values of the inputs and generate the output.
 * It can be a AlarmValue but also a MonitorPoint[A] if the scripts
 * result in a value of type A (used to implement
 * so called synthetic parameters)
 * 
 * The output of a Component can, in turn, be the input 
 * of another Component.
 * 
 * A <code>AlarmSystemComponent</code> represents the state of the ASC
 * at a given point in time. It can be considered as a snapshot of the ASC
 * for the given inputs and output generated by running the script.
 * As such it can be used also offline to reproduce the state of the system
 * for debugging or simulation purposes.
 * 
 * @param ident: The unique ID of this Component
 * @param out: The the output generated by this Component
 *             after applying the script to the inputs
 *             It can or cannot be an AlarmValue
 * @param inputList: The list of monitor points that constitute the
 *                   inputs of the component
 * @param script: The script that manipulated the inputs and generate the output  
 * @see ASCState
 */
class AlarmSystemComponent[T] (
    ident: Identifier,
    out: TypedMonitorPoint[T],
    inputList: List[MonitorPointBase],
    script: String)
    extends ASCState[T](ident,out,inputList,script) {
  
  /**
   * Update the output by running the passed script against the inputs.
   * 
   * A change of the inputs means a change in at least one of
   * the inputs of the list. 
   * A change, in turn, can be a change  of 
   * - the value (or alarm) 
   * - validity
   * - mode 
   * The change triggers a recalculation of the Validity.
   * 
   * The number of inputs of a ASC does not change during the
   * life span of a component, what changes are the values,
   * validity or mode of the inputs.
   * 
   * In case of an alarm, being ACk or shelved does not trigger
   * a  recalculation of the output
   * 
   * @param theInputs: The list of inputs 
   * @return The new output
   */
  def inputChanged(theInputs: List[MonitorPointBase]): AlarmSystemComponent[T] = {
    println("Evaluating output...")
    
    val sem = new ScriptEngineManager()
    val pyEngine: ScriptEngine = sem.getEngineByName("python");
    try {
      pyEngine.eval("print \"Python - Hello, world!\"");
    } catch {
      case ex: Exception => println(ex); throw ex
    }
    
    this
  }
  
  /**
   * Get the alarm value of AlarmValue of the output.
   * 
   * @throws IllegalStateException: If the value of the output is None 
   *         or has a wrong type.
   */
  private def getOutAlarmValue: AlarmValue = {
    // Check if the value of the output is None i.e. the output has not yet 
    // been initialized.
    //
    // Having a output with a value of None should never happen because its value is
    // updated depending on the value of the inputs
    if (out.actualValue == None) {
      throw new IllegalStateException("Trying get an alarm but the value is None (alarm ID "+out.id+")") 
    }
    val value: Any = out.actualValue.get.value
    if (! value.isInstanceOf[AlarmValue]) {
      throw new IllegalStateException("Trying to get an alarm but the value has wrong type "+value.getClass()+" (alarm ID "+out.id+")")
    }
    value.asInstanceOf[AlarmValue]
  }
  
  /**
   * Shelve the output in response to an operator action.
   * 
   * This action is possible only if the output is a MonitorPoint[AlarmValue].
   * It is not needed to recalculate the output from the inputs when shelving an alarm.
   * 
   * @param newShelveState: True to shelve the alarm; False otherwise 
   * @return The ASC shelved
   */
  def shelve(newShelveState: Boolean): AlarmSystemComponent[T] = {
    
    val value: AlarmValue = getOutAlarmValue
    val newValue = value.shelve(newShelveState)
    
    val shelvedTypedMP=TypedMonitorPoint.updateValue(out,newValue.asInstanceOf[T])
    
    new AlarmSystemComponent[T](ident,shelvedTypedMP,inputList,script)
    
  }
  
  /**
   * Acknowledge the output in response to an operator action.
   * 
   * This action is possible only if the output is a MonitorPoint[AlarmValue].
   * It is not needed to recalculate the output from the inputs when shelving an alarm.
   * 
   * @return The ASC acknowledged
   */
  def ack(): AlarmSystemComponent[T] = {
    
    val value: AlarmValue = getOutAlarmValue
    val newValue = value.acknowledge()
    
    val ackedTypedMP=TypedMonitorPoint.updateValue(out,newValue.asInstanceOf[T]) 
    
    new AlarmSystemComponent[T](ident,ackedTypedMP,inputList,script)
  }
}