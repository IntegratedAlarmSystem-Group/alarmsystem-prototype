package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.AlarmValue

/**
 * The factory of all and only possible types of the MonitorPoints
 * 
 * MonitorPoints must always be built using this factory 
 * to keep under control the types of the monitor points 
 * running inside the IAS.
 * 
 * @see MonitorPoint
 * @author acaproni
 */
object MonitorPointFactory {
  
  /**
   * Factory method to build a new MonitorPoint
   * 
   * @param ident: The unique ID of the monitor point
   * @param refreshRate: The expected refresh rate of the MP
	 * @param actualVal: The value of the monitor point
 	 * @param mode; The operational mode
 	 * @param valid: The validity of the monitor point
 	 * @param iasType: The type of the monitor point
   */
  def monitorPoint[A](
      ident: Identifier,
      refreshRate: Int,
      actualVal: Option[MonitorPointValue[A]],
      mode: OperationalMode.Mode = OperationalMode.Unknown,
      valid: Validity.Value = Validity.Unreliable,
      iasType: IASTypes.Value): MonitorPointBase = {
    iasType match {
      case IASTypes.LongType => {
        val longActualVal = actualVal.asInstanceOf[Option[MonitorPointValue[Long]]]
        new LongMP(ident,refreshRate,longActualVal,mode,valid) 
      }
      case IASTypes.AlarmType => {
        val alarmActualVal = actualVal.asInstanceOf[Option[MonitorPointValue[AlarmValue]]]
        new AlarmMP(ident,refreshRate,alarmActualVal,mode,valid) 
      }
       case _ => throw new UnsupportedOperationException("Unsupported IAS type")
    }
  }
    
    /**
     * Build a monitor point without a value, mode and validity.
     * 
     * This factory method must be used to create a new monitor point,
     * not to update an existing one as it uses only defaults values
     * 
     * @param ident: The identifier of th eMP
     * @param refreshRate: The expected refresh rate of the MP
     */
  def monitorPoint[A](
      ident: Identifier, 
      refreshRate: Int,
      iasType: IASTypes.Value): MonitorPointBase = {
    iasType match {
      case IASTypes.LongType => new LongMP(ident,refreshRate) 
      case IASTypes.AlarmType => new AlarmMP(ident,refreshRate) 
      case _ => throw new UnsupportedOperationException("Unsupported IAS type")
    }
  }
}