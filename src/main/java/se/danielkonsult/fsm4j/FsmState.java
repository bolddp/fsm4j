package se.danielkonsult.fsm4j;

public interface FsmState<TriggerType, ContextType> {
    void entering(StateMachine<TriggerType, ContextType> stateMachine, ContextType context);

    void exiting();
}
