package org.eso.ias.prototype.input.java;

import org.eso.ias.prototype.input.Identifier;
import org.eso.ias.prototype.input.java.OperationalMode;
import java.lang.StringBuilder;
/**
 * The view of a heterogeneous inputs in the java code.
 * 
 * Objects of this class are immutable i.e. updating returns
 * a new immutable object
 * 
 * @author acaproni
 *
 */
public class IASValue<T> {
	
	/**
	 * The value of the HIO
	 */
	public final T value;
	
	/**
	 * The time when the value has been assigned to the HIO
	 */
	public final long timestamp;
	
	/**
	 * The mode of the input
	 * 
	 * @see OperationalMode
	 */
	public final OperationalMode mode;
	
	/**
	 * The identifier of the input
	 * 
	 * @see Identifier
	 */
	public final String id;
	
	/**
	 * The identifier of the input concatenated with
	 * that of its parents
	 * 
	 * @see Identifier
	 */
	public final String runningId;
	
	/**
	 * The IAS representation of the type of this input.
	 * 
	 * @see IASTypes
	 */
	public final IASTypes valueType;
	
	/**
	 * Build a new IASValue with the passed mode
	 * 
	 * @param newMode The mode to set in the new IASValue
	 * @return The new IASValue with the updated mode
	 */
	public IASValue<T> updateMode(OperationalMode newMode) {
		if (newMode==null) {
			throw new NullPointerException("The mode can't be null");
		}
		return new IASValue<T>(value,System.currentTimeMillis(),newMode,id,runningId,valueType);
	}
	
	/**
	 * Build a new IASValue with the passed value
	 * 
	 * @param newValue The value to set in the new IASValue
	 * @return The new IASValue with the updated value
	 */
	public IASValue<T> updateValue(T newValue) {
		if (newValue==null) {
			throw new NullPointerException("The value can't be null");
		}
		return new IASValue<T>(newValue,System.currentTimeMillis(),mode,id,runningId,valueType);
	}
	

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
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("IASValue(id=");
		ret.append(id);
		ret.append(", runningID=");
		ret.append(runningId);
		ret.append(", value=");
		ret.append(value);
		ret.append(", timestamp=");
		ret.append(timestamp);
		ret.append(", mode=");
		ret.append(mode);
		ret.append(", type=");
		ret.append(valueType);
		ret.append(")");
		return ret.toString();
	}
}
