package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.component.AlarmSystemComponent
import org.eso.ias.prototype.input.typedmp.TypedMonitorPoint
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmState
import org.eso.ias.prototype.input.AckState
import org.eso.ias.prototype.input.MonitorPointValue

class TestComponent extends FlatSpec {
  val outId = new Identifier("OutputId", "OutParentID"+Identifier.separator+"host")
  val compId = new Identifier("ComponentId", "ComponentParentID"+Identifier.separator+"host")
  
  behavior of "A Component"
  
  it must "be correctly initialized" in {
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      None,
      OperationalMode.Unknown,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    
    assert(comp.id==compId)
    assert(comp.inputs.isEmpty)
    assert(comp.output.id==outId)
  }
  
  it must "not allow to shelve a None AlarmValue" in {
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    assertThrows[IllegalStateException] {
      val shelved = comp.shelve(true);
    }
  }
  
  it must "not allow to shelve a Non-AlarmValue output" in {
    val output: TypedMonitorPoint[Long] = TypedMonitorPoint.typedMonitor(
      outId,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[Long] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    assertThrows[IllegalStateException] {
      val shelved = comp.shelve(true);
    }
  }
  
  it must "shelve AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    
    val shelved = comp.shelve(true);
    
    val shelvedVal = shelved.output.actualValue.get.value
    assert(shelvedVal.shelved)
    
  }
  
  it must "not allow to ack a None AlarmValue" in {
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    assertThrows[IllegalStateException] {
      val acked = comp.ack();
    }
  }
  
  it must "not allow to ack a Non-AlarmValue output" in {
    val output: TypedMonitorPoint[Long] = TypedMonitorPoint.typedMonitor(
      outId,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[Long] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    assertThrows[IllegalStateException] {
      val acked = comp.ack() 
    }
  }
  
  it must "ack an AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable)
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    
    val acked = comp.ack()
    
    val ackedVal = acked.output.actualValue.get.value
    assert(ackedVal.acknowledgement==AckState.Acknowledged)
    
  }
  
  it must " update the output when input changes" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      outId,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable)
     
     // Creates 3 MPs for inputs
    val mp1Id = new Identifier("MP1-ID",compId.runningID)
    val mp1Val = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mp1Opt: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](mp1Val))
    val mp1: TypedMonitorPoint[AlarmValue] = TypedMonitorPoint.typedMonitor(
      mp1Id,
      mp1Opt, 
      OperationalMode.Operational,
      Validity.Unreliable)
      
    val mp2Id = new Identifier("MP2-ID",compId.runningID)
    val mp2Opt: Option[MonitorPointValue[Long]] = Some(new MonitorPointValue[Long](2L))
    val mp2: TypedMonitorPoint[Long] = TypedMonitorPoint.typedMonitor(
      mp2Id,
      mp2Opt, 
      OperationalMode.StartUp,
      Validity.Unreliable)
      
    val mp3Id = new Identifier("MP3-ID",compId.runningID)
    val mp3Opt: Option[MonitorPointValue[Long]] = Some(new MonitorPointValue[Long](5L))
    val mp3: TypedMonitorPoint[Long] = TypedMonitorPoint.typedMonitor(
      mp3Id,
      mp3Opt, 
      OperationalMode.Maintenance,
      Validity.Reliable)
     
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       Nil,
       "");
    
    val computed= comp.inputChanged(mp1::mp2::mp3::Nil)
  }
  
}