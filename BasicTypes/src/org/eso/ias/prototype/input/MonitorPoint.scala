package org.eso.ias.prototype.input

/**
 * The context in which the monitor point is actually running
 */
object OperationalMode extends Enumeration {
  type Mode = Value
  val StartUp, ShutDown, Maintenance, Operational = Value
}

/**
 * A monitor point holds the value of a monitor point.
 * It is parametrized because a monitor point can be a double an integer, an
 * array of integers and many other types. (Should we look for a better name?)
 * 
 * <code>MonitorPoint</code> is intended for internal use only:
 * objects of this type shall not be instantiated directly (this class has, in fact abstract)
 * <code>org.eso.ias.prototype.input.typedmp</code> provides a set of types
 * for the monitor points.    
 */
abstract class MonitorPoint[A](
    val id: String,
    val runningMode: OperationalMode.Mode = OperationalMode.Operational) {
  
    /**
     * The value of the monitoring point is associated 
     * to a timestamp corrsponding to the update time of 
     * the value
     */
    class MonitorPointValue[A](
        val value:A) {
      
      val timestamp: Long = System.currentTimeMillis()
    }
  
  /**
   *  The value of the monitor point
   *  Uninitialized at build time
   *  
   *  
   */
  private var actualValue: Option[MonitorPointValue[A]] = None
  
  /**
   * Getter
   */
  protected def setValue(v: A): Unit = {
    actualValue = Option(new MonitorPointValue[A](v))
  }
  
  /**
   * Setter
   */
  def getValue: Option[MonitorPointValue[A]] = {
    actualValue
  }
}