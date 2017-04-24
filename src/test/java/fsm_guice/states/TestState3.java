package fsm_guice.states;

import com.google.inject.Inject;

import fsm.StateMachine;
import fsm_guice.TestContext;
import fsm_guice.TestTrigger;
import fsm_guice.services.Service02;

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
