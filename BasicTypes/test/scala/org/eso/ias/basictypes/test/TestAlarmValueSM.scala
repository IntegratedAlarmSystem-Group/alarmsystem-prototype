package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.AlarmState
import org.eso.ias.prototype.input.Ack
import org.eso.ias.prototype.input.InvalidStateTransitionException
import org.eso.ias.prototype.input.Set
import org.eso.ias.prototype.input.Clear

/**
 * Test the AlarmValue State Machine
 */
class TestAlarmValueSM extends FlatSpec {
   
   "The AlarmValue" must "initially have a Unknown state and not shelved" in {
     val v = new AlarmValue()
     assert(v.alarmState == AlarmState.Unknown)
     assert(!v.shelved)
   }
   
   /**
    * Test all transition from a Unknown alarm state
    */
   "Unknown state" must "correctly handle events" in {
     val as = AlarmValue()
     
     assertThrows[InvalidStateTransitionException] {
       val newState = AlarmValue.transition(as, Ack())
     }
     
     val setAlState = AlarmValue.transition(as,Set())
     assert(setAlState.alarmState == AlarmState.ActiveAndNew)
     
     val clearedAS = AlarmValue.transition(as,Clear())
     assert(clearedAS.alarmState == AlarmState.Cleared)
   }
   
   /**
    * Test all transition from a ActiveAndNew alarm state
    */
   "ActiveAndNew state" must "correctly handle events" in {
     
     // Setup: generate an alarm and transition to the ActiveAndNew state 
     val newas = AlarmValue()
     val activeAndNewAS = AlarmValue.transition(newas, Set())
     assert(activeAndNewAS.alarmState == AlarmState.ActiveAndNew)
     
     // Check event handling from ActiveAndNew state
     val afterSet = AlarmValue.transition(activeAndNewAS,Set())
     assert(afterSet.alarmState == AlarmState.ActiveAndNew)
     
     val afterAck = AlarmValue.transition(activeAndNewAS,Ack())
     assert(afterAck.alarmState == AlarmState.ActiveAndAcknowledged)
     
     val afterClear = AlarmValue.transition(activeAndNewAS,Clear())
     assert(afterClear.alarmState == AlarmState.Cleared)
   }
   
   /**
    * Test all transition from a ActiveAndAcknowledged alarm state
    */
   "ActiveAndAcknowledged state" must "correctly handle events" in {
     
     // Setup: generate an alarm and transition to the ActiveAndAcknowledged state 
     val newas = AlarmValue()
     val activeAndNewAS = AlarmValue.transition(newas, Set())
     val activeAndAckAS = AlarmValue.transition(activeAndNewAS, Ack())
     assert(activeAndAckAS.alarmState == AlarmState.ActiveAndAcknowledged)
     
     // Check event handling from ActiveAndAcknowledged state
     val afterSet = AlarmValue.transition(activeAndAckAS,Set())
     assert(afterSet.alarmState == AlarmState.ActiveAndAcknowledged)
     
     val afterAck = AlarmValue.transition(activeAndAckAS,Ack())
     assert(afterAck.alarmState == AlarmState.ActiveAndAcknowledged)
     
     val afterClear = AlarmValue.transition(activeAndAckAS,Clear())
     assert(afterClear.alarmState == AlarmState.Cleared)
   }
   
   /**
    * Test all transition from a Cleared alarm state
    */
   "Cleared state" must "correctly handle events" in {
     
     // Setup: generate an alarm and transition to the ActiveAndAcknowledged state 
     val newas = AlarmValue()
     val activeAndNewAS = AlarmValue.transition(newas, Set())
     val clearedAS = AlarmValue.transition(activeAndNewAS, Clear())
     assert(clearedAS.alarmState == AlarmState.Cleared)
     
     // Check event handling from ActiveAndAcknowledged state
     val afterSet = AlarmValue.transition(clearedAS,Set())
     assert(afterSet.alarmState == AlarmState.ActiveAndNew)
     
     val afterAck = AlarmValue.transition(clearedAS,Ack())
     assert(afterAck.alarmState == AlarmState.Cleared)
     
     val afterClear = AlarmValue.transition(clearedAS,Clear())
     assert(afterClear.alarmState == AlarmState.Cleared)
   }
}