package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.component.AlarmSystemComponent
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmState
import org.eso.ias.prototype.input.AckState
import org.eso.ias.prototype.input.MonitorPointValue
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.input.typedmp.MonitorPointFactory
import org.eso.ias.prototype.input.typedmp.AlarmMP
import org.eso.ias.prototype.input.typedmp.IASTypes
import org.eso.ias.prototype.input.typedmp.LongMP
import org.eso.ias.prototype.input.MonitorPoint

/**
 * Test the basic functionalities of the IAS Component,
 * while the functioning of the transfer function
 * is checked elsewhere.
 */
class TestComponent extends FlatSpec {
  // The ID of the output generated by the component
  val outId = new Identifier(Some[String]("OutputId"), None)
  
  // The ID of the DAS where the components runs
  val dasId = new Identifier(Some[String]("DAS-ID"),None)
  
  // The ID of the component to test
  val compId = new Identifier(Some[String]("ComponentId"), Option[Identifier](dasId))
  
  val mpRefreshRate = MonitorPoint.MinRefreshRate+50
  
  // The IDs of the monitor points in input 
  // to pass when building a Component
  val requiredInputIDs = List("ID1", "ID2")
  // The HashMap with the monitor points in input
  // to pass when building a Component
  val intialMPs = new HashMap[String,MonitorPointBase]()
  
  // The ID of the first MP
  val mpI1Identifier = new Identifier(Some[String](requiredInputIDs(0)), Option[Identifier](compId))
  val mpID1: AlarmMP = MonitorPointFactory.monitorPoint(
      mpI1Identifier,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
  intialMPs(mpID1.id.id.get)=mpID1
  
  // The ID of the second MP
  val mpI2Identifier = new Identifier(Some[String](requiredInputIDs(1)), Option[Identifier](compId))
  val mpID2: AlarmMP = MonitorPointFactory.monitorPoint(
      mpI2Identifier,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
  intialMPs(mpID2.id.id.get)=mpID1
  val actualInputs: List[MonitorPointBase] = List(mpID1,mpID2)
  
  behavior of "A Component"
  
  it must "be correctly initialized" in {
    val output: AlarmMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      None,
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    
    assert(comp.id==compId)
    assert(comp.inputs.size==requiredInputIDs.size)
    assert(comp.output.id==outId)
  }
  
  it must "not allow to shelve a None AlarmValue" in {
    val output: AlarmMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    assertThrows[IllegalStateException] {
      val shelved = comp.shelve(true);
    }
  }
  
  it must "not allow to shelve a Non-AlarmValue output" in {
    val output: LongMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.LongType).asInstanceOf[LongMP]
      
    val comp: AlarmSystemComponent[Long] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    assertThrows[IllegalStateException] {
      val shelved = comp.shelve(true);
    }
  }
  
  it must "shelve AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: AlarmMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    
    val shelved = comp.shelve(true);
    
    val shelvedVal = shelved.output.actualValue.get.value
    assert(shelvedVal.shelved)
    
  }
  
  it must "not allow to ack a None AlarmValue" in {
    val output: AlarmMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    assertThrows[IllegalStateException] {
      val acked = comp.ack();
    }
  }
  
  it must "not allow to ack a Non-AlarmValue output" in {
    val output: LongMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.Unknown,
      Validity.Unreliable,
      IASTypes.LongType).asInstanceOf[LongMP]
      
    val comp: AlarmSystemComponent[Long] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    assertThrows[IllegalStateException] {
      val acked = comp.ack() 
    }
  }
  
  it must "ack an AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: AlarmMP = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable,
      IASTypes.AlarmType).asInstanceOf[AlarmMP]
      
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    
    val acked = comp.ack()
    
    val ackedVal = acked.output.actualValue.get.value
    assert(ackedVal.acknowledgement==AckState.Acknowledged)
    
  }
  
}