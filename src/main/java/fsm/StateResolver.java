package fsm;

/**
 * Responsible for instantiating state instances for the FSM. This can for instance be used to
 * enable dependency injection into the state classes.
 */
public interface StateResolver<TriggerType, ContextType> {

    FsmState<TriggerType, ContextType> resolve(Class<? extends FsmState<TriggerType, ContextType>> clss);
}
