package org.eso.ias.prototype.behavior;

import org.eso.ias.prototype.input.Identifier;
import org.eso.ias.prototype.input.OperationalMode;
import org.eso.ias.prototype.input.typedmp.IASTypes;

/**
 * The heterogeneous inputs in the java code.
 * 
 * @author acaproni
 *
 */
public class IASValue<T> {
	
	/**
	 * The value of the input
	 */
	final T value;
	
	/**
	 * The time when the value has been assigned to the input
	 */
	final long timestamp;
	
	/**
	 * The mode of the input
	 * 
	 * @see OperationalMode
	 */
	final OperationalMode mode;
	
	/**
	 * The identifier of the input
	 * 
	 * @see Identifier
	 */
	final String id;
	
	/**
	 * The identifier of the input concatenated with
	 * that of its parents
	 * 
	 * @see Identifier
	 */
	final String runningId;
	
	/**
	 * The IAS representation of the type of this input.
	 * 
	 * @see IASTypes
	 */
	final IASTypes valueType;
	

	/**
	 * Constructor
	 * 
	 * @param value The value of the output
	 * @param mode The new mode of the output
	 * @param id: The ID of this input
	 * @param runningId: The id of this input and its parents
	 * @param valueType: the IAS type of this input
	 */
	public IASValue(T value,
			long tStamp,
			OperationalMode mode,
			String id,
			String runningId,
			IASTypes valueType) {
		super();
		if (mode==null) {
			throw new NullPointerException("The mode can't be null");
		}
		this.value = value;
		this.timestamp=tStamp;
		this.mode = mode;
		this.id=id;
		this.runningId=runningId;
		this.valueType=valueType;
	}
}
