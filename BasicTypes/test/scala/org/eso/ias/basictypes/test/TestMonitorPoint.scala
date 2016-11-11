package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.typedmp.IASTypes
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.input.typedmp.MonitorPointFactory
import org.eso.ias.prototype.input.typedmp.LongMP

/**
 * Test the LongMP
 * 
 * @author acaproni
 */
class TestMonitorPoint extends FlatSpec {
  // The ID of the alarms built bin this test 
  val id = new Identifier(Some[String]("LongMPID"), None)
  val refreshRate=MonitorPoint.MinRefreshRate+10;
  
  behavior of "A monitor point" 
  
  it must "have an ID" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    assert(mp.asInstanceOf[MonitorPoint[Long]].id == id)
    
    assert (!mp.actualValue.isDefined)
    assert(mp.runningMode == OperationalMode.Unknown)
    assert(mp.validity == Validity.Unreliable)
  }
  
  it must "Have the same ID after changing other props" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    
    // Change the value of the previous MP
    val mp2 = mp.updateValue(3L).asInstanceOf[LongMP]
    assert(mp2.id == mp.id)
    assert(mp2.actualValue.isDefined)
    assert(mp2.actualValue.get.value == 3L)
    // Trivial check of timestamp update
    assert(mp2.actualValue.get.timestamp > 0 && mp2.actualValue.get.timestamp<=System.currentTimeMillis() )
    assert(mp2.runningMode == OperationalMode.Unknown)
    assert(mp2.validity == Validity.Unreliable)
    
    // Change validity of the previous MP
    val mp3 = mp2.updateValidity(Validity.Reliable).asInstanceOf[LongMP]
    assert(mp3.id == mp.id)
    assert(mp3.actualValue.isDefined)
    assert(mp3.actualValue.get  == mp2.actualValue.get)
    assert(mp3.runningMode == mp2.runningMode)
    assert(mp3.validity == Validity.Reliable)
    
    // Change mode of the previous MP
    val mp4 = mp3.updateMode(OperationalMode.Operational).asInstanceOf[LongMP]
    assert(mp4.id == mp.id)
    assert(mp4.actualValue.isDefined)
    assert(mp4.actualValue.get  == mp3.actualValue.get)
    assert(mp4.runningMode == OperationalMode.Operational)
    assert(mp4.validity == mp3.validity)
  }
  
  it must "allow to update the value" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    val mpUpdatedValue = mp.updateValue(5L).asInstanceOf[LongMP]
    assert(mpUpdatedValue.actualValue.get.value==5L,"The values differ")    
  }
  
  it must "allow to update the validity" in {
    val mp: MonitorPointBase = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType)
    val mpUpdatedValidityRelaible = mp.updateValidity(Validity.Reliable)
    assert(mpUpdatedValidityRelaible.validity==Validity.Reliable,"The validities differ")
    
    val mpUpdatedValidityUnRelaible = mp.updateValidity(Validity.Unreliable)
    assert(mpUpdatedValidityUnRelaible.validity==Validity.Unreliable,"The validities differ")
  }
  
  it must "allow to update the mode" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    val mpUpdatedMode= mp.updateMode(OperationalMode.Operational).asInstanceOf[LongMP]
    assert(mpUpdatedMode.runningMode==OperationalMode.Operational,"The modes differ")
  }
  
  it must "allow to update the value and validity at once" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    val mpUpdated = mp.update(15L,Validity.Reliable).asInstanceOf[LongMP]
    assert(mpUpdated.actualValue.get.value==15L,"The values differ")
    assert(mpUpdated.validity==Validity.Reliable,"The validities differ")
  }
  
  it must "return the same object if values, validity or mode did not change" in {
    val mp: LongMP = MonitorPointFactory.monitorPoint[Long](id,refreshRate,IASTypes.LongType).asInstanceOf[LongMP]
    
    val upVal = mp.updateValue(10L).asInstanceOf[LongMP]
    assert(upVal.actualValue.get.value==10L,"The values differ")
    val upValAgain = upVal.updateValue(10L).asInstanceOf[LongMP]
    assert(upValAgain.actualValue.get.value==10L,"The value differ")
    assert(upVal eq upValAgain,"Unexpected new object after updating the value\n"+upVal.toString()+"\n"+upValAgain.toString())
    
    val upValidity = mp.updateValidity(Validity.Reliable)
    assert(upValidity.validity==Validity.Reliable,"The validity differ")
    val upValidityAgain = upValidity.updateValidity(Validity.Reliable)
    assert(upValidityAgain.validity==Validity.Reliable,"The validity differ")
    assert(upValidityAgain eq upValidity,"Unexpected new object after updating the validity")
    
    val upMode = mp.updateMode(OperationalMode.StartUp)
    assert(upMode.runningMode==OperationalMode.StartUp,"The mode differ")
    val upModeAgain = upMode.updateMode(OperationalMode.StartUp).asInstanceOf[LongMP]
    assert(upModeAgain.runningMode==OperationalMode.StartUp,"The mode differ")
    assert(upMode eq upModeAgain,"Unexpected new object after updating the mode")
        
    val mpUpdated = mp.update(15L,Validity.Unreliable).asInstanceOf[LongMP]
    val mpUpdated2 = mpUpdated.update(15L,Validity.Unreliable).asInstanceOf[LongMP]
    assert(mpUpdated eq mpUpdated2,"Unexpected new object after updating value and validity")
    
  }
  
}