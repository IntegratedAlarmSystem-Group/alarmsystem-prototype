package org.eso.ias.prototype.input

/**
 * The context in which the monitor point is actually running
 */
object OperationalMode extends Enumeration {
  type Mode = Value
  val StartUp, ShutDown, Maintenance, Running = Value
}

/**
 * A monitor point holds the value of a monitor point.
 * It is parametrized because a monitor point can be a double an integer, an
 * array of integers and many other types.  
 */
class MonitorPoint[A](
    id: String,
    runningMode: OperationalMode.Mode = OperationalMode.Running) {
  
  // The value of the monitor point
  // Uninitialized at build time
  var actualValue: Option[A] = None
  
  // The unique ID of this monitor point
  val mpID: String = id
}