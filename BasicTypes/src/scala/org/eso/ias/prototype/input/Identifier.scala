package org.eso.ias.prototype.input

/**
 * Companion object
 */
object Identifier {
  /**
   * The separator between the ID and the parentID to build the runningID
   */
  val separator = '@'
}

/**
 * The immutable Identifier is composed of the uniqueID plus the parent ID
 * 
 * @param id: The unique identifier
 * @param parentID: The identifier of the parent
 */
class Identifier(val id: String, val parentID: String) {
  require (!Option(id).getOrElse("").isEmpty)
  require(id.indexOf(Identifier.separator) == -1)
  require (!Option(parentID).getOrElse("").isEmpty)
  
  /**
   * The runningID composed of the id plus the id of the parent
   * allows to uniquely identify at object at run time
   */
  val runningID = id +Identifier.separator+parentID
  
  override def toString = runningID
}