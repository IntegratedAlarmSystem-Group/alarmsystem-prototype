package org.eso.ias.prototype.input

/**
 * The context in which the monitor point is actually running
 */
object OperationalMode extends Enumeration {
  type Mode = Value
  val StartUp, ShutDown, Maintenance, Operational, Unknown = Value
}

/**
 * A immutable MonitorPointBase is the base
 * class of all MonitorPoints.
 * 
 * <code>MonitorPointBase</code> is intended for internal use only.
 * <code>MonitorPointBase</code> is immutable.
 * 
 * @param id The unique ID of the monitor point
 * @param runningMode The operational mode
 * @param validity: The validity of the monitor point
 * 
 * @see Monitorpoint
 * @author acaproni
 */
abstract class MonitorPointBase protected (
    val id: Identifier, // The unique ID of this MonitorPoint
    val runningMode: OperationalMode.Mode = OperationalMode.Unknown,
    val validity: Validity.Value = Validity.Unreliable) extends Ordered[MonitorPointBase] {
  require(Option(id) != None)
  require(Option(runningMode) != None)
  require(Option(validity) != None)
  
  /**
   * Compare two monitor points.
   * 
   * The ordering of MonitorPoints is the same of their identifier.
   * 
   * @see Ordered
   */
  def compare(that: MonitorPointBase): Int = {
    this.id.compare(that.id)
  }
  
  /**
   * Update the mode of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param newMode: The new mode of the monitor point
   * @return updates the passed monitor point with the given new mode
   */
  def updateMode(newMode: OperationalMode.Mode): MonitorPointBase
  
  /**
   * Update the validity of the monitor point
   * 
   * @param oldMP: The monitor point to update
   * @param validMode: The new validity of the monitor point
   * @return updates the passed monitor point with the given new validity
   */
  def updateValidity(valid: Validity.Value): MonitorPointBase
}