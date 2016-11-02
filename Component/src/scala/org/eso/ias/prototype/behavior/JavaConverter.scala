package org.eso.ias.prototype.behavior

import org.eso.ias.prototype.input.MonitorPointBase
import scala.collection.mutable.Buffer
import scala.collection.JavaConversions
import java.util.Collection
import org.eso.ias.prototype.input.Identifier

/**
 * <code>JavaConverter</code> converts scala inputs to java.
 * 
 * The purpose of this class is to offer to java developers data structs 
 * they are used to deal with, hiding scala details.
 * 
 * @param inputs The inputs of the component
 * @param ident: The identifier of this component
 * @author acaproni
 */
class JavaConverter(
    inputs: List[MonitorPointBase],
    ident: Identifier) {
    val javaInputs: Collection[MonitorPointBase] = JavaConversions.asJavaCollection(inputs)
    val id: String = ident.runningID
}