package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmValue

/**
 * The TypedMonitorPoint is to avoid external users of this module
 * to deal directly with the MonitorPoint[A].
 * 
 * The reasons to provide this empty class are:
 * <UL>
 * 	<LI> ensure to use only a well defined set of types 
 *       to have under control the types of the objects passed 
 *       to the behavior script.
 *  <LI> transparently deal with special types like the AlarmValue
 * 
 * At this stage it is not yet clear if we will really need to keep
 * the types of the MonitorPoint under control
 * 
 * @param ident The unique ID of the monitor point
 * @param actualVal The value of the monitor point
 * @param mode The operational mode
 * @param valid: The validity of the monitor point
 * 
 * @see MonitorPoint
 * @author acaproni
 */
class TypedMonitorPoint[A] protected ( // protected to allow only the companion
    ident: Identifier, // The unique ID of this MonitorPoint
    actualVal: Option[MonitorPointValue[A]] = None, // Uninitialized at build time
    mode: OperationalMode.Mode = OperationalMode.Unknown,
    valid: Validity.Value = Validity.Unreliable)
    extends MonitorPoint[A] (ident,actualVal,mode,valid) {
  if (!TypedMonitorPoint.isSupportedType(actualVal)) throw new UnsupportedOperationException("Unsupported typed monitor type")
  
  override def toString(): String = {
    "Typed Monitor point " + id.toString() + "\n\t" +
    (if (actualValue==None) "None value" else "Value of type " +actualValue.get.value.getClass().getName())+"\n\t" +  
    runningMode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue==None) "No value" else actualValue.get.toString())
  }
}
  
/** 
 *  Provides factory methods for building TypedMonitorPoint objects
 */
object TypedMonitorPoint {
  
  /**
   * Check if the type of the value is one of the supported type
   * for a monitor point
   */
  def isSupportedType[A](value: Option[MonitorPointValue[A]]): Boolean = {
    if (value==None) true else value.get.value match {
      case x:Long => true
      case x:AlarmValue => true
      case _ => false
    }
  }
  
  /**
   * Build a new TypedMonitorPoint delegating to the TypedMonitorPoint
   * main constructor
   * 
   * @param ident: The unique ID of the monitor point
	 * @param actualVal: The value of the monitor point
 	 * @param mode; The operational mode
 	 * @param valid: The validity of the monitor point
   */
  def typedMonitor[A](ident: Identifier, // The unique ID of this MonitorPoint
    actualVal: Option[MonitorPointValue[A]] = None, // Uninitialized at build time
    mode: OperationalMode.Mode = OperationalMode.Unknown,
    valid: Validity.Value = Validity.Unreliable): TypedMonitorPoint[A] = {
      new TypedMonitorPoint[A](ident,actualVal,mode,valid)
  }
    
    /**
     * Build a monitor point without a value, mode and validity.
     * 
     * This factory method must be used to create a new monitor point,
     * not to update an existing one as it uses only defaults values
     */
  def typedMonitor[A](ident: Identifier):TypedMonitorPoint[A] = {
    new TypedMonitorPoint[A](ident)
  }
  
  /**
   * Factory method to get a new monitor point with updated value
   * 
   * @param oldMP: The monitor point to update
   * @param newValue: The new value of the monitor point
   * @return updates the passed monitor point with the given new value
   */
  def updateValue[A](oldMP: TypedMonitorPoint[A], newValue: A):TypedMonitorPoint[A] = {
    if (oldMP.actualValue!=None && oldMP.actualValue.get == newValue) oldMP
    else {
      val value = Option(new MonitorPointValue[A](newValue))
      if (!TypedMonitorPoint.isSupportedType(value)) 
        throw new UnsupportedOperationException("Unsupported typed monitor type")
      new TypedMonitorPoint[A](oldMP.id,value,oldMP.runningMode,oldMP.validity)
    }
  }
  
  /**
   * Factory method to get a new monitor point with updated mode
   * 
   * @param oldMP: The monitor point to update
   * @param newMode: The new mode of the monitor point
   * @return updates the passed monitor point with the given new mode
   */
  def updateMode[A](oldMP: TypedMonitorPoint[A], newMode: OperationalMode.Mode):TypedMonitorPoint[A] = {
    new TypedMonitorPoint[A](oldMP.id,oldMP.actualValue,newMode,oldMP.validity)
  }
  
  /**
   * Factory method to get a new monitor point with updated validity
   * 
   * @param oldMP: The monitor point to update
   * @param validMode: The new validity of the monitor point
   * @return updates the passed monitor point with the given new validity
   */
  def updateValidity[A](oldMP: TypedMonitorPoint[A], valid: Validity.Value):TypedMonitorPoint[A] = {
    new TypedMonitorPoint[A](oldMP.id,oldMP.actualValue,oldMP.runningMode,valid)
  }
}