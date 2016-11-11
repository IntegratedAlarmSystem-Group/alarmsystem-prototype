package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.MonitorPointValue
import org.eso.ias.prototype.input.AlarmValue

/**
 * The monitor point of type Alarm.
 * 
 * @param ident The unique ID of the monitor point
 * @param refresh: The expected refresh rate (msec) of this monitor point
 *                 (to be used to assess its validity)
 * @param actualVal The value of the monitor point
 * @param mode The operational mode
 * @param validity: The validity of the monitor point
 * 
 * @see MonitorPoint
 * @author acaproni
 */
case class AlarmMP(ident: Identifier, // The unique ID of this MonitorPoint
    refresh: Int,
    actualVal: Option[MonitorPointValue[AlarmValue]] = None, // Uninitialized at build time
    mode: OperationalMode.Mode = OperationalMode.Unknown,
    valid: Validity.Value = Validity.Unreliable
    ) extends MonitorPoint[AlarmValue](ident,refresh,actualVal,mode,valid,IASTypes.AlarmType) {
}