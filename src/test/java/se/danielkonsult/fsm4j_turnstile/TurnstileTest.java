package se.danielkonsult.fsm4j_turnstile;

import org.junit.Assert;
import org.junit.Test;

import se.danielkonsult.fsm4j.FsmState;
import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j.StateMachineListener;
import se.danielkonsult.fsm4j_turnstile.states.LockedState;
import se.danielkonsult.fsm4j_turnstile.states.OpenState;

public class TurnstileTest {

	@Test
	public void shouldActAsATurnstile() {
	
		// Create and configure turnstile FSM, including giving it a context
		TurnstileData context = new TurnstileData();
		StateMachine<TurnstileTrigger, TurnstileData> sm = new StateMachine<>(context);

		// The locked state will be the initial state and will only accept a coin
		sm.state(LockedState.class).isInitialState()
			.on(TurnstileTrigger.COIN).goesTo(OpenState.class);
		// The open state expects a push when the person enters, and should lock after that
		sm.state(OpenState.class)
			.on(TurnstileTrigger.PUSH).goesTo(LockedState.class);
		
		// We also want to detect attempts to illegally pass the turnstile, e.g. getting the
		// PUSH trigger in the LockedState. For this purpose, we setup a state machine listener.
		sm.setListener(new StateMachineListener<TurnstileTrigger, TurnstileData>() {
			@Override
			public void onInvalidTrigger(TurnstileData context, TurnstileTrigger trigger,
					Class<? extends FsmState<TurnstileTrigger, TurnstileData>> stateClass) {
				if ((trigger == TurnstileTrigger.PUSH) && (stateClass == LockedState.class)) {
					context.addForcedAttempt();
				}
			}
			
			@Override
			public void onTransitioning(TurnstileData context,
					Class<? extends FsmState<TurnstileTrigger, TurnstileData>> sourceState,
					Class<? extends FsmState<TurnstileTrigger, TurnstileData>> targetState) {
				// This a perfect place to log state changes for trouble shooting etc.
			}
		});
				
		// Let's start the the FSM and start manipulating it
		sm.start();
		
		// It should immediately enter the Locked state
		Assert.assertEquals(LockedState.class, sm.getCurrentState().getClass());
		
		// Let's input a valid trigger and check the result
		sm.trigger(TurnstileTrigger.COIN);
		Assert.assertEquals(OpenState.class, sm.getCurrentState().getClass());
		
		// Now there should be a passage
		Assert.assertEquals(1, context.getPassages());
		
		// Let's input another coin even if it is already open. This is an invalid trigger,
		// but since there's a listener attached that handles the onInvalidTrigger call,
		// nothing should happen
		sm.trigger(TurnstileTrigger.COIN);
		Assert.assertEquals(OpenState.class, sm.getCurrentState().getClass());
		Assert.assertEquals(1, context.getPassages());
		
		// Let's push through
		sm.trigger(TurnstileTrigger.PUSH);
		Assert.assertEquals(LockedState.class, sm.getCurrentState().getClass());
		
		// Now let's try to force our way in
		sm.trigger(TurnstileTrigger.PUSH);
		// Should still be locked and now with one forced attempt
		Assert.assertEquals(LockedState.class, sm.getCurrentState().getClass());
		Assert.assertEquals(1, context.getForcedAttempts());
		
		// One last passage
		sm.trigger(TurnstileTrigger.COIN);
		Assert.assertEquals(OpenState.class, sm.getCurrentState().getClass());
		Assert.assertEquals(2, context.getPassages());
	}
}
