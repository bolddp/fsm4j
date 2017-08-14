package se.danielkonsult.fsm4j;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic FSM (Finite State Machine) implementation where all the states must implement a common, generic FsmState interface that
 * matches the state machine.
 *
 * The state machine behavior is based on a configuration, where each state has a set of valid triggers that controls which state
 * to enter next.
 *
 * Each state should act as a self-containing module of functionality, only interacting by accessing the state machine context and
 * by firing triggers.
 *
 * When entering a new state, a custom state resolver can be used to provide dependency injection functionality in the state
 * machine, enabling each state to request additional dependencies by constructor injection.
 *
 * @param <TriggerType> The type that should be used as the trigger of the state machine, e.g. an enum or an integer.
 * @param <ContextType> The type of the context that should be used. The context is shared by all states of the state machine, so
 * it's a natural place for sharing data and configuration that the states of the state machine need.
 */
public class StateMachine<TriggerType, ContextType> {

    private final HashMap<Class<? extends FsmState<TriggerType, ContextType>>, StateConfiguration<TriggerType, ContextType>> states = new HashMap<>();

    private StateConfiguration<TriggerType, ContextType> initialStateConfiguration;
    private StateConfiguration<TriggerType, ContextType> currentStateConfiguration;
    private FsmState<TriggerType, ContextType> currentState;
    private StateMachineListener<TriggerType, ContextType> listener;
    private ContextType context;

    // Setup a default state resolver
    private StateResolver<TriggerType, ContextType> stateResolver = clss -> {
        Constructor<? extends FsmState<TriggerType, ContextType>> ctor;
        try {
            ctor = clss.getConstructor();
            return (FsmState<TriggerType, ContextType>) ctor.newInstance();
        } catch (final Exception e) {
            throw new FsmException(String.format("Could not instantiate state class %s! No parameterless constructor?",
                    clss.getSimpleName()), e);
        }
    };

    private class TestEntry {

        private final StateConfiguration<TriggerType, ContextType> stateConfiguration;
        private int referenceCount;

        public TestEntry(final StateConfiguration<TriggerType, ContextType> stateConfiguration) {
            this.stateConfiguration = stateConfiguration;
        }
    }

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

    private void notifyOnTransitioning(final Class<? extends FsmState<TriggerType, ContextType>> sourceState,
            final Class<? extends FsmState<TriggerType, ContextType>> targetState) {
        if (listener != null) {
            listener.onTransitioning(context, sourceState, targetState);
        }
    }

    // Constructors

    public StateMachine() {
    }

    public StateMachine(final ContextType context) {
        this.context = context;
    }

    public StateConfiguration<TriggerType, ContextType> state(
            final Class<? extends FsmState<TriggerType, ContextType>> stateClass) {
        if (states.containsKey(stateClass)) {
            return states.get(stateClass);
        } else {
            final StateConfiguration<TriggerType, ContextType> stateConfiguration = new StateConfiguration<TriggerType, ContextType>(
                    this, stateClass);
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

        notifyOnTransitioning(null, currentStateConfiguration.getStateClass());
    }

    public void stop() {
        if (currentStateConfiguration != null) {
            notifyOnTransitioning(currentStateConfiguration.getStateClass(), null);
        }
        exitCurrentState();
    }

    /**
     * Verifies that all state classes that are configured can be resolved, and that no state classes except the initial state are
     * orphaned, e.g. no transitions point to them.
     */
    public void test() {
        // Create a list of test entries
        final Map<StateConfiguration<TriggerType, ContextType>, TestEntry> testEntries = new HashMap<>();
        for (final StateConfiguration<TriggerType, ContextType> stateConfiguration : states.values()) {
            testEntries.put(stateConfiguration, new TestEntry(stateConfiguration));
        }

        // Make sure that the initial state counts as one reference
        final TestEntry initialEntry = testEntries.get(initialStateConfiguration);
        if (initialEntry != null) {
            initialEntry.referenceCount = 1;
        }

        for (final TestEntry testEntry : testEntries.values()) {
            // Attempt to resolve it
            stateResolver.resolve(testEntry.stateConfiguration.getStateClass());

            // Loop all target states of this state configuration
            for (final StateConfiguration<TriggerType, ContextType> stateConfiguration : testEntry.stateConfiguration
                    .getTargetStateConfigurations()) {
                final TestEntry targetEntry = testEntries.get(stateConfiguration);
                if (targetEntry != null) {
                    targetEntry.referenceCount++;
                }
            }
        }
        // Is there any state that is orphaned?
        for (final TestEntry testEntry : testEntries.values()) {
            if (testEntry.referenceCount == 0) {
                throw new FsmException(String.format("State class '%s' isn't referenced by any triggers",
                        testEntry.stateConfiguration.getStateClass().getSimpleName()));
            }
        }

    }

    public void trigger(final TriggerType trigger) {
        // Get the correct trigger configuration from the current state
        final TriggerConfiguration<TriggerType, ContextType> triggerConfiguration = currentStateConfiguration
                .getTriggerConfiguration(trigger);

        if (triggerConfiguration == null) {
            // This can happen if the default invalidTriggerHandler has been
            // overridden
            // with a handler that doesn't throw an exception, e.g. to simply
            // ignore invalid triggers
            return;
        }

        // Exit the current state
        final Class<? extends FsmState<TriggerType, ContextType>> sourceState = currentStateConfiguration != null
                ? currentStateConfiguration.getStateClass() : null;

        exitCurrentState();

        // Determine next state
        currentStateConfiguration = triggerConfiguration.getTargetStateConfiguration();
        // Notify listener of the transition
        notifyOnTransitioning(sourceState, currentStateConfiguration.getStateClass());
        // Enter the next state
        enterCurrentState();
    }

    public FsmState<TriggerType, ContextType> getCurrentState() {
        return currentState;
    }

    public ContextType getContext() {
        return context;
    }

    public StateMachineListener<TriggerType, ContextType> getListener() {
        return listener;
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

    public void setListener(final StateMachineListener<TriggerType, ContextType> listener) {
        this.listener = listener;
    }
}
