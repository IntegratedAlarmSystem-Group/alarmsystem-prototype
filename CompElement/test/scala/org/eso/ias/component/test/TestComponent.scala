package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.compele.ComputingElement
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmState
import org.eso.ias.prototype.input.AckState
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.java.IASTypes
import org.eso.ias.prototype.input.HeteroInOut
import scala.collection.mutable.{Map => MutableMap }

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
  
  val mpRefreshRate = HeteroInOut.MinRefreshRate+50
  
  // The IDs of the monitor points in input 
  // to pass when building a Component
  val requiredInputIDs = List("ID1", "ID2")
  // The HashMap with the monitor points in input
  // to pass when building a Component
  val intialMPs = new HashMap[String,HeteroInOut]()
  
  // The ID of the first MP
  val mpI1Identifier = new Identifier(Some[String](requiredInputIDs(0)), Option[Identifier](compId))
  val mp1 = HeteroInOut(
      mpI1Identifier,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.ALARM)
  intialMPs(mp1.id.id.get)=mp1
  
  // The ID of the second MP
  val mpI2Identifier = new Identifier(Some[String](requiredInputIDs(1)), Option[Identifier](compId))
  val mp2 = HeteroInOut(
      mpI2Identifier,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.ALARM)
  intialMPs(mp2.id.id.get)=mp1
  val actualInputs: MutableMap[String, HeteroInOut] = MutableMap(mp1.id.id.get -> mp1,mp2.id.id.get -> mp2)
  
  behavior of "A Component"
  
  it must "be correctly initialized" in {
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      None,
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.ALARM)
      
    val comp: ComputingElement = new ComputingElement(
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
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.ALARM)
      
    val comp: ComputingElement = new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    assertThrows[IllegalStateException] {
      comp.shelve(true);
    }
  }
  
  it must "not allow to shelve a Non-AlarmValue output" in {
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.LONG)
      
    val comp: ComputingElement = new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    assertThrows[IllegalStateException] {
      comp.shelve(true);
    }
  }
  
  it must "shelve AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      alarmVal, 
      OperationalMode.OPERATIONAL,
      Validity.Unreliable,
      IASTypes.ALARM)
      
    val comp: ComputingElement = new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs);
    
    comp.shelve(true);
    
    val shelvedVal = comp.output.actualValue.get.value.asInstanceOf[AlarmValue]
    assert(shelvedVal.shelved)
    
  }
  
  it must "not allow to ack a None AlarmValue" in {
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.ALARM)
      
    val comp: ComputingElement= new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    assertThrows[IllegalStateException] {
      comp.ack();
    }
  }
  
  it must "not allow to ack a Non-AlarmValue output" in {
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      None, 
      OperationalMode.UNKNOWN,
      Validity.Unreliable,
      IASTypes.LONG)
      
    val comp: ComputingElement = new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    assertThrows[IllegalStateException] {
      comp.ack() 
    }
  }
  
  it must "ack an AlarmValue output" in {
    val alarmVal = new AlarmValue(AlarmState.Active,false,AckState.Acknowledged)
    val output = HeteroInOut(
      outId,
      mpRefreshRate,
      alarmVal, 
      OperationalMode.OPERATIONAL,
      Validity.Unreliable,
      IASTypes.ALARM)
      
    val comp: ComputingElement = new ComputingElement(
       compId,
       output,
       requiredInputIDs,
       actualInputs,
       "",
       intialMPs)
    
    comp.ack()
    
    val ackedVal = comp.output.actualValue.get.value.asInstanceOf[AlarmValue]
    assert(ackedVal.acknowledgement==AckState.Acknowledged)
  }
  
}