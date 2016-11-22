package org.eso.ias.prototype.input

import org.eso.ias.prototype.input.java.IASValue
import org.eso.ias.prototype.input.java.IASTypes._

/**
 * Converter methods from java to scala and vice-versa.
 * 
 * @author acaproni
 */
object JavaConverter {
  
  /**
   * Convert a scala HIO in a java IASValue
   * 
   * @param hio: the HIO to converto to java IASValue
   * @return The java value version of the passed HIO 
   */
  def hioToIASValue[T](hio: HeteroInOut): IASValue[T] = {
    require(Option[HeteroInOut](hio).isDefined)
    
    val ret = hio.iasType match {
      case LONG => new IASValue[Long](hio.actualValue.get.value.asInstanceOf[Long], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case INT => new IASValue[Int](hio.actualValue.get.value.asInstanceOf[Int], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case SHORT => new IASValue[Short](hio.actualValue.get.value.asInstanceOf[Short], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case BYTE => new IASValue[Byte](hio.actualValue.get.value.asInstanceOf[Byte], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case DOUBLE => new IASValue[Double](hio.actualValue.get.value.asInstanceOf[Double], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case FLOAT => new IASValue[Float](hio.actualValue.get.value.asInstanceOf[Float], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case CHAR => new IASValue[Char](hio.actualValue.get.value.asInstanceOf[Char], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case STRING => new IASValue[String](hio.actualValue.get.value.asInstanceOf[String], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case ALARM=> new IASValue[AlarmValue](hio.actualValue.get.value.asInstanceOf[AlarmValue], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID,hio.iasType)
      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+hio.iasType)
    }
    ret.asInstanceOf[IASValue[T]]
  }
  
  /**
   * Update a scala HeteroInOut with the passed java IASValue
   * 
   * @param hio: the HIO to update
   * @param iasValue: the java value to update the passed scala HIO
   * @return The hio updated with the passed java value
   */
  def updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T]): HeteroInOut = {
    assert(Option[HeteroInOut](hio).isDefined)
    assert(Option[IASValue[T]](iasValue).isDefined)
    // Some consistency check
    if (hio.iasType!=iasValue.valueType) {
      throw new IllegalStateException("Type mismatch for HIO "+hio.id.runningID+": "+hio.iasType+"!="+iasValue.valueType)
    }
    if (hio.id.id.get!=iasValue.id) {
      throw new IllegalStateException("ID mismatch for HIO "+hio.id.runningID+": "+hio.id.id.get+"!="+iasValue.id)
    }
    if (hio.id.runningID!=iasValue.runningId) {
      throw new IllegalStateException("Running ID mismatch for HIO "+hio.id.runningID+": "+hio.id.runningID+"!="+iasValue.runningId)
    }
    // Finally, update the HIO
    hio.updateMode(iasValue.mode).updateValue(iasValue.value)
    
  }
}