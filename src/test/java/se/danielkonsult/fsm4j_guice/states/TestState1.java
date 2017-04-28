package se.danielkonsult.fsm4j_guice.states;

import com.google.inject.Inject;

import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j_guice.TestContext;
import se.danielkonsult.fsm4j_guice.TestTrigger;
import se.danielkonsult.fsm4j_guice.services.ConcreteService01;

public class TestState1 extends BaseState {
	
	private final ConcreteService01 concreteService01;

	@Override
	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		if (concreteService01.getMessage() != "") {
			// Using the injected service
		}
		context.setConcreteService01Visited(true);
	}
	
	@Inject
	public TestState1(ConcreteService01 concreteService01) {
		this.concreteService01 = concreteService01;
	}
}
