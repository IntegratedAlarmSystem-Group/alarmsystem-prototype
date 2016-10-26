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
class MonitorPointBase protected (
    val id: Identifier, // The unique ID of this MonitorPoint
    val runningMode: OperationalMode.Mode = OperationalMode.Unknown,
    val validity: Validity.Value = Validity.Unreliable) {
  require(Option(id) != None)
  require(Option(runningMode) != None)
  require(Option(validity) != None)
}