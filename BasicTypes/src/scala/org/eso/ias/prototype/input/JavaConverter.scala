package org.eso.ias.prototype.input

import org.eso.ias.prototype.input.java.IASValue
import org.eso.ias.prototype.input.java.IASTypes._
import org.eso.ias.prototype.input.java.IasAlarm
import org.eso.ias.prototype.input.java.IasLong
import org.eso.ias.prototype.input.java.IASValueBase
import org.eso.ias.prototype.input.java.IasDouble
import org.eso.ias.prototype.input.java.IasInt
import org.eso.ias.prototype.input.java.IasShort
import org.eso.ias.prototype.input.java.IasByte
import org.eso.ias.prototype.input.java.IasFloat
import org.eso.ias.prototype.input.java.IasChar
import org.eso.ias.prototype.input.java.IasString
import org.eso.ias.prototype.input.java.IasBool

/**
 * Converter methods from java to scala and vice-versa.
 * 
 * @author acaproni
 */
object JavaConverter {
  
  /**
   * Convert a scala HIO in a java IASValue
   * 
   * @param hio: the HIO to convert to java IASValue
   * @return The java value version of the passed HIO 
   */
  def hioToIASValue[T](hio: HeteroInOut): IASValueBase = {
    require(Option[HeteroInOut](hio).isDefined)
    
    val ret = if (!hio.actualValue.isDefined) {
      hio.iasType match {
        case LONG => new IasLong(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case INT => new IasInt(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case SHORT => new IasShort(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case BYTE => new IasByte(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case DOUBLE => new IasDouble(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case FLOAT => new IasFloat(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case BOOLEAN => new IasBool(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case CHAR => new IasChar(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case STRING => new IasString(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case ALARM=> new IasAlarm(null, Long.MinValue,hio.mode,hio.id.id.get,hio.id.runningID)
        case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+hio.iasType)
      }
    } else {
      hio.iasType match {
        case LONG => new IasLong(hio.actualValue.get.value.asInstanceOf[Long], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case INT => new IasInt(hio.actualValue.get.value.asInstanceOf[Int], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case SHORT => new IasShort(hio.actualValue.get.value.asInstanceOf[Short], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case BYTE => new IasByte(hio.actualValue.get.value.asInstanceOf[Byte], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case DOUBLE => new IasDouble(hio.actualValue.get.value.asInstanceOf[Double], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case FLOAT => new IasFloat(hio.actualValue.get.value.asInstanceOf[Float], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case BOOLEAN => new IasBool(hio.actualValue.get.value.asInstanceOf[Boolean], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case CHAR => new IasChar(hio.actualValue.get.value.asInstanceOf[Char], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case STRING => new IasString(hio.actualValue.get.value.asInstanceOf[String], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case ALARM=> new IasAlarm(hio.actualValue.get.value.asInstanceOf[AlarmValue], hio.actualValue.get.timestamp,hio.mode,hio.id.id.get,hio.id.runningID)
        case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+hio.iasType)
      }
    }
    ret.asInstanceOf[IASValueBase]
  }
  
  /**
   * Update a scala HIO with a IasValueBase inferring its type
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IASValueBase): HeteroInOut = {
    hio.iasType match {
      case LONG => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasLong])
      case INT => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasInt])
      case SHORT => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasShort])
      case BYTE => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasByte])
      case DOUBLE => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasDouble])
      case FLOAT => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasFloat])
      case BOOLEAN => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasBool])
      case CHAR => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasChar])
      case STRING => updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasString])
      case ALARM=> updateHIOWithIasValue(hio,iasValue.asInstanceOf[IasAlarm])
      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+hio.iasType)
    }
  }
  
  
  /**
   * Update a scala HIO with a IasLong
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasLong): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Long](hio, iasValue.asInstanceOf[IASValue[Long]])
  }
  
  /**
   * Update a scala HIO with a IasInt
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasInt): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Int](hio, iasValue.asInstanceOf[IASValue[Int]])
  }
  
  /**
   * Update a scala HIO with a IasShort
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasShort): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Short](hio, iasValue.asInstanceOf[IASValue[Short]])
  }
  
  /**
   * Update a scala HIO with a IasByte
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasByte): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Byte](hio, iasValue.asInstanceOf[IASValue[Byte]])
  }
  
  /**
   * Update a scala HIO with a IasDouble
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasDouble): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Double](hio, iasValue.asInstanceOf[IASValue[Double]])
  }
  
  /**
   * Update a scala HIO with a IasFloat
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasFloat): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Float](hio, iasValue.asInstanceOf[IASValue[Float]])
  }
  
  /**
   * Update a scala HIO with a IasFloat
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasBool): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Boolean](hio, iasValue.asInstanceOf[IASValue[Boolean]])
  }
  
  /**
   * Update a scala HIO with a IasChar
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasChar): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[Char](hio, iasValue.asInstanceOf[IASValue[Char]])
  }
  
  /**
   * Update a scala HIO with a IasString
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasString): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[String](hio, iasValue.asInstanceOf[IASValue[String]])
  }
  
  /**
   * Update a scala HIO with a IasAlarm
   * 
   * @see #updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T])
   */
  def updateHIOWithIasValue(hio: HeteroInOut, iasValue: IasAlarm): HeteroInOut = {
    JavaConverter.updateHIOWithIasValue[AlarmValue](hio, iasValue.asInstanceOf[IASValue[AlarmValue]])
  }
  
  /**
   * Update a scala HeteroInOut with the passed java IASValue
   * 
   * @param hio: the HIO to update
   * @param iasValue: the java value to update the passed scala HIO
   * @return The hio updated with the passed java value
   */
  private def updateHIOWithIasValue[T](hio: HeteroInOut, iasValue: IASValue[T]): HeteroInOut = {
    assert(Option[HeteroInOut](hio).isDefined)
    assert(Option[IASValueBase](iasValue).isDefined)
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
    hio.updateMode(iasValue.mode).updateValue(iasValue.asInstanceOf[IASValue[T]].value)
    
  }
}