package fsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Handles the configuration of one state in the state machine, e.g. the
 * guarded and unguarded triggers that are valid for the specific state.
 * Only one state configuration can exist for a specific state in the
 * state machine. 
 * 
 * @param <TriggerType> The trigger type of the configuration (set by the owning state machine)
 * @param <ContextType> The context type of the configuration (set by the owning state machine)
 */
public class StateConfiguration<TriggerType, ContextType> {

    private final Class<? extends FsmState<TriggerType, ContextType>> stateClass;
    private final StateMachine<TriggerType, ContextType> stateMachine;
    private final HashMap<TriggerType, TriggerConfiguration<TriggerType, ContextType>> unguardedTriggerConfigurations = new HashMap<>();
    private final HashMap<TriggerType, List<TriggerConfiguration<TriggerType, ContextType>>> guardedTriggerConfigurations = new HashMap<>();

    public StateConfiguration(final StateMachine<TriggerType, ContextType> stateMachine,
            final Class<? extends FsmState<TriggerType, ContextType>> stateClass) {
        this.stateMachine = stateMachine;
        this.stateClass = stateClass;
    }

    public StateConfiguration<TriggerType, ContextType> isInitialState() {
        // This state should be the initial state
        stateMachine.setInitialStateConfiguration(this);
        return this;
    }

    /**
     * Creates, or returns an existing, unguarded trigger configuration on this state configuration.
     * The trigger configuration can then be fitted with a target state configuration through the
     * TriggerConfiguration.goesTo method.  
     * @param trigger The trigger that this configuration should apply to.
     * @return The created or existing trigger configuration.
     */
    public TriggerConfiguration<TriggerType, ContextType> on(final TriggerType trigger) {
        // The same trigger must not have been registered as guarded already
        if (guardedTriggerConfigurations.containsKey(trigger)) {
            throw new FsmException(
                    "Trigger %s has already been registered with guard, cannot add as unguarded as well");
        }

        if (unguardedTriggerConfigurations.containsKey(trigger)) {
            return unguardedTriggerConfigurations.get(trigger);
        } else {
            final TriggerConfiguration<TriggerType, ContextType> triggerConfiguration = new TriggerConfiguration<TriggerType, ContextType>(
                    this);
            unguardedTriggerConfigurations.put(trigger, triggerConfiguration);
            return triggerConfiguration;
        }
    }

    public TriggerConfiguration<TriggerType, ContextType> on(final TriggerType trigger, final Function<ContextType, Boolean> guard) {
        // The same trigger mustn't be registered as unguarded already
        if (unguardedTriggerConfigurations.containsKey(trigger)) {
            throw new FsmException(String.format("Trigger %s has already been registered without guard, cannot add as guarded as well", trigger));
        }

        // Make sure there is a list of guarded configurations for this trigger
        if (!guardedTriggerConfigurations.containsKey(trigger)) {
            guardedTriggerConfigurations.put(trigger, new ArrayList<TriggerConfiguration<TriggerType, ContextType>>());
        }

        final TriggerConfiguration<TriggerType, ContextType> triggerConfiguration = new TriggerConfiguration<TriggerType, ContextType>(this,
                guard);
        guardedTriggerConfigurations.get(trigger).add(triggerConfiguration);

        return triggerConfiguration;
    }

    public StateMachine<TriggerType, ContextType> getStateMachine() {
        return stateMachine;
    }

    public Class<? extends FsmState<TriggerType, ContextType>> getStateClass() {
        return stateClass;
    }

    public TriggerConfiguration<TriggerType, ContextType> getTriggerConfiguration(final TriggerType trigger) {
        if (unguardedTriggerConfigurations.containsKey(trigger)) {
            // It's unguarded, just return it
            return unguardedTriggerConfigurations.get(trigger);
        } else if (guardedTriggerConfigurations.containsKey(trigger)) {
            // It's guarded, which guard evaluates to true?
            TriggerConfiguration<TriggerType, ContextType> satisfied = null;
            final List<TriggerConfiguration<TriggerType, ContextType>> triggerConfigurations = guardedTriggerConfigurations
                    .get(trigger);
            for (final TriggerConfiguration<TriggerType, ContextType> tc : triggerConfigurations) {
                // Calculate guard result
                boolean guardResult = false;
                try {
                    guardResult = tc.getGuard().apply(stateMachine.getContext());
                } catch (final Exception e) {
                    throw new FsmException(
                            String.format("Could not evaluate guard on state %s", stateClass.getName()), e);
                }

                if (guardResult) {
                    // Only one guard must evaluate to true
                    if (satisfied != null) {
                        throw new FsmException(String.format("More than one guard evaluates to true on state %s",
                                stateClass.getName()));
                    }
                    satisfied = tc;
                }
            }

            if (satisfied == null) {
                throw new FsmException(
                        String.format("No guard evaluates to true on state %s", stateClass.getName()));
            }

            return satisfied;
        } else {
        	// This trigger is not valid for the current class, let the designated handler deal with it
        	if (stateMachine.getInvalidTriggerHandler() != null) {
            	stateMachine.getInvalidTriggerHandler().handle(trigger, stateClass);
        	}
        	return null;
        }
    }
}
