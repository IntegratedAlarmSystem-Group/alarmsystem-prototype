package org.eso.ias.prototype.transfer

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
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
trait JavaTransfer extends ComputingElementBase {
  
  /**
   * Flush the scala Map into a Java Map
   */
  private def flushOnJavaMap(
      inputs: Map[String, HeteroInOut]): JavaMap[String, IASValueBase] = {
    val map: JavaMap[String, IASValueBase] = new JavaHashMap[String, IASValueBase]()
    for (key <-inputs.keySet) {
      val hio = inputs(key)
      val iasVal = JavaConverter.hioToIASValue(hio)
      map.put(key,iasVal)
    }
    map
  }
  
  /**
   * scala data structs need to be converted before invoking
   * the java code.
   * 
   * @see ComputingElementBase#transfer
   */
  abstract override def transfer(
      inputs: Map[String, HeteroInOut], 
      id: Identifier,
      actualOutput: HeteroInOut): HeteroInOut = {
    println("JavaTransfer.transfer")
    
    if (tfSetting.javaTransferExecutor.isDefined) {
      val map: JavaMap[String, IASValueBase] = flushOnJavaMap(inputs)
      val newOutput=tfSetting.javaTransferExecutor.get.asInstanceOf[JavaTransferExecutor].eval(map,JavaConverter.hioToIASValue(actualOutput))
      super.transfer(inputs, id, JavaConverter.updateHIOWithIasValue(actualOutput, newOutput))
    } else {
      super.transfer(inputs, id, actualOutput)
    }
  }
  
}