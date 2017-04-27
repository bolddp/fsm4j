package se.danielkonsult.fsm4j;

public interface StateMachineListener<TriggerType, ContextType> {
	/**
	 * Called when the state machine has transitioned between states.
	 * @param sourceState The state class that the transition originated in.
	 * This value is null when the state machine starts.
	 * @param targetState The state class that the transition targets.
	 * This value is null when the state machine stops.
	 */
	default void onTransitioning(Class<? extends FsmState<TriggerType, ContextType>> sourceState,
			Class<? extends FsmState<TriggerType, ContextType>> targetState) {
		// No default behavior
	}

	/**
	 * Called when an invalid trigger is fired, e.g. a trigger that the current state
	 * wasn't expecting.
	 * @param trigger The trigger that was fired
	 * @param stateClass The state that don't know how to handle the trigger
	 */
	default void onInvalidTrigger(TriggerType trigger, Class<? extends FsmState<TriggerType, ContextType>> stateClass) {
		// If not overridden by implementing class, throw an exception to raise awareness
		throw new FsmException(String.format("Trigger %s is not valid for state %s", trigger, stateClass.getSimpleName()));
	}
}
