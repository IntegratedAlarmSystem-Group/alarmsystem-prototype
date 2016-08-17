package org.eso.ias.prototype.input

/**
 * The validity of an alarm or value of a monitor point for example if the value
 * of a monitor point is not updated as expected (network problem?).
 */
trait Validity extends Enumeration {
  val Reliable = Value("Reliable") // Reliable
  val Unreliable = Value("Unreliable") // Unreliable
}