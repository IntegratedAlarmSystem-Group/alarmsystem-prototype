package org.eso.ias.prototype.behavior

import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
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
trait JavaTransfer extends ComputingElementBase {
  
   abstract override def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut,
      props: Properties): HeteroInOut = {
    println("JavaTransfer.transfer")
    
    super.transfer(inputs, id, actualOutput,props)
  }
  
}