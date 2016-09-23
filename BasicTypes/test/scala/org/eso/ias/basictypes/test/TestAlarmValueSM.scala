package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.AlarmState

/**
 * Test the AlarmValue State Machine
 */
class TestAlarmValueSM extends FlatSpec {
  
   behavior of "An alarm state machine"
   
   it must "be Unknown" in {
     val v = new AlarmValue()
     assert(v.alarmState == AlarmState.Unknown)
   }
}