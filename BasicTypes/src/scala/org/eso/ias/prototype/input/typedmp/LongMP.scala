package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.OperationalMode

/**
 * <code>LongMP</code> implements a MonitorPoint for the type Long.
 */
case class LongMP(
    mpID: String,
    mode: OperationalMode.Mode = OperationalMode.Unknown) 
    extends MonitorPoint[Long](mpID,mode) {
  
}