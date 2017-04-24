package se.danielkonsult.fsm4j;

/**
 * Determines behavior when an invalid trigger is used for a state.
 */
public interface InvalidTriggerHandler<TriggerType, ContextType> {
	
	void handle(TriggerType trigger, Class<? extends FsmState<TriggerType, ContextType>> clss);
}
