package se.danielkonsult.fsm4j_guice.states;

import com.google.inject.Inject;

import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j_guice.TestContext;
import se.danielkonsult.fsm4j_guice.TestTrigger;
import se.danielkonsult.fsm4j_guice.services.Service02;

public class TestState3 extends BaseState {
	
	private final Service02 service02;

	@Override
	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		context.setService02Message(service02.getMessage());
	}
	
	@Inject
	public TestState3(Service02 service02) {
		this.service02 = service02;
	}
}
