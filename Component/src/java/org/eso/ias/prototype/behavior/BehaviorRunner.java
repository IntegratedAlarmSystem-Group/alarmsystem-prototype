package org.eso.ias.prototype.behavior;

import java.util.Collection;

import org.eso.ias.prototype.input.MonitorPointBase;
import org.eso.ias.prototype.input.MonitorPointValue;
import org.eso.ias.prototype.input.OperationalMode;
import org.eso.ias.prototype.input.Validity;
import org.eso.ias.prototype.input.Identifier;

public abstract class BehaviorRunner<T> {
	/**
	 * A java bean holding the value returned after evaluating the inputs.
	 * 
	 * 
	 * @author acaproni
	 *
	 * @param <T> The type of the output
	 */
	class IASOutput {
		/**
		 * Constructor
		 * 
		 * @param value The value of the output
		 * @param mode The mode of the output
		 * @param validity The validity of the output
		 */
		public IASOutput(T value,
				OperationalMode mode, 
				Validity validity) {
			super();
			if (value==null) {
				throw new NullPointerException("The value can't be null");
			}
			if (mode==null) {
				throw new NullPointerException("The mode can't be null");
			}
			if (validity==null) {
				throw new NullPointerException("The validity can't be null");
			}
			this.value = new MonitorPointValue<T>(value);
			this.mode = mode;
			this.validity = validity;
		}
		final MonitorPointValue<T> value;
		final OperationalMode mode;
		final Validity validity;
	}
	
	
	protected final Identifier id;
	
	public BehaviorRunner(Identifier id) {
		if (id==null) {
			throw new NullPointerException("The ID is null!");
		}
		this.id=id;
	}
	
	/**
	 * Initialize the BehaviorRunner.
	 * 
	 * The life cycle method is called once by the IAS before running eval.
	 * User initialization code goes here. In particular long lasting operations
	 * like reading from a database should go here while eval is supposed 
	 * to return as soon as possible.
	 */
	public abstract void initialize();
	
	/**
	 * Shuts down the BehaviorRunner when the IAS does not need it anymore.
	 * 
	 * This life cycle method should be called to clean up the resources assuming
	 * that it will never be called again by the IAS.
	 */
	public abstract void tearDown();
	
	/**
	 * Produces the output of the component by evaluating the inputs.
	 * @return
	 */
	public abstract IASOutput eval(Collection<MonitorPointBase> compInputs);
}
