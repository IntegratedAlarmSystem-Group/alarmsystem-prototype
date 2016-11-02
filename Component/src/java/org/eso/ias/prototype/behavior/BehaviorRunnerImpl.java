package org.eso.ias.prototype.behavior;

import java.util.Collection;

import org.eso.ias.prototype.input.MonitorPointBase;
import org.eso.ias.prototype.input.Identifier;

public class BehaviorRunnerImpl<T>  extends BehaviorRunner<T> {
	
	public BehaviorRunnerImpl(Identifier id) {
		super(id);
	}

	@Override
	public void initialize() {
		System.out.println("BehaviorRunnerImpl: Initializing");
	}

	@Override
	public void tearDown() {
		System.out.println("BehaviorRunnerImpl: shutting down");
	}
	
	public IASOutput eval(Collection<MonitorPointBase> compInputs) {
		System.out.println("BehaviorRunnerImpl: evaluating "+compInputs.size()+" inputs");
		System.out.println("BehaviorRunnerImpl for comp. with ID="+id);
		for (MonitorPointBase input: compInputs) {
			System.out.println(input);
		}
		return null;
	}
}
