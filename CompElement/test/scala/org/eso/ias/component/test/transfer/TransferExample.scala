package org.eso.ias.component.test.transfer

import java.util.Properties

import org.eso.ias.prototype.transfer.ScalaTransferExecutor
import org.eso.ias.prototype.input.HeteroInOut

/**
 * A scala TransferExecutor for testing purposes
 * 
 * @see TransferExecutor
 */
class TransferExample(
    cEleId: String, 
		cEleRunningId: String,
		props: Properties) extends ScalaTransferExecutor(cEleId,cEleRunningId,props) {
  
  /**
   * Intialization
   * 
   * @see TransferExecutor
   */
  def initialize() {
    println("Scala TransferExample intializing")
  }
  
  /**
   * Shut dwon
   * 
   * @see TransferExecutor
   */
  def shutdown() {
    println("Scala TransferExample shutting down")
  }
  
  def eval(compInputs: Map[String, HeteroInOut], actualOutput: HeteroInOut): HeteroInOut = {
    actualOutput
  }
  
}