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
 * 
 * <code>MonitorPoint</code> is intended for internal use only:
 * object of this type shall not be instantiated directly (this class has, in fact be made abstract)
 * <code>org.eso.ias.prototype.input.typedmp</code> provides a set of types
 * for the monitor points.    
 */
abstract class MonitorPoint[A](
    val id: String,
    val runningMode: OperationalMode.Mode = OperationalMode.Running) {
  
  /**
   *  The value of the monitor point
   *  Uninitialized at build time
   */
  private var actualValue: Option[A] = None
  
  /**
   * Getter
   */
  protected def setValue(v: A): Unit = {
    actualValue = Option(v)
  }
  
  /**
   * Setter
   */
  def getValue: Option[A] = {
    actualValue
  }
}