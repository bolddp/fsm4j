package fsm;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Generic FSM (Finite State Machine) implementation where all the states must implement
 * a common, generic FsmState interface that matches the state machine.
 * 
 * The state machine behavior is based on a configuration, where each state has a set of valid
 * triggers that controls which state to enter next.
 * 
 * Each state should act as a self-containing module of functionality, only interacting by
 * accessing the state machine context and by firing triggers.
 * 
 * When entering a new state, a custom state resolver can be used to provide dependency injection
 * functionality in the state machine, enabling each state to request additional dependencies by
 * constructor injection.
 *
 * @param <TriggerType> The type that should be used as the trigger of the state machine, e.g.
 * an enum or an integer.
 * @param <ContextType> The type of the context that should be used. The context is shared by
 * all states of the state machine, so it's a natural place for sharing data and configuration
 * that the states of the state machine need.
 */
public class StateMachine<TriggerType, ContextType> {

    private final HashMap<Class<? extends FsmState<TriggerType, ContextType>>, StateConfiguration<TriggerType, ContextType>> states = new HashMap<>();
    
    private StateConfiguration<TriggerType, ContextType> initialStateConfiguration;
    private StateConfiguration<TriggerType, ContextType> currentStateConfiguration;
    private FsmState<TriggerType, ContextType> currentState;
    private ContextType context;

    // Setup a default state resolver
    private StateResolver<TriggerType, ContextType> stateResolver = clss -> {
        Constructor<? extends FsmState<TriggerType, ContextType>> ctor;
        try {
            ctor = currentStateConfiguration.getStateClass().getConstructor();
            return (FsmState<TriggerType, ContextType>) ctor.newInstance();
        } catch (final Exception e) {
            throw new FsmException(String.format("Could not instantiate state class %s! No parameterless constructor?",
                    currentStateConfiguration.getStateClass().getName()), e);
        }
    };

    // Setup a default invalid trigger handler
    private InvalidTriggerHandler<TriggerType, ContextType> invalidTriggerHandler = (trigger, clss) -> {
        throw new FsmException(
                String.format("Trigger %s is not valid for state %s", trigger, clss.getName()));
    };
    
    private void enterCurrentState() {
        currentState = stateResolver.resolve(currentStateConfiguration.getStateClass());
        currentState.entering(this, this.context);
    }

    private void exitCurrentState() {
        if (currentState != null) {
        	currentState.exiting();
        	currentState = null;
        }
    }
    
    public StateMachine() {
    }

    public StateMachine(final ContextType context) {
        this.context = context;
    }

    public StateConfiguration<TriggerType, ContextType> state(final Class<? extends FsmState<TriggerType, ContextType>> stateClass) {
        if (states.containsKey(stateClass)) {
            return states.get(stateClass);
        } else {
            final StateConfiguration<TriggerType, ContextType> stateConfiguration = new StateConfiguration<TriggerType, ContextType>(this,
                    stateClass);
            states.put(stateClass, stateConfiguration);
            return stateConfiguration;
        }
    }

    public void start() {
        if (initialStateConfiguration == null) {
        	throw new FsmException("Cannot start, no initial state set");
        }
        currentStateConfiguration = initialStateConfiguration;
        enterCurrentState();
    }

    public void stop() {
    	exitCurrentState();
    }
    
    public void trigger(final TriggerType trigger) {
        // Get the correct trigger configuration from the current state
        final TriggerConfiguration<TriggerType, ContextType> triggerConfiguration = currentStateConfiguration
                .getTriggerConfiguration(trigger);

        if (triggerConfiguration == null) {
        	// This can happen if the default invalidTriggerHandler has been overridden
        	// with a handler that doesn't throw an exception, e.g. to simply ignore invalid triggers
        	return;
        }

        // Exit the current state
        exitCurrentState();
        
        // Enter the next state
        currentStateConfiguration = triggerConfiguration.getTargetStateConfiguration();
        enterCurrentState();
    }

    public FsmState<TriggerType, ContextType> getCurrentState() {
        return currentState;
    }

    public ContextType getContext() {
        return context;
    }

    /*
     * Sets the handler to use when an invalid trigger is used for a state, e.g. a trigger
     * whose target state hasn't been defined.
     */
	public InvalidTriggerHandler<TriggerType, ContextType> getInvalidTriggerHandler() {
		return invalidTriggerHandler;
	}

    public void setInitialStateConfiguration(final StateConfiguration<TriggerType, ContextType> stateConfiguration) {
        this.initialStateConfiguration = stateConfiguration;
    }

    public void setStateResolver(final StateResolver<TriggerType, ContextType> stateResolver) {
        this.stateResolver = stateResolver;
    }

    public void setContext(final ContextType context) {
        this.context = context;
    }

	public void setInvalidTriggerHandler(InvalidTriggerHandler<TriggerType, ContextType> invalidTriggerHandler) {
		this.invalidTriggerHandler = invalidTriggerHandler;
	}
}
