package org.eso.ias.prototype.component

import org.eso.ias.prototype.behavior.JavaTransfer
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.MonitorPointBase
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.AckState

class AlarmSystemComponent[T](
    ident: Identifier,
    out: MonitorPoint[T],
    requiredInputs: List[String],
    actualInputs: List[MonitorPointBase],
    script: String,
    newInputs: HashMap[String, MonitorPointBase]) 
    extends AlarmSystemComponentBase[T](ident,out,requiredInputs,actualInputs,script,newInputs) with JavaTransfer[T] {
  
  /**
   * A monitor point changed: it is stored in the map
   * ready to be evaluated when refreshing the value
   * of the output 
   * 
   * @param mp: The new value of a monitor point in input
   */
  def inputChanged(mp: Some[MonitorPointBase]) {
    if (!newInputs.contains(mp.get.id.id.get)) {
      throw new IllegalStateException("Trying to pass aMP to a component that does not want it")
    }
    newInputs.synchronized {
      newInputs(mp.get.id.id.get)=mp.get
    }
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
   * @param newShelveState: True to shelve the alarm; false otherwise 
   * @return The ASC shelved
   */
  def shelve(newShelveState: Boolean): AlarmSystemComponentBase[T] = {
    
    val value: AlarmValue = getOutAlarmValue
    if (value.shelved==newShelveState) this // No change
    val newValue = value.shelve(newShelveState)
    
    val shelvedTypedMP=out.updateValue(newValue.asInstanceOf[T])
    
    new AlarmSystemComponent[T](ident,shelvedTypedMP,requiredInputs,actualInputs,script,newInputs)
    
  }
  
  /**
   * Acknowledge the output in response to an operator action.
   * 
   * This action is possible only if the output is a MonitorPoint[AlarmValue].
   * It is not needed to recalculate the output from the inputs when shelving an alarm.
   * 
   * @return The ASC acknowledged
   */
  def ack(): AlarmSystemComponentBase[T] = {
    
    val value: AlarmValue = getOutAlarmValue
    if (value.acknowledgement==AckState.Acknowledged) this // No change
    val newValue = value.acknowledge()
    
    val ackedTypedMP=out.updateValue(newValue.asInstanceOf[T]) 
    
    new AlarmSystemComponent[T](ident,ackedTypedMP,requiredInputs,actualInputs,script,newInputs)
  }
}