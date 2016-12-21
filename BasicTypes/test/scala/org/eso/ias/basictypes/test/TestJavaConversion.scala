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
import org.eso.ias.prototype.input.java.IasDouble
import org.eso.ias.prototype.input.java.IasAlarm

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
      val mode = OperationalMode.OPERATIONAL
      // The values
      val alarmValue = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
      val doubleValue = 48.6D
      val floatValue = -11.5F
      val longValue = 11L
      val intValue = -76
      val shortValue = 15.toShort
      val byteValue = 43.toByte
      val charValue = 'X'
      val stringValue = "Test"
      val boolValue = false
      
      // Validity
      val validity = Validity.Reliable
      // The HIOs
      val longHIO = HeteroInOut[Long](doubleHioId,refRate,longValue,mode,validity,IASTypes.LONG)
      val intHIO = HeteroInOut[Int](doubleHioId,refRate,intValue,mode,validity,IASTypes.INT)
      val shortHIO = HeteroInOut[Short](doubleHioId,refRate,shortValue,mode,validity,IASTypes.SHORT)
      val byteHIO = HeteroInOut[Byte](doubleHioId,refRate,byteValue,mode,validity,IASTypes.BYTE)
      val charHIO = HeteroInOut[Char](doubleHioId,refRate,charValue,mode,validity,IASTypes.CHAR)
      val stringHIO = HeteroInOut[String](doubleHioId,refRate,stringValue,mode,validity,IASTypes.STRING)
      val boolHIO = HeteroInOut[Boolean](doubleHioId,refRate,boolValue,mode,validity,IASTypes.BOOLEAN)
      val alarmHIO = HeteroInOut[AlarmValue](alarmHioId,refRate,alarmValue,alarmMode,validity,IASTypes.ALARM)
      val doubleHIO = HeteroInOut[Double](doubleHioId,refRate,doubleValue,doubleMode,validity,IASTypes.DOUBLE)
      val floatHIO = HeteroInOut[Float](doubleHioId,refRate,floatValue,mode,validity,IASTypes.FLOAT)
      
      // Ensure we are testing all possible types
      val hios = List (longHIO,intHIO,shortHIO,byteHIO,charHIO,stringHIO,boolHIO,alarmHIO,doubleHIO,floatHIO)
      assert(hios.size==IASTypes.values().size)
    }
  }
  
  it must "build the java value with the proper values" in {
    val f = fixture
    val doubleVal = JavaConverter.hioToIASValue[Double](f.doubleHIO).asInstanceOf[IasDouble]
    assert(doubleVal.valueType==f.doubleHIO.iasType)
    assert(doubleVal.mode==f.doubleHIO.mode)
    assert(doubleVal.timestamp==f.doubleHIO.actualValue.get.timestamp)
    assert(doubleVal.id==f.doubleHIO.id.id.get)
    assert(doubleVal.runningId==f.doubleHIO.id.runningID)
    assert(doubleVal.value==f.doubleHIO.actualValue.get.value)
    
    val alarmVal = JavaConverter.hioToIASValue[AlarmValue](f.alarmHIO).asInstanceOf[IasAlarm]
    assert(alarmVal.value==f.alarmHIO.actualValue.get.value)
  }
  
  it must "Update a HIO with the values from a IASValue" in {
    val f = fixture
    
    val doubleVal = JavaConverter.hioToIASValue[Double](f.doubleHIO).asInstanceOf[IasDouble]
    val newdoubleVal = new IasDouble(doubleVal.value+8.5,System.currentTimeMillis(),OperationalMode.OPERATIONAL,doubleVal.id,doubleVal.runningId)
    val hio = JavaConverter.updateHIOWithIasValue(f.doubleHIO,newdoubleVal)
    
    assert(newdoubleVal.valueType==hio.iasType)
    assert(newdoubleVal.mode==hio.mode)
    assert(newdoubleVal.id==hio.id.id.get)
    assert(newdoubleVal.runningId==hio.id.runningID)
    assert(newdoubleVal.value==hio.actualValue.get.value)
    
    val alarmVal = JavaConverter.hioToIASValue[AlarmValue](f.alarmHIO).asInstanceOf[IasAlarm]
    val alarm = alarmVal.value
    val newAlarm = AlarmValue.transition(alarm, new Clear())
    val newAlarmValue = alarmVal.updateValue(newAlarm.right.get).asInstanceOf[IasAlarm]
    val alarmHio = JavaConverter.updateHIOWithIasValue(f.alarmHIO,newAlarmValue)
    
    assert(alarmHio.actualValue.get.value.asInstanceOf[AlarmValue].alarmState==AlarmState.Cleared)
  }
  
  it must "fail updating with wrong IDs, runningIDs, type" in {
    val f = fixture
    val doubleVal = JavaConverter.hioToIASValue(f.doubleHIO).asInstanceOf[IasDouble]
    // Build a IASValue with another ID
    val newDoubleValueWrongId = new IasDouble(
        doubleVal.value,
        doubleVal.timestamp,
        doubleVal.mode,
        doubleVal.id+"WRONG!",
        doubleVal.runningId)
    
    assertThrows[IllegalStateException] {
      JavaConverter.updateHIOWithIasValue(f.doubleHIO, newDoubleValueWrongId)
    }
    
    val newDoubleValueWrongRunId = new IasDouble(
        doubleVal.value,
        doubleVal.timestamp,
        doubleVal.mode,
        doubleVal.id,
        doubleVal.runningId+"WRONG!")
    assertThrows[IllegalStateException] {
      JavaConverter.updateHIOWithIasValue(f.doubleHIO, newDoubleValueWrongRunId)
    }
  }
  
}