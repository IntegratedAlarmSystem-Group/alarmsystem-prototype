package org.eso.ias.prototype.compele

/**
 * The state machine for the ASCE
 * 
 * The ASCE transitions through 6 different states during its 
 * life time 
 */
abstract class FiniteStateMachineState[T <: Enumeration](val state: T)

/**
 * The state of the ASCE
 */
object AsceStates extends Enumeration {
  type State = Value
  
  val Initing = Value("Initializing") // Initializing
  val Healthy = Value("Running") // Everything OK
  val TFBroken = Value("TF broken") // The transfer function is too broken
  val TFSlow = Value("TF slow") // The transfer function is slow
  val ShuttingDown = Value("Shutting down") // The ASCE is shutting down
  val Closed = Value("Closed") // Closed i.e. shutdown complete
  
  // The alarm has been set by the alarm source
  val Active = Value("Active") 
  // The alarm has been cleared by the alarm source
  val Cleared = Value("Cleared") 
  
  /**
   * When the state of the ASCE is in
   * this list, the transfer function is not executed
   */
  val inhibitorStates = Set(Initing, TFBroken, ShuttingDown, Closed)
}

/**
 *  The events to switch the state of the ASCE
 */
trait Event

/**
 *  The ASCE has been initialized i.e. at least
 *  the TF class has been loaded and intialized
 */
case class Initialized() extends Event

/**
 * The user provided TF is broken 
 * i.e it typically threw an exception.
 */
case class Broken() extends Event

/**
 * The user provided TF is too slow
 */
case class Slow() extends Event

/**
 * The user provided TF is back to normal
 */
case class Normal() extends Event

/**
 *  The ASCE has been shutdown
 *  This state shuts down the TF
 */
case class Shutdown() extends Event

/**
 *  The TF has been shutdown and the ASCE is now closed 
 */
case class Close() extends Event

/**
 * The exception thrown when the actual state does not accept a transition
 */
class InvalidAsceStateTransitionException(
    actualState: AsceStates.State,
    transition: Event) extends Exception(
       "Invalid transition "+transition+" from "+actualState+" state"
    )

/**
 * The ASCE state 
 */
class AsceState(val actualState: AsceStates.State = AsceStates.Initing) {
  
  /**
   * @return true if the TF can be executed in the current state
   */
  def canRunTF(): Boolean = !AsceStates.inhibitorStates.contains(actualState)
  
  override def toString() = actualState.toString()
  
}

/**
 * The implementation of the ASCE state machine transitions
 */
object AsceState {
  
  /**
   * The transition of the state of a ASCE as a result of an event
   * 
   * @param asceState: the ASCE state that receives the event
   * @param e: the event to apply to the current state of the ASCE
   * @result the new state of the ASCE after applying the event to the current state
   */
  def transition(asceState: AsceState, e: Event): AsceState = {
    asceState.actualState match {
      case AsceStates.Initing =>
        e match {
          case Initialized() => new AsceState(AsceStates.Healthy)
          case Shutdown()  => new AsceState(AsceStates.ShuttingDown)
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
      case AsceStates.Healthy =>
        e match {
          case Broken()  => new AsceState(AsceStates.TFBroken)
          case Slow()  => new AsceState(AsceStates.TFSlow)
          case Shutdown()  => new AsceState(AsceStates.ShuttingDown)
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
      case AsceStates.TFBroken =>
        e match {
          case Shutdown()  => new AsceState(AsceStates.ShuttingDown)
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
        case AsceStates.TFSlow =>
        e match {
          case Normal()  => new AsceState(AsceStates.Healthy)
          case Shutdown()  => new AsceState(AsceStates.ShuttingDown)
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
      case AsceStates.ShuttingDown =>
        e match {
          case Close()  => new AsceState(AsceStates.Closed)
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
      case AsceStates.Closed =>
        // Final state
        e match {
          case _ => throw new InvalidAsceStateTransitionException(asceState.actualState,e)
        }
    }
  }
}




