package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.java.IASTypes

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
abstract class HeteroInOut private[input] (
    val id: Identifier,
    val refreshRate: Int,    
    val mode: OperationalMode,
    val validity: Validity.Value,
    val iasType: IASTypes) {
  require(id!=None,"The identifier can't be None")
  require(refreshRate>=HeteroInOut.MinRefreshRate,"Invalid refresh rate (too low): "+refreshRate)
  
  /**
   * Abstract type. 
   * The type is defined by the factory when building a HeteroInOut
   */
  type HeteroInOutType
  
  val theValue: Option[HeteroInOutType]
  
  /**
   * This property is abstract because its type depends
   * on the abstract HeteroInOutType
   */
  lazy val actualValue: Option[HeteroIOValue] = {
    if (theValue==None) None else 
    Option[HeteroIOValue](new HeteroIOValue(theValue.get.asInstanceOf[HeteroInOutType]))
  }
  
  /**
   * The value of the HIO is associated 
   * to a timestamp corresponding to the update time of 
   * the value
   */
  class HeteroIOValue(
    val value: HeteroInOutType) {
    val timestamp: Long = System.currentTimeMillis()
    
    override def toString(): String = { 
      "Value "+value.toString() +
      " updated at "+ISO8601Helper.getTimestamp(timestamp)
    }
    
    /**
     * Redefine the hashCode in terms of the value
     * @see #equals(other: Any)
     */
    override def hashCode = value.##
    
    /**
     * In IAS semantic 2 values are equal if and only
     * if their values are the same.
     * The timestamp is not used for comparison. 
     * 
     * @see #hashCode
     */
    override def equals(other: Any): Boolean = {
      other match {
        case that: HeteroIOValue => value==that.value
        case _ => false
      }
    }
  }
  
  override def toString(): String = {
    "Monitor point " + id.toString() +" of IAS type " +iasType+"\n\t" +  
    mode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue==None) "No value" else "Value: "+actualValue.get.toString())
  }
  
  /**
   * Update the value and validity of the monitor point
   */
  def update[T](newValue: T,valid: Validity.Value): HeteroInOut = {
    if (!Option[T](newValue).isDefined) throw new IllegalArgumentException("Inavalid new value for "+id.id.get+": "+newValue)
    if (
        actualValue!=None && 
        actualValue.get.value == newValue.asInstanceOf[HeteroInOutType] &&
        valid==validity) {
      this
    }
    else {
      val value = if (newValue==None) None else newValue
      HeteroInOut(id,refreshRate,value,mode,valid,iasType)
    }
  }
  
  /**
   * Update the value of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param newValue: The new value of the monitor point
   * @return updates the passed monitor point with the given new value
   */
  def updateValue[T](newValue: T): HeteroInOut = {
    update[T](newValue,validity)
  }
  
  /**
   * Update the mode of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param newMode: The new mode of the monitor point
   * @return updates the passed monitor point with the given new mode
   */
  def updateMode(newMode: OperationalMode):HeteroInOut = {
    if (newMode==mode) this
    else  if (theValue==None) HeteroInOut(id,refreshRate,None,newMode,validity,iasType)
    else HeteroInOut(id,refreshRate,theValue.get,newMode,validity,iasType)
  }
  
  /**
   * Update the validity of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param validMode: The new validity of the monitor point
   * @return updates the passed monitor point with the given new validity
   */
  def updateValidity(valid: Validity.Value):HeteroInOut = {
    if (valid==validity) this
    else if (theValue==None) HeteroInOut(id,refreshRate,None,mode,valid,iasType)
    else HeteroInOut(id,refreshRate,theValue.get,mode,valid,iasType)
  }
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
  
  /**
   * Check if the passed value is of the proper type
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
  
    /**
   * Build a monitor point without a value, mode and validity.
   * 
   * This factory method must be used to create a new monitor point,
   * not to update an existing one as it uses only defaults values
   * 
   * @param ident: The identifier of the MP
   * @param refreshRate: The expected refresh rate of the MP
   */
  def apply(
      ident: Identifier, 
      refreshRate: Int,
      iasType: IASTypes): HeteroInOut = {
    iasType match {
      case IASTypes.LONG=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Long; val theValue=None}
      case IASTypes.INT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Int; val theValue=None}
      case IASTypes.SHORT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Short; val theValue=None}
      case IASTypes.BYTE => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Byte; val theValue=None}
      case IASTypes.DOUBLE => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Double; val theValue=None}
      case IASTypes.FLOAT => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Float; val theValue=None}
      case IASTypes.BOOLEAN => new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Boolean; val theValue=None}
      case IASTypes.CHAR=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=Char; val theValue=None}
      case IASTypes.STRING=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType) {type HeteroInOutType=String; val theValue=None}
      case IASTypes.ALARM=> new HeteroInOut(ident,refreshRate,OperationalMode.UNKNOWN,Validity.Unreliable,iasType){type HeteroInOutType=AlarmValue; val theValue=None} 
      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+iasType)
    }
    
  }
  
  /**
   * Factory method to build a new HeteroInOut
   * 
   * @param ident: The unique ID of the monitor point
   * @param refreshRate: The expected refresh rate of the MP
	 * @param value: The value of the monitor point
 	 * @param mode: The operational mode
 	 * @param valid: The validity of the monitor point
 	 * @param iasType: The type of the monitor point
   */
  def apply[T](
      ident: Identifier,
      refreshRate: Int,
      value: T,
      mode: OperationalMode= OperationalMode.UNKNOWN,
      valid: Validity.Value = Validity.Unreliable,
      iasType: IASTypes): HeteroInOut = {
    
    if (!checkType(value, iasType)) throw new ClassCastException("The value ["+value+"] is not a "+iasType.toString())
    
    iasType match {
      case IASTypes.LONG => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Long; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) } 
      case IASTypes.INT => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Int; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.SHORT => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Short; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.BYTE => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Byte; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.DOUBLE => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Double; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.FLOAT=> new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Float; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.BOOLEAN => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Boolean; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.CHAR => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=Char; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.STRING => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=String; val theValue= if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) }
      case IASTypes.ALARM => new HeteroInOut(ident,refreshRate,mode,valid,iasType){type HeteroInOutType=AlarmValue; val theValue=if (value==None) None else Option[HeteroInOutType](value.asInstanceOf[HeteroInOutType]) } 
      case _ => throw new UnsupportedOperationException("Unsupported IAS type: "+iasType)
    }
  }
}