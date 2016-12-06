package org.eso.ias.component.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.transfer.TransferFunctionSetting
import org.eso.ias.prototype.transfer.TransferFunctionLanguage
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.input.java.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.java.IASTypes
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.compele.ComputingElement
import scala.collection.mutable.{Map => MutableMap }
import java.util.concurrent.ScheduledThreadPoolExecutor
import org.eso.ias.prototype.input.AlarmState
import java.util.Properties
import org.eso.ias.prototype.transfer.impls.MinMaxThresholdTF

class TestMinMaxThreshold extends FlatSpec{
 
  trait TFBuilder {
    // The thread factory used by the setting to async
    // intialize and shutdown the TF objects
    val threadFactory = new TestThreadFactory()
    
    // The TF executor to test
    val scalaMinMaxTF = new TransferFunctionSetting(
        "org.eso.ias.prototype.transfer.impls.MinMaxThresholdTF",
        TransferFunctionLanguage.scala,
        threadFactory)
    
    // The ID of the DAS where the components runs
    val dasId = new Identifier(Some[String]("MinMaxTF-DAS-ID"),None)
    
    // The ID of the component running into the DAS
    val compID = new Identifier(Some[String]("MinMaxTF-COMP-ID"),Option[Identifier](dasId))
    
    // The refresh rate of the component
    val mpRefreshRate = HeteroInOut.MinRefreshRate+500
    
    // The ID of the output generated by the component
    val outId = new Identifier(Some[String]("MinMaxOutputId"), None)
    // Build the MP in output
    val alarmVal = new AlarmValue()
    val output: HeteroInOut = HeteroInOut(
      outId,
      mpRefreshRate,
      alarmVal, 
      OperationalMode.OPERATIONAL,
      Validity.Unreliable, IASTypes.ALARM)
    
    // Create the HIO in input
   val hioId = new Identifier(Some[String]("HIO"),Option[Identifier](compID))
   val hio = HeteroInOut(
          hioId,
          mpRefreshRate,
          1L, 
          OperationalMode.OPERATIONAL,
          Validity.Unreliable, IASTypes.LONG)

    //val threadFactory: CompEleThreadFactory = new CompEleThreadFactory("Test-runningId")
    val requiredInputIDs = List(hioId.id.get)
    
    val inputsMPs = MutableMap(hioId.id.get->hio)
    
    // Instantiate one ASCE with a scala TF implementation
    val scalaTFSetting =new TransferFunctionSetting(
        "org.eso.ias.prototype.transfer.impls.MinMaxThresholdTF",
        TransferFunctionLanguage.scala,
        threadFactory)
    
    val props: Properties
    
    
    val scalaComp: ComputingElement = new ComputingElement(
       compID,
       output,
       requiredInputIDs,
       inputsMPs,
       scalaTFSetting,
       Some[Properties](props))
  }
  
  behavior of "The MinMaxThreshold executor"
  
  it must "Correctly load, init and shutdown the TF executor" in new TFBuilder {
    val props = new Properties()
    assert(!scalaMinMaxTF.initialized)
    assert(!scalaMinMaxTF.isShutDown)
    scalaMinMaxTF.initialize("ASCE-MinMaxTF-ID", "ASCE-running-ID", System.getProperties)
    Thread.sleep(500)
    assert(scalaMinMaxTF.initialized)
    assert(!scalaMinMaxTF.isShutDown)
    scalaMinMaxTF.shutdown()
    Thread.sleep(500)
    assert(scalaMinMaxTF.initialized)
    assert(scalaMinMaxTF.isShutDown)
    
    assert(threadFactory.numberOfAliveThreads()==0)
    assert(threadFactory.instantiatedThreads==2)
  }
  
  it must "run the scala Min/Max TF executor" in new TFBuilder {
    val props = new Properties()
    props.put(MinMaxThresholdTF.highOnPropName, "50")
    props.put(MinMaxThresholdTF.highOffPropName, "25")
    props.put(MinMaxThresholdTF.lowOffPropName, "-10")
    props.put(MinMaxThresholdTF.lowOnPropName, "-20")
    val stpe: ScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5)
    scalaComp.initialize(stpe)
    println("Sleeping")
    Thread.sleep(5000)
    // Change the input to trigger the TF
    val changedMP = inputsMPs(inputsMPs.keys.head).updateValue(5L)
    scalaComp.inputChanged(Some(changedMP))
    Thread.sleep(5000)
    scalaComp.shutdown()
    
    println(scalaComp.output.actualValue.toString())
    val alarm = scalaComp.output.actualValue.get.value.asInstanceOf[AlarmValue]
    assert(alarm.alarmState==AlarmState.Active)
  }
  
}