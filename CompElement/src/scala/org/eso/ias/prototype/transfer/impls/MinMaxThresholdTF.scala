package org.eso.ias.prototype.transfer.impls

import org.eso.ias.prototype.transfer.ScalaTransferExecutor
import java.util.Properties
import org.eso.ias.prototype.input.HeteroInOut
import org.eso.ias.prototype.compele.exceptions.PropsMisconfiguredException
import org.eso.ias.prototype.compele.exceptions.UnexpectedNumberOfInputsException
import org.eso.ias.prototype.input.java.IASTypes._
import org.eso.ias.prototype.compele.exceptions.TypeMismatchException
import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.Set
import org.eso.ias.prototype.input.Clear

/**
 * The TF implementing a Min/Max threshold TF  (there is also
 * a java implementation for comparison).
 * 
 * The alarm is activated when the alarm is higher then
 * the max threshold or when it is lower then the low threshold.
 * 
 * We could call this alarm a "Non-nominal temperature" because it is 
 * equally set if the temperature is too low or is too high but
 * cannot distinguish between the 2 cases.
 * 
 * If we want to distinguish between the 2 cases,  we need 2 ASCE having 
 * the same input, one checking for the high value and the other checking 
 * for the low value.
 * 
 * To be generic, the value of the properties and that of the HIO 
 * are converted in double.
 * 
 * The value of the Min and Max thresholds are passed as properties:
 * <UL>
 * 	<LI>HighON: the (high) alarm is activated when the value of the HIO 
 *              is greater then HighON
 *  <LI>HighOFF: if the (high) alarm is active and the value of the HIO
 *               goes below HighOFF, then the alarm is deactivated
 *  <LI>LowOFF: if the (low) alarm is active and the value of the HIO
 *               becomes greater then LowOFF, then the alarm is deactivated
 *  <LI>LowON: the (low) alarm is activated when the value of the HIO is
 *             lower then LowON
 *             
 * @author acaproni
 */
class MinMaxThresholdTF(cEleId: String, cEleRunningId: String, props: Properties) 
extends ScalaTransferExecutor(cEleId,cEleRunningId,props) {
  
  /**
   * The (high) alarm is activated when the value of the HIO 
   * is greater then HighON
   */
  lazy val highOn: Double = getValue(props, MinMaxThresholdTF.highOnPropName, Double.MaxValue)
  
  /**
   * if the (high) alarm is active and the value of the HIO
   * goes below HighOFF, then the alarm is deactivated
   */
  lazy val highOff: Double = getValue(props, MinMaxThresholdTF.highOffPropName, Double.MaxValue)
  
  /**
   * the (low) alarm is activated when the value of the HIO is
   * lower then LowON
   */
  lazy val lowOn: Double =  getValue(props, MinMaxThresholdTF.lowOnPropName, Double.MinValue)
  
  /**
   * if the (low) alarm is active and the value of the HIO
   * becomes greater then LowOFF, then the alarm is deactivated
   */
  lazy val lowOff: Double = getValue(props, MinMaxThresholdTF.lowOffPropName, Double.MinValue)
  
  /**
   * Get the value of a property from the passed properties.
   * 
   * @param props: The properties to look for the property with 
   *               the given name
   * @param propName: the name of the property
   * @param default: the value to return if the property is not defined 
   *                 in the passed properties
   */
  def getValue(props: Properties, propName: String, default: Double): Double = {
    val propStr = Option[String](props.getProperty(propName))
    if (propStr.isDefined) {
      propStr.get.toDouble
    } else {
      default
    }
  }
  
  /**
   * Initialize the TF by getting the four properties
   * (being the properties lazy, they will be initialized here.
   * 
   * This method merely checks if the values of the properties are coherent
   * with the definitions given above.
   * 
   * @see TransferExecutor#initialize()
   */
  def initialize() {
    if (highOn<highOff) {
      throw new PropsMisconfiguredException(Map[String,String](MinMaxThresholdTF.highOnPropName->highOn.toString(),MinMaxThresholdTF.highOffPropName->highOff.toString()))
    }
    if (lowOff<lowOn) {
      throw new PropsMisconfiguredException(Map[String,String](MinMaxThresholdTF.lowOnPropName->lowOn.toString(),MinMaxThresholdTF.lowOffPropName->lowOff.toString()))
    }
    if (lowOff>highOff) {
      throw new PropsMisconfiguredException(Map[String,String](MinMaxThresholdTF.lowOffPropName->lowOff.toString(),MinMaxThresholdTF.highOffPropName->highOff.toString()))
    }
  }
  
  /**
   * @see TransferExecutor#shutdown()
   */
  def shutdown() {}
  
  /**
   * @see ScalaTransferExecutor#eval
   */
  def eval(compInputs: Map[String, HeteroInOut], actualOutput: HeteroInOut): HeteroInOut = {
    if (compInputs.size!=1) throw new UnexpectedNumberOfInputsException(compInputs.size,1)
    if (actualOutput.iasType!=ALARM) throw new TypeMismatchException(actualOutput.id.runningID)
    
    // Get the input
    val hio = compInputs.values.head
    
    val hioValue: Double = hio.iasType match {
      case LONG => hio.actualValue.get.value.asInstanceOf[Long].toDouble
      case INT => hio.actualValue.get.value.asInstanceOf[Int].toDouble
      case SHORT => hio.actualValue.get.value.asInstanceOf[Short].toDouble
      case BYTE => hio.actualValue.get.value.asInstanceOf[Byte].toDouble
      case DOUBLE => hio.actualValue.get.value.asInstanceOf[Double]
      case FLOAT => hio.actualValue.get.value.asInstanceOf[Float].toDouble
      case _ => throw new TypeMismatchException(hio.id.runningID)
    }
    
    if (hioValue>=highOn || hioValue<=lowOn) {
      val actualOutputValue=actualOutput.actualValue.get.value.asInstanceOf[AlarmValue]
      val newValue: AlarmValue  = AlarmValue.transition(actualOutputValue,new Set())
      actualOutput.updateValue(newValue)
    } else if (hioValue<highOff && hioValue>lowOff) {
      val actualOutputValue=actualOutput.actualValue.get.value.asInstanceOf[AlarmValue]
      val newValue: AlarmValue  = AlarmValue.transition(actualOutputValue,new Clear())
      actualOutput.updateValue(newValue)
    } else {
      actualOutput
    }
    
  }
  
}

object MinMaxThresholdTF {
 /**
   * The name of the HighOn property
   */
  val highOnPropName = "org.eso.ias.tf.minmaxthreshold.highOn"
  
  /**
   * The name of the HighOff property
   */
  val highOffPropName = "org.eso.ias.tf.minmaxthreshold.highOff"
  
  /**
   * The name of the lowOn property
   */
  val lowOnPropName = "org.eso.ias.tf.minmaxthreshold.lowOn"
  
  /**
   * The name of the lowOff property
   */
  val lowOffPropName = "org.eso.ias.tf.minmaxthreshold.lowOff" 
}