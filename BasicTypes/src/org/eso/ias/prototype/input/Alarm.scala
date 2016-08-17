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
 * An Alarm has three possible states (@see AlarmState) a context plus a shelved property.
 */
case class Alarm(
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

object Alarm {

  /**
   * The transition of the state of an alarm as a result of an event
   * 
   * @param a: the alarm that receives the event
   * @param e: the event to apply to the alarm
   * @result the alarm after the event has been processed
   */
  def transition(a: Alarm, e: Event): Alarm = {
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
  
  def shelve(a: Alarm): Alarm = a.copy(shelved=true)
  
  def unshelve(a: Alarm): Alarm = a.copy(shelved=false)
  
  
}
