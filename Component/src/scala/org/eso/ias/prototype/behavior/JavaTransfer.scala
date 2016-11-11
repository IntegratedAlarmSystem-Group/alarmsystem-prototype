package org.eso.ias.prototype.behavior

import org.eso.ias.prototype.input.MonitorPointBase
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.component.AlarmSystemComponentBase
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.typedmp.LongMP

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
      inputs: List[MonitorPointBase], 
      id: Identifier,
      actualOutput:MonitorPoint[T]): MonitorPoint[T] = {
    println("JavaTransfer.transfer")
    
    val mpL = inputs(0)
    println("mpL.isInstanceOf[LongMP]="+mpL.isInstanceOf[LongMP])
    
    val mpA = inputs(1)
    println("mpA.isInstanceOf[LongMP]="+mpA.isInstanceOf[LongMP])
    
    super.transfer(inputs, id, actualOutput)
  }
  
}