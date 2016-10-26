package org.eso.ias.prototype.input

import org.eso.ias.prototype.utils.ISO8601Helper

/**
 * The context in which the monitor point is actually running
 */
object OperationalMode extends Enumeration {
  type Mode = Value
  val StartUp, ShutDown, Maintenance, Operational, Unknown = Value
}

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
 * A immutable MonitorPoint holds the value of a monitor point.
 * 
 * It is parametrized because a monitor point can be a double, an integer, an
 * array of integers and many other types. (Should we look for a better name?)
 * 
 * <code>MonitorPoint</code> is intended for internal use only:
 * objects of this type shall not be instantiated directly but accessed
 * through objects defined in <code>org.eso.ias.prototype.input.typedmp</code>.
 * 
 * @param id The unique ID of the monitor point
 * @param runningMode The operational mode
 * @param validity: The validity of the monitor point
 * @author acaproni
 */
class MonitorPoint[A] protected (
    val id: Identifier, // The unique ID of this MonitorPoint
    val actualValue: Option[MonitorPointValue[A]] = None, // Uninitialized at build time
    val runningMode: OperationalMode.Mode = OperationalMode.Unknown,
    val validity: Validity.Value = Validity.Unreliable) {
  require(Option(id) != None)
  require(Option(runningMode) != None)
  require(Option(validity) != None)

  /**
   * Update the value of the monitor point
   * 
   * @param v: the new value of the monitor point
   * @return a new MonitorPoint with the value set to v
   * 
   */
  def updateValue(v: A): MonitorPoint[A] = {
    val opt: Option[MonitorPointValue[A]] =  if (v==None) None else Some(new MonitorPointValue[A](v))
    new MonitorPoint[A](id,opt,runningMode,validity)
  }
  
  /**
   * Update the operational mode of the monitor point
   * 
   * @param mode The new operational mode
   * @return A new monitor point with the update operational mode 
   */
  def switchOperationalMode(mode: OperationalMode.Mode): MonitorPoint[A] = {
    new MonitorPoint[A](id,actualValue,mode,validity)
  }
  
  /**
   * Update the validity of the monitor point
   * 
   * @param validity The new validity
   * @return A new monitor point with the passed validity
   */
  def setValidity(newValidity: Validity.Value): MonitorPoint[A] = {
    new MonitorPoint[A](id,actualValue,runningMode,newValidity)
  }
  
  override def toString(): String = {
    "Monitor point " + id.toString() + "\n\t" + 
    runningMode.toString() + "\n\t" +
    validity.toString() +"\n\t" +
    (if (actualValue==None) "No value" else actualValue.get.toString())
  }
}