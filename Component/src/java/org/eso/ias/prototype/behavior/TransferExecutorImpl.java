package org.eso.ias.prototype.behavior;

import java.util.Collection;
import java.util.Properties;

import org.eso.ias.prototype.input.HeteroInOut;

public class TransferExecutorImpl<T>  extends TransferExecutor<T> {
	
	public TransferExecutorImpl(String cEleId, 
			String cEleRunningId,
			Properties props) {
		super(cEleId,cEleRunningId,props);
	}

	@Override
	public void initialize() {
		System.out.println("TransferExecutorImpl: Initializing");
	}

	@Override
	public void tearDown() {
		System.out.println("TransferExecutorImpl: shutting down");
	}
	
	public IASValue eval(Collection<HeteroInOut> compInputs) {
		System.out.println("TransferExecutorImpl: evaluating "+compInputs.size()+" inputs");
		System.out.println("TransferExecutorImpl for comp. with ID="+compElementId);
		for (HeteroInOut input: compInputs) {
			System.out.println(input);
		}
		return null;
	}
}
