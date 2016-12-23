package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.java.IASTypes

sealed abstract class InOut[+A]

/**
 * A  <code>HeteroInOut</code> holds the value of a monitor point.
 * 
 * The type of the value that objects of this class hold is abstract
 * in an attempt to get rid of java type erasure
 * The type of a monitor point can be a double, an integer, an
 * array of integers and many other types and must be set before
 * instantiating an object of the class (@see the factory methods in the
 * companion object).
 * TypeTag-gin could be investigated but it uses reflections 
 * reducing performances. 
 * 
 * <code>HeteroInOut</code> is immutable.
 * 
 * @param value: the actual value of this HeteroInOut (it can be undefined 
 * @param ident The unique ID of the monitor point
 * @param actualValue The value of the monitor point
 * @param mode The operational mode
 * @param refreshRate: The expected refresh rate (msec) of this monitor point
 *                     (to be used to assess its validity)
 * @param valid: The validity of the monitor point
 * @param theType: is the IAS type of this HeteroInOut
 * 
 * @see IASType
 * 
 * @author acaproni
 */
case class HeteroInOut[A] (
    actualValue: Option[InOutValue[A]]=None,
    id: Identifier,
    refreshRate: Int,    
    mode: OperationalMode= OperationalMode.UNKNOWN,
    validity: Validity.Value= Validity.Unreliable,
    iasType: IASTypes) extends InOut[A] {
  require(Option[Identifier](id).isDefined,"The identifier can't be None")
  require(refreshRate>=HeteroInOut.MinRefreshRate,"Invalid refresh rate (too low): "+refreshRate)
  
  def this(v: A,
    id: Identifier,
    refreshRate: Int,    
    mode: OperationalMode,
    validity: Validity.Value,
    iasType: IASTypes) {
    this(Option[InOutValue[A]](new InOutValue[A](v)),id,refreshRate,mode,validity,iasType)
  }
  
  
  
  /**
   * The hashCode is calculated only once when needed
   */
  lazy val hashCodeValue: Int = {
    var temp = 19
    temp += 31*temp + refreshRate
    temp += 31*temp + mode.ordinal()
    temp += 31*temp + validity.id
    temp += 31*temp + actualValue.hashCode()
    temp += 31*temp + id.hashCode()
    temp += 31*temp + iasType.ordinal()
    temp
  }
  
  override def toString(): String = {
    "Monitor point " + id.toString() +" of IAS type " +iasType+"\n\t" +  
    mode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (!actualValue.isDefined) "No value" else "Value: "+actualValue.get.toString())
  }
  
  /**
   * Update the value and validity of the monitor point
   * 
   * @param newValue: The new value of the monitor point
   * @param valid: the new validity
   * @return A new HeteroInOut with updated value and validity
   */
  def update(newValue: A,valid: Validity.Value): Option[HeteroInOut[A]] = {
    val value: Option[A] = actualValue.map( x=> actualValue.get.value)
    (value,this) match {
      case (Some(t), HeteroInOut(a,_,_,_,v,_)) if (this.validity==v && t==newValue) => Some(this)
      case _ => Some(new HeteroInOut(newValue,id,refreshRate,mode,valid,iasType))
    }
  }
  
  /**
   * Update the value of the monitor point
   * 
   * @param newValue: The new value of the monitor point
   */
  def updateValue(newValue: A): Option[HeteroInOut[A]] = {
    update(newValue,validity)
  }
  
  /**
   * Update the mode of the monitor point
   * 
   * @param newMode: The new mode of the monitor point
   */
  def updateMode(newMode: OperationalMode): Option[HeteroInOut[A]] = {
    if (newMode==mode) Some(this)
    else Some(new HeteroInOut(actualValue,id,refreshRate,newMode,validity,iasType))
  }
  
  /**
   * Update the validity of the monitor point
   * 
   * @param valid: The new validity of the monitor point
   */
  def updateValidity(valid: Validity.Value):Option[HeteroInOut[A]]= {
    if (valid==validity) Some(this)
    else Some(new HeteroInOut(actualValue,id,refreshRate,mode,valid,iasType))
  }
  
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
  
}

/** 
 *  Provides factory methods for building HeteroInOut objects
 */
object HeteroInOut {
  
  /**
   * The min possible value for the refresh rate
   * If it is too little the MP will be invalid most of the time; if too 
   * big it is not possible to understand if it has been properly refreshed or
   * the source is stuck/dead.
   */
  val MinRefreshRate = 50;
}

//  
//  /**
//   * Check if the passed value is of the proper type
//   * 
//   * @param value: The value to check they type against the iasType
//   * @param iasType: The IAS type
//   */
//  def checkType[T](value: T, iasType: IASTypes): Boolean = {
//    if (value==None) true
//    else iasType match {
//      case IASTypes.LONG => value.isInstanceOf[Long]
//      case IASTypes.INT => value.isInstanceOf[Int]
//      case IASTypes.SHORT => value.isInstanceOf[Short]
//      case IASTypes.BYTE => value.isInstanceOf[Byte]
//      case IASTypes.DOUBLE => value.isInstanceOf[Double]
//      case IASTypes.FLOAT => value.isInstanceOf[Float]
//      case IASTypes.BOOLEAN =>value.isInstanceOf[Boolean]
//      case IASTypes.CHAR => value.isInstanceOf[Char]
//      case IASTypes.STRING => value.isInstanceOf[String]
//      case IASTypes.ALARM =>value.isInstanceOf[AlarmValue]
//      case _ => false
//    }
//  }
//  
//    /**
//   * Build a monitor point without a value, mode and validity.
//   * 
//   * This factory method must be used to create a new monitor point,
//   * not to update an existing one as it uses only defaults values
//   * 
//   * @param ident: The identifier of the MP
//   * @param refreshRate: The expected refresh rate of the MP
//   */
//  def apply(
//      ident: Identifier, 
//      refreshRate: Int,
//      iasType: IASTypes): HeteroInOut = {
//    iasType match {
//      case IASTypes.LONG=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Long; val theValue=None}
//      case IASTypes.INT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Int; val theValue=None}
//      case IASTypes.SHORT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Short; val theValue=None}
//      case IASTypes.BYTE => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Byte; val theValue=None}
//      case IASTypes.DOUBLE => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Double; val theValue=None}
//      case IASTypes.FLOAT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Float; val theValue=None}
//      case IASTypes.BOOLEAN => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Boolean; val theValue=None}
//      case IASTypes.CHAR=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Char; val theValue=None}
//      case IASTypes.STRING=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=String; val theValue=None}
//      case IASTypes.ALARM=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType){type HeteroInOutType=AlarmValue; val theValue=None} 
//      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+iasType)
//    }
//    
//  }
//  
//  /**
//   * Factory method to build a new HeteroInOut
//   * 
//   * @param ident: The unique ID of the monitor point
//   * @param refreshRate: The expected refresh rate of the MP
//	 * @param value: The value of the monitor point
// 	 * @param mode: The operational mode
// 	 * @param valid: The validity of the monitor point
// 	 * @param iasType: The type of the monitor point
//   */
//  def apply[T](
//      ident: Identifier,
//      refreshRate: Int,
//      value: T,
//      mode: OperationalMode= OperationalMode.UNKNOWN,
//      valid: Validity.Value = Validity.Unreliable,
//      iasType: IASTypes): HeteroInOut = {
//    
//    if (!checkType(value, iasType)) throw new ClassCastException("The value ["+value+"] is not a "+iasType.toString())
//    
//    iasType match {
//      case IASTypes.LONG => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Long; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) } 
//      case IASTypes.INT => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Int; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.SHORT => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Short; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.BYTE => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Byte; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.DOUBLE => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Double; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.FLOAT=> new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Float; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.BOOLEAN => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Boolean; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.CHAR => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Char; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.STRING => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=String; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
//      case IASTypes.ALARM => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=AlarmValue; val theValue=if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) } 
//      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+iasType)
//    }
//  }
//}