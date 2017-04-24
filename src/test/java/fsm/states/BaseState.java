package fsm.states;

import fsm.FsmState;
import fsm.StateMachine;
import fsm.TestContext;
import fsm.TestTrigger;

public abstract class BaseState implements FsmState<TestTrigger, TestContext> {

    private boolean isCurrent;
    private StateMachine<TestTrigger, TestContext> stateMachine;
	private TestContext context;

    protected void doEntering(final StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
        // No behavior here
    }

    protected void doExiting() {
        // No behavior here
    }

    protected void fireTrigger(final TestTrigger trigger) {
        if (stateMachine != null) {
            stateMachine.trigger(trigger);
        }
    }

    @Override
    public final void entering(final StateMachine<TestTrigger, TestContext> stateMachine, TestContext context) {
        isCurrent = true;
        this.stateMachine = stateMachine;
        this.context = this.stateMachine.getContext();
        
        // For testing purposes, let's log all activity in the context
        if (this.context != null) {
        	this.context.addLog(String.format("Entering %s", this.getClass().getName()));
        }
        
        doEntering(this.stateMachine, this.context);
    }

    @Override
    public final void exiting() {
        try {
            // For testing purposes, let's log all activity in the context
            if (this.context != null) {
            	this.context.addLog(String.format("Exiting %s", this.getClass().getName()));
            }
        	
            doExiting();
        } finally {
            isCurrent = false;
        }
    }

    @Override
    public boolean getIsCurrent() {
        return isCurrent;
    }
}
