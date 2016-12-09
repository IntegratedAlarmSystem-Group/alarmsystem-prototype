package org.eso.ias.prototype.compele.exceptions

import org.eso.ias.prototype.input.java.IASTypes

/**
 * Exception thrown by the TF executor when the type
 * of a HIOs does not match with the expected on
 * 
 * @param hioId: the ID of the mismatched HIO
 * @param actualType: the type of the HIO
 * @param expectedType: the expected type of the HIO
 */
class TypeMismatchException(hioId: String, actualType: IASTypes, expectedType: IASTypes) 
extends Exception("Type mismatch for HIO "+hioId+": expected was "+expectedType+" but "+actualType+" found") {
}