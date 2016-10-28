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
extends MonitorPointBase(ident,mode,valid) 