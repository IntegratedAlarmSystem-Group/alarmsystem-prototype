package org.eso.ias.prototype.input

abstract class FiniteStateMachineState[T <: Enumeration](val state: T)

/**
 * The state of an alarm
 */
object AlarmState extends Enumeration {
  type State = Value
  val ActiveAndNew = Value("New") // Active and new
  val ActiveAndAcknowledged = Value("Acknowledged") // Active and acknowledged by operator
  val Cleared = Value("Cleared") // Not active (i.e. terminated)
  val Unknown = Value("Unknown") // Unknown state (an alarm should never have this state)
}

/**
 * The value of an alarm. 
 * 
 * The AlarmValue has a state (@see AlarmState) plus a shelved property.
 * In the design of the IAS, the Alarm is a special monitor point so
 * that at a certain level it is possible to use indifferently alarms and monitor points.
 * 
 * Objects from this class shall not be used directly:
 * <code>org.eso.ias.prototype.input.typedmp</code> provides a Alarm class.
 * 
 * @see org.eso.ias.prototype.input.typedmp.Alarm
 */
case class AlarmValue(
    alarmState: AlarmState.State = AlarmState.Unknown,  
    shelved: Boolean = false) {
}


// The events to switch state
trait Event
// A active or cleared alarm has been acknowledged
case class Ack() extends Event
// A new or acknowledged alarm became inactive
case class Clear() extends Event
// A cleared alarm became active again
case class Set() extends Event

/**
 * The AlarmValue companion implements the state class.
 */
object AlarmValue {

  /**
   * The transition of the state of an alarm as a result of an event
   * 
   * @param a: the alarm that receives the event
   * @param e: the event to apply to the alarm
   * @result the alarm after the event has been processed
   */
  def transition(a: AlarmValue, e: Event): AlarmValue = {
    a.alarmState match {
      case AlarmState.ActiveAndNew =>
        e match {
          case Ack() => a.copy(alarmState = AlarmState.ActiveAndAcknowledged)
          case Clear() => a.copy(alarmState = AlarmState.Cleared)
          case _ => a
        }
      case AlarmState.ActiveAndAcknowledged =>
        e match {
          case Clear() => a.copy(alarmState = AlarmState.Cleared)
          case _ => a
        }
      case AlarmState.Cleared =>
        e match {
          case Set() => a.copy(alarmState = AlarmState.ActiveAndNew)
          case _ => a
        }
      case _ => a
    }
  }
  
  def shelve(a: AlarmValue): AlarmValue = a.copy(shelved=true)
  
  def unshelve(a: AlarmValue): AlarmValue = a.copy(shelved=false)
  
  
}
