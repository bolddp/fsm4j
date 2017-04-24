package fsm;

import java.util.function.Function;

public class TriggerConfiguration<TriggerType, ContextType> {

    private StateConfiguration<TriggerType, ContextType> parentStateConfiguration;
    private StateConfiguration<TriggerType, ContextType> targetStateConfiguration;
    private Function<ContextType, Boolean> guard;

    public TriggerConfiguration(final StateConfiguration<TriggerType, ContextType> stateConfiguration) {
        parentStateConfiguration = stateConfiguration;
    }

    public TriggerConfiguration(final StateConfiguration<TriggerType, ContextType> stateConfiguration,
            final Function<ContextType, Boolean> guard) {
        this(stateConfiguration);
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
