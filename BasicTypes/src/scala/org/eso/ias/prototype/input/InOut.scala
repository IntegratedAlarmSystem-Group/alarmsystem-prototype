package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.java.IASTypes._
import org.eso.ias.prototype.input.java.IASTypes

/**
 * A  <code>IasInOut</code> holds the value of an input or output 
 * of the IAS.
 * Objects of this type constitutes both the input of ASCEs and the output 
 * they produce.
 * 
 * The type of a IasInOut a double, an integer, an
 * array of integers and many other customized types.
 * 
 * <code>IasInOut</code> is immutable.
 * 
 * @param actualValue: the actual value of this IasInOut (can be undefined) 
 * @param id: The unique ID of the monitor point
 * @param refreshRate: The expected refresh rate (msec) of this monitor point
 *                     (to be used to assess its validity)
 * @param mode: The operational mode
 * @param validity: The validity of the monitor point
 * @param theType: is the IAS type of this IasInOut
 * 
 * @see IASType
 * 
 * @author acaproni
 */
case class InOut[A](
    value: Option[A],
    val id: Identifier,
    val refreshRate: Int,    
    val mode: OperationalMode,
    val validity: Validity.Value,
    val iasType: IASTypes) {
  require(Option[Identifier](id).isDefined,"The identifier must be defined")
  require(refreshRate>=InOut.MinRefreshRate,"Invalid refresh rate (too low): "+refreshRate)
  require(Option[Validity.Value](validity).isDefined,"Undefined validity is not allowed")
  require(Option[IASTypes](iasType).isDefined,"The type must be defined")
  
  val  actualValue: InOutValue[A] = new InOutValue(value)
  if (actualValue.value.isDefined) require(InOut.checkType(actualValue.value.get,iasType),"Type mismatch: ["+actualValue.value.get+"] is not "+iasType)
  
  def this(id: Identifier,
    refreshRate: Int,
    iasType: IASTypes) {
    this(None,id,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType)
  }
  
//  /**
//   * The hashCode is calculated only once when needed
//   */
//  lazy val hashCodeValue: Int = {
//    var temp = 19
//    temp += 31*temp + refreshRate
//    temp += 31*temp + mode.ordinal()
//    temp += 31*temp + validity.id
//    temp += 31*temp + actualValue.hashCode()
//    temp += 31*temp + id.hashCode()
//    temp += 31*temp + iasType.ordinal()
//    temp
//  }
  
  //  /**
  //   * Redefine the hashCode in terms of the values
  //   * of the properties.
  //   * 
  //   * @see #equals(other: Any)
  //   */
  //  override def hashCode = hashCodeValue
  //  
  //  /**
  //   * In IAS semantic 2 values are equal if and only
  //   * if the values of the properties the same.
  //   * 
  //   * @see #hashCode
  //   */
  //  override def equals(other: Any): Boolean = {
  //    other match {
  //      case that: HeteroInOut => 
  //        this.iasType==that.iasType &&
  //        this.mode==that.mode && 
  //        this.validity == that.validity &&
  //        this.actualValue == that.actualValue &&
  //        this.id == that.id &&
  //        this.refreshRate == that.refreshRate
  //      case _ => false
  //    }
  //  }
  
  override def toString(): String = {
    "Monitor point " + id.toString() +" of IAS type " +iasType+"\n\t" +  
    mode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue.value.isEmpty) "No value" else "Value: "+actualValue.value.get.toString())
  }
  
  /**
   * Update the mode of the monitor point
   * 
   * @param newMode: The new mode of the monitor point
   */
  def updateMode(newMode: OperationalMode): InOut[A] = {
    if (newMode==mode) this
    else this.copy(mode=newMode)
  }
  
  /**
   * Update the value of the monitor point
   * 
   * @param newValue: The new value of the monitor point
   */
  def updateValue(newValue: Option[A]): InOut[A] = update(newValue,validity)
  
  /**
   * Update the value and validity of the monitor point
   * 
   * @param newValue: The new value of the monitor point
   * @param valid: the new validity
   * @return A new IasInOut with updated value and validity
   */
  def update(newValue: Option[A], valid: Validity.Value): InOut[A] = {
    if (newValue==actualValue.value && valid==validity) this 
    else InOut(newValue,id,refreshRate,mode,valid,iasType)
  }
  
  /**
   * Update the validity of the monitor point
   * 
   * @param valid: The new validity of the monitor point
   */
  def updateValidity(valid: Validity.Value): InOut[A] = {
    if (valid==validity) this
    else this.copy(validity=valid)
  }
}

/** 
 *  IasInOut companion object
 */
object InOut {
  
  /**
   * The min possible value for the refresh rate
   * If it is too short the value will be invalid most of the time; if too 
   * long it is not possible to understand if it has been properly refreshed or
   * the source is stuck/dead.
   */
  val MinRefreshRate = 50;

  /**
   * Check if the passed value has of the proper type
   * 
   * @param value: The value to check they type against the iasType
   * @param iasType: The IAS type
   */
  def checkType[T](value: T, iasType: IASTypes): Boolean = {
    if (value==None) true
    else iasType match {
      case IASTypes.LONG => value.isInstanceOf[Long]
      case IASTypes.INT => value.isInstanceOf[Int]
      case IASTypes.SHORT => value.isInstanceOf[Short]
      case IASTypes.BYTE => value.isInstanceOf[Byte]
      case IASTypes.DOUBLE => value.isInstanceOf[Double]
      case IASTypes.FLOAT => value.isInstanceOf[Float]
      case IASTypes.BOOLEAN =>value.isInstanceOf[Boolean]
      case IASTypes.CHAR => value.isInstanceOf[Char]
      case IASTypes.STRING => value.isInstanceOf[String]
      case IASTypes.ALARM =>value.isInstanceOf[AlarmValue]
      case _ => false
    }
  }
  
  def apply[T](id: Identifier,
    refreshRate: Int,
    iasType: IASTypes): InOut[T] = {
    InOut[T](None,id,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType)
  }

  
//  /**
//   * Factory method to build a new IasInOut
//   * 
//   * @param actualValue: The value
//   * @param ident: The unique ID of the monitor point
//   * @param refreshRate: The expected refresh rate of the MP
//	 * @param value: The value of the monitor point
// 	 * @param mode: The operational mode
// 	 * @param valid: The validity of the monitor point
// 	 * @param iasType: The type of the monitor point
//   */
//  def apply[T](
//      actualValue: InOutValue[T],
//      ident: Identifier,
//      refreshRate: Int,
//      mode: OperationalMode= OperationalMode.UNKNOWN,
//      validity: Validity.Value = Validity.Unreliable,
//      iasType: IASTypes): Option[IasInOut[T]] = {
//    (actualValue, iasType) match {
//      case (None, _) => Some[IasInOut[T]](new InOutNone(ident,refreshRate,mode,validity,iasType))
//      case (v: Some[InOutValue[T]], t) if checkType(v.get.value,t) => Option[IasInOut[T]](new InOut[T](v,ident,refreshRate,mode,validity,t))
//      case (_, _) => None
//    }
    
//    if (actualValue.isEmpty) {
//      Some[IasInOut[T]](new InOutNone(ident,refreshRate,mode,validity,iasType))
//    } else {
//      val value = actualValue.get.value
//       if (!checkType(value, iasType)) None
//       else {
//         iasType match {
//          case LONG => Option[IasInOut[T]](new InOut[T](actualValue,ident,refreshRate,mode,validity,iasType)) 
//          case INT => new InOut[Int](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Int; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case SHORT => new InOut[Short](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Short; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case BYTE => new InOut[Byte](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Byte; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case DOUBLE => new InOut[Double](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Double; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case FLOAT=> new InOut[Float](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Float; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case BOOLEAN => new InOut[Boolean](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Boolean; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case CHAR => new InOut[Char](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Char; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case STRING => new InOut[String](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=String; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//          case ALARM => new InOut[AlarmValue](ident,refreshRate,mode,valid,iasType){type HeteroInOutType=AlarmValue; val theValue=if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) } 
//          case _ => None
//    }
//       }
//    }
}