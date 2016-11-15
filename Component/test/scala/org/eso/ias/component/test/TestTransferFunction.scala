package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.component.AlarmSystemComponent
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.component.AlarmSystemComponentBase
import org.eso.ias.prototype.input.typedmp.MonitorPointFactory
import org.eso.ias.prototype.input.typedmp.IASTypes

class TestTransferFunction extends FlatSpec {
  
  /**
   * Builds a Component with a set of inputs to test the transfer method
   */
  trait CompBuilder {
    
    val numOfInputs = 5
    
    // The ID of the DAS where the components runs
    val dasId = new Identifier(Some[String]("DAS-ID"),None)
    
    // The ID of the component running into the DAS
    val compID = new Identifier(Some[String]("COMP-ID"),Option[Identifier](dasId))
    
    // The refresh rate of the component
    val mpRefreshRate = MonitorPoint.MinRefreshRate+50
    
    // The ID of the output generated by the component
    val outId = new Identifier(Some[String]("OutputId"), None)
    // Build the MP in output
    val alarmVal = new AlarmValue()
    val mpVal: Option[MonitorPointValue[AlarmValue]] = Some(new MonitorPointValue[AlarmValue](alarmVal))
    val output: MonitorPoint[AlarmValue] = MonitorPointFactory.monitorPoint(
      outId,
      mpRefreshRate,
      mpVal, 
      OperationalMode.Operational,
      Validity.Unreliable, IASTypes.AlarmType).asInstanceOf[MonitorPoint[AlarmValue]]
      
    // The IDs of the monitor points in input 
    // to pass when building a Component
    val requiredInputIDs = (for (i <- 1 to numOfInputs)  yield ("ID"+i)).toList
    
    // Create numOfInputs MPs
    var i=0 // To create different types of MPs
    val inputsMPs: List[MonitorPointBase] = for (id <- requiredInputIDs) yield {
      val mpId = new Identifier(Some[String](id),Option[Identifier](compID))
      i=i+1
      if ((i%2)==0) {
        val mpVal = Option[MonitorPointValue[AlarmValue]](new MonitorPointValue[AlarmValue](new AlarmValue()))
        MonitorPointFactory.monitorPoint(
          mpId,
          mpRefreshRate,
          mpVal, 
          OperationalMode.Operational,
          Validity.Unreliable, IASTypes.AlarmType)
      } else {
        val mpVal = Option[MonitorPointValue[Long]](new MonitorPointValue[Long](1L))
        MonitorPointFactory.monitorPoint(
          mpId,
          mpRefreshRate,
          mpVal, 
          OperationalMode.Operational,
          Validity.Unreliable, IASTypes.LongType)
      }
      
    }
    val comp: AlarmSystemComponent[AlarmValue] = new AlarmSystemComponent[AlarmValue](
       compID,
       output,
       requiredInputIDs,
       inputsMPs,
       "")
  }
  
  behavior of "The Component transfer function"
  
  it must "set the validity to Unreliable when at least one MP is Unreliable" in new CompBuilder {
    
    val computed= comp.transfer()
    
    var component: AlarmSystemComponentBase[AlarmValue] = comp
    for (i <- 1 until inputsMPs.size) {
      
      val changedMP = inputsMPs(i).updateValidity(Validity.Reliable)
          
      comp.asInstanceOf[AlarmSystemComponent[AlarmValue]].inputChanged(Some(changedMP))
      
      component=component.transfer()
      assert(component.output.validity==Validity.Unreliable)
    }
  }
  
  it must "set the validity to the lower value" in new CompBuilder {
    // This test checks if the validity is set to Reliable if all the
    // validities have this level
    // At the present, this is the only test we can do with only 2 values for the
    // validity
    val computed= comp.transfer()
    
    var component: AlarmSystemComponentBase[AlarmValue] = comp
    
    for (i <- 0 until inputsMPs.size) {
      
      val changedMP = inputsMPs(i).updateValidity(Validity.Reliable)
          
      component.asInstanceOf[AlarmSystemComponent[AlarmValue]].inputChanged(Some(changedMP))
      
      component=component.transfer()
      if (i<inputsMPs.size-1) assert(component.output.validity==Validity.Unreliable)
      else assert(component.output.validity==Validity.Reliable)
    }
  }
  
}