package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.AlarmState
import org.eso.ias.prototype.input.AckState
import org.eso.ias.prototype.input.java.IASTypes
import org.eso.ias.prototype.input.JavaConverter
import org.eso.ias.prototype.input.java.IASValue
import org.eso.ias.prototype.input.Clear

/**
 * Test the conversion between HIO to IASValue and vice-versa
 */
class TestJavaConversion  extends FlatSpec {
  behavior of "The Scala<->Java converter"
  
  def fixture = {
    new {
      // The IDs
      val parentId = new Identifier(Some[String]("ParentID"),None)
      val doubleHioId = new Identifier(Some[String]("DoubleID"),Option[Identifier](parentId))
      val alarmHioId = new Identifier(Some[String]("AlarmID"),Option[Identifier](parentId))
      // Refresh rate
      val refRate = HeteroInOut.MinRefreshRate+10
      // Modes
      val doubleMode = OperationalMode.MAINTENANCE
      val alarmMode = OperationalMode.STARTUP
      // The values
      val alarmValue = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
      val doubleValue = 48.6.toDouble
      // Validity
      val validity = Validity.Reliable
      // The HIOs
      val alarmHIO = HeteroInOut[AlarmValue](alarmHioId,refRate,alarmValue,alarmMode,validity,IASTypes.ALARM)
      val doubleHIO = HeteroInOut[Double](doubleHioId,refRate,doubleValue,doubleMode,validity,IASTypes.DOUBLE)
    }
  }
  
  it must "build the java value with the proper values" in {
    val f = fixture
    val doubleVal = JavaConverter.hioToIASValue[Double](f.doubleHIO)
    assert(doubleVal.valueType==f.doubleHIO.iasType)
    assert(doubleVal.mode==f.doubleHIO.mode)
    assert(doubleVal.timestamp==f.doubleHIO.actualValue.get.timestamp)
    assert(doubleVal.id==f.doubleHIO.id.id.get)
    assert(doubleVal.runningId==f.doubleHIO.id.runningID)
    assert(doubleVal.value==f.doubleHIO.actualValue.get.value)
    
    val alarmVal = JavaConverter.hioToIASValue[AlarmValue](f.alarmHIO)
    assert(alarmVal.value==f.alarmHIO.actualValue.get.value)
  }
  
  it must "Update a HIO with the values from a IASValue" in {
    val f = fixture
    
    val doubleVal = JavaConverter.hioToIASValue[Double](f.doubleHIO)
    val newdoubleVal = new IASValue[Double](doubleVal.value+8.5,System.currentTimeMillis(),OperationalMode.OPERATIONAL,doubleVal.id,doubleVal.runningId,doubleVal.valueType)
    val hio = JavaConverter.updateHIOWithIasValue[Double](f.doubleHIO,newdoubleVal)
    
    assert(newdoubleVal.valueType==hio.iasType)
    assert(newdoubleVal.mode==hio.mode)
    assert(newdoubleVal.id==hio.id.id.get)
    assert(newdoubleVal.runningId==hio.id.runningID)
    assert(newdoubleVal.value==hio.actualValue.get.value)
    
    val alarmVal = JavaConverter.hioToIASValue[AlarmValue](f.alarmHIO)
    val alarm = alarmVal.value.asInstanceOf[AlarmValue]
    val newAlarm = AlarmValue.transition(alarm, new Clear())
    val newAlarmValue = alarmVal.updateValue(newAlarm)
    val alarmHio = JavaConverter.updateHIOWithIasValue[AlarmValue](f.alarmHIO,newAlarmValue)
    
    assert(alarmHio.actualValue.get.value.asInstanceOf[AlarmValue].alarmState==AlarmState.Cleared)
  }
  
  it must "fail updating with wrong IDs, runningIDs, type" in {
    val f = fixture
    val doubleVal = JavaConverter.hioToIASValue[Double](f.doubleHIO)
    // Build a IASValue with another ID
    val newDoubleValueWrongId = new IASValue(
        doubleVal.value,
        doubleVal.timestamp,
        doubleVal.mode,
        doubleVal.id+"WRONG!",
        doubleVal.runningId,
        doubleVal.valueType)
    
    assertThrows[IllegalStateException] {
      JavaConverter.updateHIOWithIasValue[Double](f.doubleHIO, newDoubleValueWrongId)
    }
    
    val newDoubleValueWrongRunId = new IASValue(
        doubleVal.value,
        doubleVal.timestamp,
        doubleVal.mode,
        doubleVal.id,
        doubleVal.runningId+"WRONG!",
        doubleVal.valueType)
    assertThrows[IllegalStateException] {
      JavaConverter.updateHIOWithIasValue[Double](f.doubleHIO, newDoubleValueWrongRunId)
    }
    
    val newDoubleValueWrongType = new IASValue(
        doubleVal.value,
        doubleVal.timestamp,
        doubleVal.mode,
        doubleVal.id,
        doubleVal.runningId,
        IASTypes.BOOLEAN)
    assertThrows[IllegalStateException] {
      JavaConverter.updateHIOWithIasValue[Double](f.doubleHIO, newDoubleValueWrongType)
    }
    
  }
  
}