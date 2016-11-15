package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper
import org.eso.ias.prototype.input.typedmp.IASTypes
import org.eso.ias.prototype.input.typedmp.MonitorPointFactory

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
 * @param refreshRate: The expected refresh rate (msec) of this monitor point
 *                     (to be used to assess its validity)
 * @param valid: The validity of the monitor point
 * @param theType: is the IAS type of this MonitorPoint
 * 
 * @see IASTYpe
 * 
 * @author acaproni
 */
class MonitorPoint[A] protected[input] (
    ident: Identifier,
    val refreshRate: Int,
    val actualValue: Option[MonitorPointValue[A]],
    mode: OperationalMode.Mode,
    valid: Validity.Value,
    theType: IASTypes.Value) 
extends MonitorPointBase(ident,mode,valid,theType) {
  require(ident!=None,"The identifier can't be None")
  require(refreshRate>=MonitorPoint.MinRefreshRate,"Invalid refresh rate (too low): "+refreshRate)
  
   
  override def toString(): String = {
    "Monitor point " + id.toString() +" of IAS type " +iasType+"\n\t" +  
    runningMode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue==None) "No value" else "Value: "+actualValue.get.toString())
  }
  
  /**
   * Update the value and validity of the monitor point
   */
  def update(newValue: A,valid: Validity.Value):MonitorPoint[A]= {
    if (
        actualValue!=None && 
        actualValue.get.value == newValue &&
        valid==validity) this
    else {
      val value = Option(new MonitorPointValue[A](newValue))
      MonitorPointFactory.monitorPoint[A](id,refreshRate,value,runningMode,valid,iasType).asInstanceOf[MonitorPoint[A]]
    }
  }
  
  /**
   * Update the value of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param newValue: The new value of the monitor point
   * @return updates the passed monitor point with the given new value
   */
  def updateValue(newValue: A):MonitorPoint[A] = {
    if (actualValue!=None && actualValue.get.value == newValue) this
    else {
      val value = Option(new MonitorPointValue[A](newValue))
      MonitorPointFactory.monitorPoint[A](
          id, 
          refreshRate, 
          value,
          runningMode, 
          validity,
          iasType).asInstanceOf[MonitorPoint[A]]
    }
  }
  
  /**
   * Update the mode of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param newMode: The new mode of the monitor point
   * @return updates the passed monitor point with the given new mode
   */
  override def updateMode(newMode: OperationalMode.Mode):MonitorPoint[A] = {
    if (newMode==runningMode) this
    else MonitorPointFactory.monitorPoint[A](id,refreshRate,actualValue,newMode,validity,iasType).asInstanceOf[MonitorPoint[A]]
  }
  
  /**
   * Update the validity of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param validMode: The new validity of the monitor point
   * @return updates the passed monitor point with the given new validity
   */
  override def updateValidity(valid: Validity.Value):MonitorPoint[A] = {
    if (valid==validity) this
    else MonitorPointFactory.monitorPoint[A](id,refreshRate,actualValue,runningMode,valid,iasType).asInstanceOf[MonitorPoint[A]]
  }
}

/** 
 *  Provides factory methods for building MonitorPoint objects
 */
object MonitorPoint {
  
  /**
   * The min possible value for the refresh rate
   * If it is too little the MP will be invalid most of the time; if too 
   * big it is not possible to understand if it has been properly refreshed or
   * the source is stuck/dead.
   */
  val MinRefreshRate = 50;
}