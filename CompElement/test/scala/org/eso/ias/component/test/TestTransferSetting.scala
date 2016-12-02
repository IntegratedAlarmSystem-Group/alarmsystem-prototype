package org.eso.ias.component.test

import org.scalatest.FlatSpec
import java.util.concurrent.ThreadFactory
import org.eso.ias.prototype.transfer.TransferFunctionSetting
import org.eso.ias.prototype.transfer.TransferFunctionLanguage

/**
 * Test the TransferFunctionSetting
 * 
 * @see TransferFunctionSetting
 */
class TestTransferFunctionSetting extends FlatSpec {
  
  /**
   * ThreadFactory for testing.
   * 
   * It keeps track of all the running threads to check if they
   * terminated
   */
  class TestThreadFactory() extends ThreadFactory {
    private var threads: List[Thread] = Nil
    
    private var count: Int = 0
     def newThread(r: Runnable): Thread = {
       val t: Thread = new Thread(r, "TestTransferFunctionSetting thread #"+count);
       t.setDaemon(true)
       threads = t::threads
       t
     }
    
    /**
     * The total number of threads instantiated by this 
     * factory
     */
    def instantiatedThreads = threads.size
    
    /**
     * @return The number of alive threads
     */
    def numberOfAliveThreads(): Int = {
      threads.filter(t => t.isAlive()).size
    }
  }
  
  trait TFBuilder {
    // The thread factory used by the setting to async
    // intialize and shutdown the TF objects
    val threadFactory = new TestThreadFactory()
    
    val javaTF = new TransferFunctionSetting(
        "org.eso.ias.component.test.transfer.TransferExecutorImpl",
        TransferFunctionLanguage.java,
        threadFactory)
    
    val scalaTF = new TransferFunctionSetting(
        "org.eso.ias.component.test.transfer.TransferExample",
        TransferFunctionLanguage.scala,
        threadFactory)
  }
  
  
  // The thread factory used by the setting to async
  // intialize and shutdown the TF objects
  val threadFactory = new TestThreadFactory() 
  
  behavior of "TransferFunctionSetting"
  
  it must "load, initialize and shutdown a java TF" in new TFBuilder {
    assert(!javaTF.initialized)
    assert(!javaTF.isShutDown)
    javaTF.initialize("ASCE-ID", "ASCE-running-ID", System.getProperties)
    Thread.sleep(500)
    assert(javaTF.initialized)
    assert(!javaTF.isShutDown)
    javaTF.shutdown()
    Thread.sleep(500)
    assert(javaTF.initialized)
    assert(javaTF.isShutDown)
    
    assert(threadFactory.numberOfAliveThreads()==0)
    assert(threadFactory.instantiatedThreads==2)
  }
  
  it must "load, initialize and shutdown a scala TF" in new TFBuilder {
    assert(!scalaTF.initialized)
    assert(!scalaTF.isShutDown)
    scalaTF.initialize("ASCE-ID", "ASCE-running-ID", System.getProperties)
    Thread.sleep(500)
    assert(scalaTF.initialized)
    assert(!scalaTF.isShutDown)
    scalaTF.shutdown()
    Thread.sleep(500)
    assert(scalaTF.initialized)
    assert(scalaTF.isShutDown)
    
    assert(threadFactory.numberOfAliveThreads()==0)
    assert(threadFactory.instantiatedThreads==2)
  }
}