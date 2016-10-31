package org.eso.ias.prototype.component

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.utils.ISO8601Helper
import org.eso.ias.prototype.input.MonitorPoint

/**
 * A snapshot of a AlarmSystemComponent at a given point in time
 * useful for after-the-facts investigation and debugging.
 * 
 * The state is composed of an output, generated by the
 * script applied to the inputs (i.e. monitor points and/or
 * other alarms).
 * The AlarmSystemComponent change its state when the input changes
 * (for example the value of a monitor point changes) or when the operators 
 * acknowledge or shelve the alarm (if the output is an alarm).
 * 
 * The output of a ASC is normally an alarm generated by digesting the values
 * of the inputs to the component itself.
 * Sometimes, the output is a MonitorPoint of a given type (for example an integer) 
 * to implement what we called synthetic parameters. 
 * 
 * Note that object of this class does not necessarily represent a change of 
 * the output i.e. the alarm been set or cleared. 
 * In fact, an object is created also when one of its input changed even if
 * the change did not trigger a change of the output because of the algorithm 
 * in the script digesting the inputs.
 * 
 * Objects of this class are immutable.
 * 
 * @param id: the unique identifier of the ASC
 * @param output: The output alarm produced applying the script to the inputs
 * @param inputs: The inputs (i.e. monitor points and alarms) of the Component
 * @param behaviorScript: The script to update the output depending on the
   *                      values of the inputs.
 */
class ASCState[T] (
    val id: Identifier,
    val output: MonitorPoint[T],
    val inputs: List[MonitorPointBase],
    val behaviorScript: String)
{
  /**
   * The point in time when this objects (i.e. the snapshot) has been
   * built.
   */
  val timestamp = System.currentTimeMillis()
  
  override def toString() = {
    val outStr: StringBuilder = new StringBuilder("State of component ")
    outStr.append(id.toString())
    outStr.append(" generated at ")
    outStr.append(ISO8601Helper.getTimestamp(timestamp))
    outStr.append("\n>Output<\n")
    outStr.append(output.toString())
    outStr.append("\n>Inputs<\n")
    for (mp <- inputs) outStr.append(mp.toString())
    outStr.append("\n>Script<\n")
    outStr.append(behaviorScript)
    outStr.toString()
  }
}