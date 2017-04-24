package fsm.states;

import fsm.StateMachine;
import fsm.TestContext;
import fsm.TestTrigger;

/**
 * Special test state that triggers inside its entering() method, which is a
 * perfectly valid use case.
 */
public class TestState8 extends BaseState {
	@Override
	protected void doEntering(StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
		// Immediately finished
		stateMachine.trigger(TestTrigger.STATE8_SUCCESS);
	}
}
