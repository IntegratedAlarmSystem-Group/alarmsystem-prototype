package org.eso.ias.prototype.behavior

import org.eso.ias.prototype.component.ComputingElementBase
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.Identifier
import java.util.Properties

/**
 * <code>ScalaTransfer</code> implements the
 * transfer function by delegating to the scala class provided by the 
 * user.
 * 
 * Note that the Validity of the output is not set by the transfer function
 * but automatically implemented by the ASC
 */
trait ScalaTransfer extends ComputingElementBase {
  
  abstract override def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut,
      props: Properties): HeteroInOut = {
    println("ScalaTransfer.transfer")
    
    super.transfer(inputs, id, actualOutput,props)
  }
  
}