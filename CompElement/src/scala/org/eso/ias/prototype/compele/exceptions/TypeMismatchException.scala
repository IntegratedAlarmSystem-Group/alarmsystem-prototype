package org.eso.ias.prototype.compele.exceptions

/**
 * Exception thrown by the TF executor when the type
 * of a HIOs does not match with the expected on
 */
class TypeMismatchException(hioId: String) 
extends Exception("Type mismatch for HIO "+hioId) {
}