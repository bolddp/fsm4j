package fsm;

import java.util.function.Function;

/**
 * Handles the configuration of one trigger that is valid for a specific state,
 * e.g. the the state class that is the target of the transition, as well as 
 * any guard condition that must evaluate to true for the transition to take place.
 *
 * @param <TriggerType> The trigger type of the configuration (set by the owning state configuration)
 * @param <ContextType> The context type of the configuration (set by the owning state configuration)
 */
public class TriggerConfiguration<TriggerType, ContextType> {

    private StateConfiguration<TriggerType, ContextType> parentStateConfiguration;
    private StateConfiguration<TriggerType, ContextType> targetStateConfiguration;
    private Function<ContextType, Boolean> guard;

    public TriggerConfiguration(final StateConfiguration<TriggerType, ContextType> parentStateConfiguration) {
        this.parentStateConfiguration = parentStateConfiguration;
    }

    public TriggerConfiguration(final StateConfiguration<TriggerType, ContextType> parentStateConfiguration,
            final Function<ContextType, Boolean> guard) {
        this(parentStateConfiguration);
        this.guard = guard;
    }

    public StateConfiguration<TriggerType, ContextType> goesTo(final Class<? extends FsmState<TriggerType, ContextType>> targetState) {
        targetStateConfiguration = parentStateConfiguration.getStateMachine().state(targetState);
        return parentStateConfiguration;
    }

    public StateConfiguration<TriggerType, ContextType> getTargetStateConfiguration() {
        return targetStateConfiguration;
    }

    public Function<ContextType, Boolean> getGuard() {
        return guard;
    }
}
