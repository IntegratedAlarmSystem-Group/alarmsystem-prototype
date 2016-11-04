package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper

/**
 * The value of the monitoring point is associated 
 * to a timestamp corresponding to the update time of 
 * the value
*/
class MonitorPointValue[A](
    val value:A) {
  val timestamp: Long = System.currentTimeMillis()
  
  override def toString(): String = { 
    "Value "+value.toString() +
    " updated at "+ISO8601Helper.getTimestamp(timestamp)
  }
}

/**
 * A  <code>MonitorPoint</code> extends  <code>MonitorPointBase</code> 
 * by holding the value of a monitor point.
 * 
 * It is parametrized because a monitor point can be a double, an integer, an
 * array of integers and many other types. (Should we look for a better name?)
 * 
 * <code>MonitorPoint</code> is intended for internal use only:
 * objects of this type shall not be instantiated directly but accessed
 * through objects defined in <code>org.eso.ias.prototype.input.typedmp</code>.
 * 
 * <code>MonitorPoint</code> is immutable.
 * 
 * MonitorPoint have an order that is the order of their Identifier
 * 
 * @param ident The unique ID of the monitor point
 * @param actualValue The value of the monitor point
 * @param mode The operational mode
 * @param valid: The validity of the monitor point
 * @author acaproni
 */
class MonitorPoint[A] protected (
    ident: Identifier, // The unique ID of this MonitorPoint
    val actualValue: Option[MonitorPointValue[A]] = None, // Uninitialized at build time
    mode: OperationalMode.Mode = OperationalMode.Unknown,
    valid: Validity.Value = Validity.Unreliable) 
extends MonitorPointBase(ident,mode,valid) with Ordered[MonitorPointBase] {
  
  /**
   * Compare two monitor points.
   * 
   * The ordering of MonitorPoints is the same of their identifier.
   * 
   * @see Ordered
   */
  def compare(that: MonitorPointBase): Int = {
    ident.compare(that.id)
  }
  
  override def toString(): String = {
    "Monitor point " + id.toString() +
    (if (actualValue==None) "\n\t" else " with a value of type " +actualValue.get.value.getClass().getName())+"\n\t" +  
    runningMode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue==None) "No value" else "Value: "+actualValue.get.toString())
  }
}

/** 
 *  Provides factory methods for building MonitorPoint objects
 */
object MonitorPoint {
  
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
   * Build a new MonitorPoint delegating to the TypedMonitorPoint
   * main constructor
   * 
   * @param ident: The unique ID of the monitor point
	 * @param actualVal: The value of the monitor point
 	 * @param mode; The operational mode
 	 * @param valid: The validity of the monitor point
   */
  def monitorPoint[A](ident: Identifier, // The unique ID of this MonitorPoint
    actualVal: Option[MonitorPointValue[A]] = None, // Uninitialized at build time
    mode: OperationalMode.Mode = OperationalMode.Unknown,
    valid: Validity.Value = Validity.Unreliable): MonitorPoint[A] = {
      new MonitorPoint[A](ident,actualVal,mode,valid)
  }
    
    /**
     * Build a monitor point without a value, mode and validity.
     * 
     * This factory method must be used to create a new monitor point,
     * not to update an existing one as it uses only defaults values
     */
  def monitorPoint[A](ident: Identifier):MonitorPoint[A] = {
    new MonitorPoint[A](ident)
  }
  
  /**
   * Factory method to get a new monitor point with updated value
   * 
   * @param oldMP: The monitor point to update
   * @param newValue: The new value of the monitor point
   * @return updates the passed monitor point with the given new value
   */
  def updateValue[A](oldMP: MonitorPoint[A], newValue: A):MonitorPoint[A] = {
    if (oldMP.actualValue!=None && oldMP.actualValue.get == newValue) oldMP
    else {
      val value = Option(new MonitorPointValue[A](newValue))
      if (!MonitorPoint.isSupportedType(value)) 
        throw new UnsupportedOperationException("Unsupported typed monitor type")
      new MonitorPoint[A](oldMP.id,value,oldMP.runningMode,oldMP.validity)
    }
  }
  
  /**
   * Factory method to get a new monitor point with updated mode
   * 
   * @param oldMP: The monitor point to update
   * @param newMode: The new mode of the monitor point
   * @return updates the passed monitor point with the given new mode
   */
  def updateMode[A](oldMP: MonitorPoint[A], newMode: OperationalMode.Mode):MonitorPoint[A] = {
    new MonitorPoint[A](oldMP.id,oldMP.actualValue,newMode,oldMP.validity)
  }
  
  /**
   * Factory method to get a new monitor point with updated validity
   * 
   * @param oldMP: The monitor point to update
   * @param validMode: The new validity of the monitor point
   * @return updates the passed monitor point with the given new validity
   */
  def updateValidity[A](oldMP: MonitorPoint[A], valid: Validity.Value):MonitorPoint[A] = {
    new MonitorPoint[A](oldMP.id,oldMP.actualValue,oldMP.runningMode,valid)
  }
}