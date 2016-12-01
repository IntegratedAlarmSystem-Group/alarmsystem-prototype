package org.eso.ias.prototype.transfer;

import java.util.Map;
import java.util.Properties;

import org.eso.ias.prototype.input.java.IASValueBase;

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
public abstract class TransferExecutor {
	
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
	 * Properties for this executor.
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
	 * The life cycle method is called once by the IAS and always before running eval.
	 * User initialization code goes here. In particular long lasting operations
	 * like reading from a database should go here while eval is supposed 
	 * to return as soon as possible.
	 */
	public abstract void initialize();
	
	/**
	 * Shuts down the BehaviorRunner when the IAS does not need it anymore.
	 * 
	 * This life cycle method is called last, to clean up the resources.
	 * 
	 * It is supposed to return quickly, even if not mandatory.
	 */
	public abstract void shutdown();
	
	/**
	 * Produces the output of the component by evaluating the inputs.
	 * 
	 * <EM>IMPLEMENTATION NOTE</EM>
	 * The {@link IASValue} is immutable. The easiest way to produce
	 * the output to return is to execute the methods of the actualOutput
	 * that return a new IASValue.
	 * 
	 * @param compInputs: the inputs to the ASCE
	 * @param actualOutput: the actual output of the ASCE
	 * @return the computed value to set as output of the ASCE
	 */
	public abstract IASValueBase eval(Map<String, IASValueBase> compInputs, IASValueBase actualOutput);
}
