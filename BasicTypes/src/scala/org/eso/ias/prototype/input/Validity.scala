package org.eso.ias.prototype.input

/**
 * The validity of an alarm or value of a monitor point for example if the value
 * of a monitor point is not updated as expected (network problem?).
 * 
 * @author acaproni
 */
object Validity extends Enumeration {
  val Reliable = Value("Reliable") // Reliable
  val Unreliable = Value("Unreliable") // Unreliable
  
  /**
   * Check if the passed validity is "valid"
   * 
   * @param v: The validity to check
   * @return True if the passed validity is Reliable;
   * 			   False otherwise
   */
  def isValid(v: Validity.Value): Boolean = v==Reliable
}
