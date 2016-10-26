package org.eso.ias.prototype.input

/**
 * The validity of an alarm or value of a monitor point for example if the value
 * of a monitor point is not updated as expected (network problem?).
 * 
 * @author acaproni
 */
object Validity extends Enumeration {
  
  // Scala uses the ID of the Value to order 
  // the Validity following the declaration order
  // of the Values. To avoid confusion we set the
  // id esplicitly
  val Unreliable = Value(0,"Unreliable") // Unreliable
  val Reliable = Value(1,"Reliable") // Reliable
  
  /**
   * Scans all the values of this object to get the min id.
   * 
   * Note that scala Enumeration returns the maxId but for our
   * purposes it is better to reason in terms of min values.
   * However the cost of this method is negligible because 
   * it is executed only once for very few elements.
   * 
   * We do not want to hardcode the value because it easy
   * to forget updating the hardcoded value when adding new
   * Valitiy.Values to this Enumeration
   */
  private val minID: Int = {
    val validities =Validity.values.toList
    val ids = for (validity <- validities) yield validity.id 
    val sorted = ids.sortWith( (l,r) => l<=r)
    sorted(0)
  }
  
  /**
   * Check if the passed validity is "valid"
   * 
   * @param v: The validity to check
   * @return True if the passed validity is Reliable;
   * 			   False otherwise
   */
  def isValid(v: Validity.Value): Boolean = v==Reliable
  
  def min(left: Validity.Value, right: Validity.Value): Validity.Value = {
    Validity.ValueOrdering.min(left, right)
  }
  
  /**
   * Look for the min value between all the validities in the
   * passed list.
   * 
   * @param vals The list of Validity to check for the min value
   * @return The min validity between those in the passed 
   *         non-empty List;
   *         Reliable if the list is empty
   * @see sortingFunction
   */
  def min(vals: List[Validity.Value]): Validity.Value = {
    // The function to sort the list
    val sortingFunction = (l,r) => Validity.ValueOrdering.lteq(l, r)
    if (vals.isEmpty) Reliable
    else vals.sortWith(sortingFunction)(0)
  }
}
