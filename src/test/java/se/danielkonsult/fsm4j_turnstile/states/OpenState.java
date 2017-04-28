package se.danielkonsult.fsm4j_turnstile.states;

import se.danielkonsult.fsm4j.FsmState;
import se.danielkonsult.fsm4j.StateMachine;
import se.danielkonsult.fsm4j_turnstile.TurnstileData;
import se.danielkonsult.fsm4j_turnstile.TurnstileTrigger;

public class OpenState implements FsmState<TurnstileTrigger, TurnstileData> {

	@Override
	public void entering(StateMachine<TurnstileTrigger, TurnstileData> stateMachine, TurnstileData context) {
		// Increase the passage counter
		context.addPassage();
	}

	@Override
	public void exiting() {
		// TODO Auto-generated method stub
		
	}
}
