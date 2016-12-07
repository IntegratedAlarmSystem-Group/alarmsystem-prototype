package org.eso.ias.prototype.compele

import org.eso.ias.prototype.transfer.JavaTransfer
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.AckState
import scala.collection.mutable.{Map => MutableMap }
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.java.IASTypes
import org.eso.ias.prototype.transfer.ScalaTransfer
import org.eso.ias.prototype.transfer.TransferFunctionSetting
import java.util.Properties

/**
 * <code>ComputingElement</code> extends the <code>ComputingElementBase</code>) 
 * with methods to deal alarms shelving and acknowledging and to update
 * the value of the inputs.
 * 
 * The output is updated each time one of the input is updated:
 * a script checks the values of the inputs and generate the output.
 * It can be a AlarmValue but also a HIO of a given type (used to implement
 * so called synthetic parameters)
 * 
 * The output of a Component can, in turn, be the input 
 * of another Component.
 * 
 * <EM>Functioning</EM>
 * The ASC has an output, produced by the actualInputs that is refreshed at 
 * a given time interval.
 * Changes in the inputs are temporarily stored in the newInputs map.
 * When the refresh rate elapses the new value output with its validity is calculated 
 * from the newly received inputs (newInputs) and a new AlarmSystemComponent
 * is built.
 * 
 * TODO: instead of mixing here all the transfer traits, we must
 *       mix only the one corresponding to the implementation language of the TF
 *       when the DASU instantiate the ASCE.
 * 
 * 
 * @param id: @see ComputingElementBase#id 
 * @param output: @see ComputingElementBase#output
 * @param requiredInputs: @see ComputingElementBase#requiredInputs
 * @param inputs: The list of monitor points in input that generated the actual output
 * @param tfSetting: @see ComputingElementBase#tfSetting
 * @param props: @see ComputingElementBase#props
 * 
 * @see AlarmSystemComponentBase
 * 
 * @author acaproni
 */
class ComputingElement(
    override val id: Identifier,
    override var output: HeteroInOut,
    override val requiredInputs: List[String],
    override val inputs: MutableMap[String,HeteroInOut],
    override val tfSetting: TransferFunctionSetting,
    override val props: Some[Properties] = Some(new Properties())) 
extends ComputingElementBase 
with JavaTransfer with ScalaTransfer {
  
  /**
   * @return true if this component produces a synthetic parameter instead of an alarm
   * @see isAlarmComponent
   */
  def isSyntheticParameterComponent = output.iasType!=IASTypes.ALARM
  
  /**
   * @return true if this component generates an alarm
   * @see #isSyntheticParameterComponent
   */
  def isAlarmComponent = output.iasType!=IASTypes.ALARM
  
  /**
   * A HIO changed: it is stored in the map
   * ready to be evaluated when refreshing the value
   * of the output 
   * 
   * @param hio: The new value of a monitor point in input
   */
  def inputChanged(hio: Some[HeteroInOut]) {
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
    if (output.actualValue== None) {
      throw new IllegalStateException("Trying get an alarm but the value is None (alarm ID "+output.id+")") 
    }
    if (output.iasType!=IASTypes.ALARM) {
      throw new IllegalStateException("Trying to get an alarm but the value has IAS type "+output.iasType+" (alarm ID "+output.id+")")
    }
    val value: Any = output.actualValue.get.value
    
    if (! value.isInstanceOf[AlarmValue]) {
      throw new IllegalStateException("Type mismatch: IAS type is "+output.iasType+" but the class of the valueis "+value.getClass()+" (alarm ID "+output.id+")")
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
   * @return The ASCE shelved
   */
  def shelve(newShelveState: Boolean) = {
    
    val value: AlarmValue = getOutAlarmValue
    if (value.shelved==newShelveState) this // No change
    val newValue = value.shelve(newShelveState)
    
    output=output.updateValue(newValue)    
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
    
    val value: AlarmValue = getOutAlarmValue
    if (value.acknowledgement==AckState.Acknowledged) this // No change
    val newValue = value.acknowledge()
    
    output=output.updateValue(newValue)
  }
  
}