package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.AlarmValue
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.Identifier

/**
 * The Alarm is a MonitorPoint whose value is the AlarmValue.
 * 
 * This way it is possible to unify alarms and monitor points.
 * 
 * @see org.eso.ias.prototype.input.AlarmValue
 */
case class Alarm (alarmID: Identifier) 
    extends MonitorPoint[AlarmValue](alarmID) {
  
}