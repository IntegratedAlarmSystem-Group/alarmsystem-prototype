package org.eso.ias.prototype.input.typedmp

import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.OperationalMode
import org.eso.ias.prototype.input.Identifier

/**
 * <code>LongMP</code> implements a MonitorPoint for the type Long.
 */
case class LongMP(
    mpID: Identifier) 
    extends MonitorPoint[Long](mpID) {
}