package org.eso.ias.prototype.behavior

import org.eso.ias.prototype.input.MonitorPointBase
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.component.AlarmSystemComponentBase

/**
 * <code>JavaTransfer</code> implements the
 * transfer function by delegating to the java class provided by the 
 * user.
 * 
 * The Validity of the output is not set by the transfer function
 * but automatically implemented by the ASC
 */
trait JavaTransfer[T] extends AlarmSystemComponentBase[T] {
  
   abstract override def transfer(
      actualInputs: List[MonitorPointBase], 
      updatedInputs: HashMap[String, MonitorPointBase], 
      id: Identifier,
      actualOutput:MonitorPointBase): AlarmSystemComponentBase[T] = {
    println("JavaTransfer.transfer")
    
    super.transfer(actualInputs, updatedInputs, id, actualOutput)
  }
  
}