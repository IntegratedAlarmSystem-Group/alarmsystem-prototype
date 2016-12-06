package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.compele.ComputingElement
import org.eso.ias.prototype.compele.ComputingElementBase
import org.eso.ias.prototype.input.java.IASTypes
import scala.collection.mutable.{Map => MutableMap }
import org.eso.ias.prototype.transfer.TransferFunctionSetting
import java.util.Properties
import org.eso.ias.prototype.transfer.TransferFunctionLanguage
import java.util.concurrent.ScheduledThreadPoolExecutor
import org.eso.ias.prototype.compele.CompEleThreadFactory
import org.eso.ias.prototype.input.AlarmState

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
    val mpRefreshRate = HeteroInOut.MinRefreshRate+500
    
    // The ID of the output generated by the component
    val outId = new Identifier(Some[String]("OutputId"), None)
    // Build the MP in output
    val alarmVal = new AlarmValue()
    val output: HeteroInOut = HeteroInOut(
      outId,
      mpRefreshRate,
      alarmVal, 
      OperationalMode.OPERATIONAL,
      Validity.Unreliable, IASTypes.ALARM)
      
    // The IDs of the monitor points in input 
    // to pass when building a Component
    val requiredInputIDs = (for (i <- 1 to numOfInputs)  yield ("ID"+i)).toList
    
    // Create numOfInputs MPs
    var i=0 // To create different types of MPs
    val inputsMPs: MutableMap[String, HeteroInOut] = MutableMap[String, HeteroInOut]()
    for (id <- requiredInputIDs) {
      val mpId = new Identifier(Some[String](id),Option[Identifier](compID))
      i=i+1
      val mp = if ((i%2)==0) {
        val mpVal = new AlarmValue()
        HeteroInOut(
          mpId,
          mpRefreshRate,
          mpVal, 
          OperationalMode.OPERATIONAL,
          Validity.Unreliable, IASTypes.ALARM)
      } else {
        val mpVal = 1L
        HeteroInOut(
          mpId,
          mpRefreshRate,
          mpVal, 
          OperationalMode.OPERATIONAL,
          Validity.Unreliable, IASTypes.LONG)
      }
      inputsMPs+=(mp.id.id.get -> mp)
    }
    val threadFactory: CompEleThreadFactory = new CompEleThreadFactory("Test-runningId")
    
    // Instantiate on ASCE with a java TF implementation
    val javaTFSetting =new TransferFunctionSetting(
        "org.eso.ias.component.test.transfer.TransferExecutorImpl",
        TransferFunctionLanguage.java,
        threadFactory)
    val javaComp: ComputingElement = new ComputingElement(
       compID,
       output,
       requiredInputIDs,
       inputsMPs,
       javaTFSetting,
       Some[Properties](new Properties()))
    
    
    // Instantiate one ASCE with a scala TF implementation
    val scalaTFSetting =new TransferFunctionSetting(
        "org.eso.ias.component.test.transfer.TransferExample",
        TransferFunctionLanguage.scala,
        threadFactory)
    val scalaComp: ComputingElement = new ComputingElement(
       compID,
       output,
       requiredInputIDs,
       inputsMPs,
       scalaTFSetting,
       Some[Properties](new Properties()))
  }
  
  behavior of "The Component transfer function"
  
  /**
   * This test checks if the validity is set to Reliable if all the
   * validities have this level.
   */
  it must "set the validity to the lower value" in new CompBuilder {
    val component: ComputingElementBase = javaComp
    javaComp.initialize(new ScheduledThreadPoolExecutor(2))
    
    val keys=inputsMPs.keys.toList.sorted
    keys.foreach { key  => {
      val changedMP = inputsMPs(key).updateValidity(Validity.Reliable)
      javaComp.inputChanged(Some(changedMP))
      } 
    }
    // Leave time to run the TF
    Thread.sleep(3000)
    javaComp.shutdown()
    assert(component.output.validity==Validity.Reliable)
  }
  
  it must "run the java TF executor" in new CompBuilder {
    val stpe: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5)
    javaComp.initialize(stpe)
    Thread.sleep(5000)
    // Change one input to trigger the TF
    val changedMP = inputsMPs(inputsMPs.keys.head).updateValidity(Validity.Reliable)
    javaComp.inputChanged(Some(changedMP))
    Thread.sleep(5000)
    javaComp.shutdown()
    println(javaComp.output.actualValue.toString())
    val alarm = javaComp.output.actualValue.get.value.asInstanceOf[AlarmValue]
    assert(alarm.alarmState==AlarmState.Active)
  }
  
  it must "run the scala TF executor" in new CompBuilder {
    val stpe: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5)
    scalaComp.initialize(stpe)
    println("Sleeping")
    Thread.sleep(5000)
    // Change one input to trigger the TF
    val changedMP = inputsMPs(inputsMPs.keys.head).updateValidity(Validity.Reliable)
    scalaComp.inputChanged(Some(changedMP))
    Thread.sleep(5000)
    scalaComp.shutdown()
    
    println(scalaComp.output.actualValue.toString())
    val alarm = scalaComp.output.actualValue.get.value.asInstanceOf[AlarmValue]
    assert(alarm.alarmState==AlarmState.Active)
  }
  
}