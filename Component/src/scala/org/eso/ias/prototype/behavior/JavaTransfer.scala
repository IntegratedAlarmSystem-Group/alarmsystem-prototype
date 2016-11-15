package org.eso.ias.prototype.behavior

import org.eso.ias.prototype.input.MonitorPointBase
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.component.ComputingElementBase
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.typedmp.LongMP
import java.util.Properties

/**
 * <code>JavaTransfer</code> implements the
 * transfer function by delegating to the java class provided by the 
 * user.
 * 
 * The Validity of the output is not set by the transfer function
 * but automatically implemented by the ASC
 */
trait JavaTransfer[T] extends ComputingElementBase[T] {
  
   abstract override def transfer(
      inputs: Map[String, MonitorPointBase], 
      id: Identifier,
      actualOutput: MonitorPoint[T],
      props: Properties): MonitorPoint[T] = {
    println("JavaTransfer.transfer")
    
    super.transfer(inputs, id, actualOutput,props)
  }
  
}