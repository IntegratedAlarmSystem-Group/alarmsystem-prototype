package org.eso.ias.prototype.compele

import org.eso.ias.prototype.transfer.JavaTransfer
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.InOut
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
 * TODO: instead of mixing here all the transfer traits, we better
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
class ComputingElement[T](
    id: Identifier,
    output: InOut[T],
    requiredInputs: List[String],
    inputs: MutableMap[String,InOut[_]],
    tfSetting: TransferFunctionSetting,
    props: Some[Properties] = Some(new Properties())) 
extends ComputingElementBase[T](id,output,requiredInputs,inputs,tfSetting,props) {
//with JavaTransfer with ScalaTransfer {
  
  
  
}