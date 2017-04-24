package fsm_guice.states;

import fsm.FsmState;
import fsm.StateMachine;
import fsm_guice.TestContext;
import fsm_guice.TestTrigger;

public abstract class BaseState implements FsmState<TestTrigger, TestContext> {
	
	private boolean isCurrent;

	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		// TODO Auto-generated method stub
	}

	protected void doExiting() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public final void entering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		isCurrent = true;
		doEntering(stateMachine, context);
	}
	
	@Override
	public final void exiting() {
		doExiting();
		isCurrent = false;
	}
	
	@Override
	public boolean getIsCurrent() {
		return isCurrent;
	}
}
