package se.danielkonsult.fsm4j_guice.states;

import se.danielkonsult.fsm4j.FsmState;
import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j_guice.TestContext;
import se.danielkonsult.fsm4j_guice.TestTrigger;

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
