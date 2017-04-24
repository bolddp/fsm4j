package se.danielkonsult.fsm4j_guice;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import se.danielkonsult.fsm4j.FsmState;
import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j.StateResolver;
import se.danielkonsult.fsm4j_guice.services.Service02;
import se.danielkonsult.fsm4j_guice.services.Service02Impl02;
import se.danielkonsult.fsm4j_guice.states.TestState1;
import se.danielkonsult.fsm4j_guice.states.TestState2;
import se.danielkonsult.fsm4j_guice.states.TestState3;

/**
 * Tests that incorporate Guice dependency injection into the state
 * instantiation, making it possible to inject dependencies through
 * the constructor of the states. 
 */
public class GuiceStateMachineTest {
	
	private static Injector injector = Guice.createInjector(new AbstractModule() {
		@Override
		protected void configure() {
			bind(Service02.class).to(Service02Impl02.class);
		}
	});
	
	@Test
	public void shouldHandleStatesWithDependencies() {
		TestContext testContext = new TestContext();
		StateMachine<TestTrigger, TestContext> sm = new StateMachine<>(testContext);
		sm.setStateResolver(new StateResolver<TestTrigger, TestContext>() {
			@Override
			public FsmState<TestTrigger, TestContext> resolve(Class<? extends FsmState<TestTrigger, TestContext>> clss) {
				return injector.getInstance(clss);
			}
		});
		
		sm.state(TestState1.class).isInitialState()
			.on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class);
		sm.state(TestState2.class)
			.on(TestTrigger.STATE2_SUCCESS).goesTo(TestState3.class);
		
		sm.start();
		sm.trigger(TestTrigger.STATE1_SUCCESS);
		sm.trigger(TestTrigger.STATE2_SUCCESS);
		
		// Make sure that the correct services have left their mark in the context!
		Assert.assertEquals(true, testContext.isConcreteService01Visited());
		Assert.assertEquals("This is Impl02!", testContext.getService02Message());
	}
}
