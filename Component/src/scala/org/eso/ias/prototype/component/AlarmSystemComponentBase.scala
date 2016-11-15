package org.eso.ias.prototype.component

import org.eso.ias.prototype.input.Identifier
import org.eso.ias.prototype.input.MonitorPointBase
import org.eso.ias.prototype.input.MonitorPoint
import org.eso.ias.prototype.input.Validity
import org.eso.ias.prototype.input.AlarmValue
import scala.util.control.NonFatal
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.eso.ias.prototype.behavior.BehaviorRunner
import org.eso.ias.prototype.behavior.BehaviorRunnerImpl
import org.eso.ias.prototype.behavior.JavaConverter
import scala.collection.mutable.HashMap
import org.eso.ias.prototype.input.AckState
import org.eso.ias.prototype.behavior.JavaTransfer
import scala.collection.mutable.{Set => MutableSet }
import org.eso.ias.prototype.input.typedmp.IASTypes

/**
 * The Integrated Alarm System Computing Element (ASCE) 
 * is the basic unit of the IAS. This  base class 
 * allows to implement stackable modifications
 * 
 * @param ident: The unique ID of this Component
 * @param out: The the output generated by this Component
 *             after applying the script to the inputs
 *             It can or cannot be an AlarmValue
 * @param requiredInputs: The IDs of the inputs that this component
 *                        needs to generate the output. The list does not change
 *                        during the life time of the component.
 * @param actualInputs: The list of monitor points in input that generated the actual output
 * @param script: The script that manipulated the inputs and generate the output
 * @param newInputs: the map with the value of the monitor points in input
 *                   received after the last update of the output  
 * @see AlarmSystemComponent, ASCState
 * @author acaproni
 */
abstract class AlarmSystemComponentBase[T] (
    ident: Identifier,
    out: MonitorPoint[T],
    requiredInputs: List[String],
    actualInputs: List[MonitorPointBase],
    script: String,
    val newInputs: HashMap[String, MonitorPointBase])
    extends ComputingElementState(ident,out,actualInputs,script) {
  require(requiredInputs!=None && !requiredInputs.isEmpty,"Invalid (empty or null) list of required inputs to the component")
  require(requiredInputs.size==actualInputs.size,"Inconsistent size of lists of inputs")
  
  /**
   * Update the output by running the user provided script/class against the inputs.
   * This is actually the core of the ASC.
   * 
   * A change of the inputs means a change in at least one of
   * the inputs of the list. 
   * A change, in turn, can be a change  of 
   * - the value (or alarm) 
   * - validity
   * - mode 
   * The change triggers a recalculation of the Validity.
   * 
   * The number of inputs of a ASC does not change during the
   * life span of a component, what changes are the values,
   * validity or mode of the inputs.
   * 
   * In case of an alarm, being ACK or shelved does not trigger
   * a  recalculation of the output.
   * 
   * The calculation of the input is delegated to the overloaded 
   * #transfer(...) that generated the output by stackable modifications.
   * The method provided here, updates the Validity.
   * 
   * @see transfer(...)
   */
  def transfer() : AlarmSystemComponentBase[T] = {
    println("AlarmSystemComponent[T].transfer()")
    
    // Prepare the list of the inputs by replacing the ones in the 
    // actualInputs with those in the newInputs
    val mixedInputs: List[MonitorPointBase] = newInputs.synchronized {
      val temp = mixInputs(actualInputs,newInputs)
      newInputs.clear()
      temp
    }
    
    println("mixedInputs contains "+mixedInputs.size+" items:")
    println(mixedInputs.mkString("\n"))
    
    val newOut=transfer(mixedInputs,ident,out)
    
    if ((out eq newOut) && mixedInputs==actualInputs) this
    else new AlarmSystemComponent[T](ident,newOut, requiredInputs, mixedInputs, script, newInputs)    
  }
  
  /**
   * Generate a new list of inputs by replacing the newly received inputs 
   * to those in the list of inputs
   * 
   * @param oldInput: The inputs that generated the old output of the Component
   * @param receivedInputs: the inputs that have been updated since the refresh
   *                        of the output of the component
   */
  private def mixInputs(
      oldInputs: List[MonitorPointBase], 
      receivedInputs: HashMap[String,MonitorPointBase] ): List[MonitorPointBase] = {
      
    receivedInputs.synchronized {
      for (oldMP <- oldInputs; newMP=receivedInputs.getOrElse(oldMP.id.runningID,oldMP)) yield newMP
    }
  } 
  
  /**
   * Update the output by running the user provided script/class against the inputs
   * by stackable modifications (@see the classes mixed in the {@link AlarmSystemComponent}
   * class)
   * 
   * This method sets the validity of the output from the validity of its inputs.
   * 
   * @param theInputs: The list of inputs 
   * @return The new output
   */
  def transfer(
      inputs: List[MonitorPointBase], 
      id: Identifier,
      actualOutput:MonitorPoint[T]) : MonitorPoint[T] = {
    
    val valitiesSet = MutableSet[Validity.Value]()
    for ( monitorPoint <- inputs ) valitiesSet += monitorPoint.validity
    val newValidity = Validity.min(valitiesSet.toList) 
    
    out.updateValidity(newValidity)
  }
  
  override def toString() = {
    val outStr: StringBuilder = new StringBuilder(super.toString())
    outStr.append("\n>ID of inputs<\n")
    outStr.append(requiredInputs.mkString(", "))
    outStr.append("\n>Not yet processed inputs<\n")
    newInputs.synchronized( { outStr.append(newInputs.values.mkString(", "))})
    outStr.toString()
  }
}
