package org.eso.ias.prototype.behavior

import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.component.ComputingElementBase
import org.eso.ias.prototype.input.AlarmValue
import java.util.Properties

/**
 * <code>JavaTransfer</code> implements the
 * transfer function by delegating to the java class provided by the 
 * user.
 * 
 * Note that the Validity of the output is not set by the transfer function
 * but automatically implemented by the ASC
 */
trait JavaTransfer[T] extends ComputingElementBase[T] {
  
   abstract override def transfer(
      inputs: Map[String, MonitorPoint], 
      id: Identifier,
      actualOutput: MonitorPoint,
      props: Properties): MonitorPoint = {
    println("JavaTransfer.transfer")
    
    super.transfer(inputs, id, actualOutput,props)
  }
  
}