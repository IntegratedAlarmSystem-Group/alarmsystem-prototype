package org.eso.ias.prototype.transfer

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.InOut
import org.eso.ias.prototype.compele.ComputingElementBase
import org.eso.ias.prototype.input.AlarmValue
import java.util.Properties
import java.util.{Map => JavaMap, HashMap => JavaHashMap}
import org.eso.ias.prototype.input.java.IASValue
import org.eso.ias.prototype.input.JavaConverter
import org.eso.ias.prototype.input.java.IASValueBase

/**
 * <code>JavaTransfer</code> calls the java
 * transfer function provided by the user.
 * 
 * Note that the Validity of the output is not set by the transfer function
 * but automatically implemented by the ASCE
 */
trait JavaTransfer[T] extends ComputingElementBase[T] {
  
  /**
   * Flush the scala Map into a Java Map
   */
  private[this] def flushOnJavaMap(
      inputs: Map[String, InOut[_]]): JavaMap[String, IASValueBase] = {
    val map: JavaMap[String, IASValueBase] = new JavaHashMap[String, IASValueBase]()
    for (key <-inputs.keySet) {
      val hio = inputs(key)
      val iasVal = JavaConverter.inOutToIASValue(hio)
      map.put(key,iasVal)
    }
    map
  }
  
  /**
   * Check if the TF is java, intialized, not shutdown
   */
  private[this] def canRunTheJavaTF = 
    tfSetting.initialized &&
    tfSetting.transferExecutor.isDefined && 
    tfSetting.language==TransferFunctionLanguage.java &&
    !tfSetting.isShutDown
  
  /**
   * scala data structs need to be converted before invoking
   * the java code.
   * 
   * @see ComputingElementBase#transfer
   */
  abstract override def transfer(
      inputs: Map[String, InOut[_]], 
      id: Identifier,
      actualOutput: InOut[T]): Either[Exception,InOut[T]] = {
    if (canRunTheJavaTF) {
      val map: JavaMap[String, IASValueBase] = flushOnJavaMap(inputs)
      val newOutput=tfSetting.transferExecutor.get.asInstanceOf[JavaTransferExecutor].eval(map,JavaConverter.inOutToIASValue(actualOutput))
      val x=JavaConverter.updateHIOWithIasValue(actualOutput, newOutput).asInstanceOf[InOut[T]]
      super.transfer(inputs, id, x)
    } else {
      super.transfer(inputs, id, actualOutput)
    }
  }
  
}