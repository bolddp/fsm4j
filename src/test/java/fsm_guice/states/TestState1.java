package fsm_guice.states;

import com.google.inject.Inject;

import fsm.StateMachine;
import fsm_guice.TestContext;
import fsm_guice.TestTrigger;
import fsm_guice.services.ConcreteService01;

public class TestState1 extends BaseState {
	
	private final ConcreteService01 concreteService01;

	@Override
	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		context.setConcreteService01Visited(true);
	}
	
	@Inject
	public TestState1(ConcreteService01 concreteService01) {
		this.concreteService01 = concreteService01;
	}
}
