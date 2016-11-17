package org.eso.ias.prototype.behavior;

import java.util.Collection;
import java.util.Properties;

import org.eso.ias.prototype.input.Identifier;
import org.eso.ias.prototype.input.MonitorPoint;

/**
 * 
 * <code>TransferExecutor<code> is the abstract for the
 * implementators of the transfer function in java.
 * 
 * @author acaproni
 *
 * @param <T> The type of the value produced evaluating the inputs
 *            i.e. the type of the ASCE
 */
public abstract class TransferExecutor<T> {
	
	/**
	 * The ID of the computational element that runs this
	 * transfer function
	 */
	protected final String compElementId;
	
	/**
	 * The ID of the computational element that runs this
	 * transfer function extended with the IDs of its parents
	 * 
	 * @see Identifier
	 */
	protected final String compElementRunningId;
	
	/**
	 * Propertis for this executor.
	 */
	protected final Properties props;
	
	/**
	 * Constructor
	 * 
	 * @param cEleId: The id of the ASCE
	 * @param cEleRunningId: the running ID of the ASCE
	 */
	public TransferExecutor(
			String cEleId, 
			String cEleRunningId,
			Properties props) {
		if (cEleId==null) {
			throw new NullPointerException("The ID is null!");
		}
		this.compElementId=cEleId;
		if (cEleRunningId==null) {
			throw new NullPointerException("The running ID is null!");
		}
		this.compElementRunningId=cEleRunningId;
		if (props==null) {
			throw new NullPointerException("The properties is null!");
		}
		this.props=props;
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
	 * 
	 * @param compInputs: the inputs to the ASCE
	 * @return the computed value to set as output of the ASCE
	 */
	public abstract IASValue<T> eval(Collection<MonitorPoint> compInputs);
}
