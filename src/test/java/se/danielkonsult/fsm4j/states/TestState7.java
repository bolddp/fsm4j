package se.danielkonsult.fsm4j.states;

import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j.TestContext;
import se.danielkonsult.fsm4j.TestTrigger;

/**
 * Special test state that triggers inside its entering() method, which is a
 * perfectly valid use case.
 */
public class TestState7 extends BaseState {
	@Override
	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		// Immediately finished
		stateMachine.trigger(TestTrigger.STATE7_SUCCESS);
	}
}
