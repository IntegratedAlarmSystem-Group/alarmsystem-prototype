package org.eso.ias.prototype.transfer

import org.eso.ias.prototype.compele.ComputingElementBase
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.Identifier
import java.util.Properties

/**
 * <code>ScalaTransfer</code> calls the scala
 * transfer function provided by the user. 
 * user.
 * 
 * Note that the Validity of the output is not set by the transfer function
 * but automatically implemented by the ASCE
 */
trait ScalaTransfer extends ComputingElementBase {
  
  /**
   * Check if the TF is scala, intialized, not shutdown
   */
  def canRunTheScalaTF = 
    tfSetting.initialized &&
    tfSetting.transferExecutor.isDefined && 
    tfSetting.language==TransferFunctionLanguage.scala &&
    !tfSetting.isShutDown
  
  abstract override def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut): HeteroInOut = {
     if (canRunTheScalaTF) {
      val newOutput=tfSetting.transferExecutor.get.asInstanceOf[ScalaTransferExecutor].eval(inputs,actualOutput)
      
      super.transfer(inputs, id, newOutput)
    } else {
      super.transfer(inputs, id, actualOutput)
    }
  }
  
}