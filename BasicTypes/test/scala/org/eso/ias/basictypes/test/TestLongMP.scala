package org.eso.ias.basictypes.test

import org.scalatest.FlatSpec
import org.eso.ias.prototype.input.typedmp.LongMP
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.OperationalMode

/**
 * Test the LongMP
 * 
 * @author acaproni
 */
class TestLongMP extends FlatSpec {
  // The ID of the alarms built bin this test 
  val id = "TheUniqueID"
  
  "A long monitor point" must "have an ID" in {
    val mp: LongMP = new LongMP(id)
    assert(mp.mpID == id)
    
    assert (!mp.actualValue.isDefined)
    assert(mp.runningMode == OperationalMode.Unknown)
    assert(mp.validity == Validity.Unreliable)
  }
  
  it must "Have the same ID after changing other props" in {
    val mp: LongMP = new LongMP(id)
    
    // Change the value of the previous MP
    val mp2 = mp.updateValue(3L)
    assert(mp2.id == mp.id)
    assert(mp2.actualValue.isDefined)
    assert(mp2.actualValue.get.value == 3L)
    // Trivial check of timestamp update
    assert(mp2.actualValue.get.timestamp > 0 && mp2.actualValue.get.timestamp<=System.currentTimeMillis() )
    assert(mp2.runningMode == OperationalMode.Unknown)
    assert(mp2.validity == Validity.Unreliable)
    
    // Change validity of the previous MP
    val mp3 = mp2.setValidity(Validity.Reliable);
    assert(mp3.id == mp.id)
    assert(mp3.actualValue.isDefined)
    assert(mp3.actualValue.get  == mp2.actualValue.get)
    assert(mp3.runningMode == mp2.runningMode)
    assert(mp3.validity == Validity.Reliable)
    
    // Change mode of the previous MP
    val mp4 = mp3.switchOperationalMode(OperationalMode.Operational)
    assert(mp4.id == mp.id)
    assert(mp4.actualValue.isDefined)
    assert(mp4.actualValue.get  == mp3.actualValue.get)
    assert(mp4.runningMode == OperationalMode.Operational)
    assert(mp4.validity == mp3.validity)
  }
}