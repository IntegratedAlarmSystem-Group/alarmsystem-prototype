package org.eso.ias.prototype.behavior

import scala.collection.mutable.Buffer
import scala.collection.JavaConversions
import java.util.Collection
import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.HeteroInOut

/**
 * <code>JavaConverter</code> offers commodity methods 
 * to convert scala data structures to java.
 * 
 * The purpose of this methods is to offer to java developers 
 * data structs they are used to deal with, hiding scala details.
 * 
 * @author acaproni
 */
object JavaConverter {
    
  /**
   * Convert the HIOs in input in a Ordered collection of
   * IASValues (hpefully) easy to deal with from java
   */
  def buildInputs(inputs: Map[String, HeteroInOut]): Unit = {
    
  }
     
}