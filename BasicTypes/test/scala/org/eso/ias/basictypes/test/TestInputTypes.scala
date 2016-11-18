package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.typedmp.IASTypes
import org.eso.ias.prototype.input.AlarmValue

/**
 * Test inputs against different types
 */
class TestInputTypes  extends FlatSpec {
  // The ID of the alarms built bin this test 
  val id = new Identifier(Some[String]("LongMPID"), None)
  val refreshRate=HeteroInOut.MinRefreshRate+10;
  
  behavior of "A monitor point"
  
  it must "throws an exception with a type mismatch" in {
    
    val mpLong:  HeteroInOut = HeteroInOut.monitorPoint(id,refreshRate,IASTypes.LongType)
    val mpAlarm: HeteroInOut = HeteroInOut.monitorPoint(id,refreshRate,IASTypes.AlarmType)
    
    assertThrows[ClassCastException] {
      mpLong.updateValue(new AlarmValue)
    }
    assertThrows[ClassCastException] {
     mpLong.updateValue(7.5) 
    }
    
    assertThrows[ClassCastException] {
      mpAlarm.updateValue(-1)
    }
    assertThrows[ClassCastException] {
     mpAlarm.updateValue(true) 
    }
    
  }
  
  
}