package org.eso.ias.prototype.behavior;

import org.eso.ias.prototype.input.Identifier;
import org.eso.ias.prototype.input.MonitorPointValue;
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
	final MonitorPointValue<T> value;
	
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
			OperationalMode mode,
			String id,
			String runningId,
			IASTypes valueType) {
		super();
		if (value==null) {
			throw new NullPointerException("The value can't be null");
		}
		if (mode==null) {
			throw new NullPointerException("The mode can't be null");
		}
		this.value = new MonitorPointValue<T>(value);
		this.mode = mode;
		this.id=id;
		this.runningId=runningId;
		this.valueType=valueType;
	}
}
