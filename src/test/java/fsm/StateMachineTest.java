package fsm;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import fsm.states.TestState1;
import fsm.states.TestState2;
import fsm.states.TestState3;
import fsm.states.TestState4;
import fsm.states.TestState5;
import fsm.states.TestState6;
import fsm.states.TestState7;
import fsm.states.TestState8;

public class StateMachineTest {

    @Test
    public void shouldThrowOnAddingGuardedAfterUnguarded() {
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>();
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class);
        try {
            sm.state(TestState1.class)
            	.on(TestTrigger.STATE1_SUCCESS, ctx -> { return false; }).goesTo(TestState2.class);
            Assert.fail("Could add guarded after unguarded");
        } catch (FsmException ex) {
        	// This is expected
        }
    }
    
    @Test
    public void shouldThrowOnAddingUnguardedAfterGuarded() {
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>();
        sm.state(TestState1.class).isInitialState()
        	.on(TestTrigger.STATE1_SUCCESS, ctx -> { return false; }).goesTo(TestState2.class);
        try {
            sm.state(TestState1.class)
            	.on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class);
            Assert.fail("Could add unguarded after guarded");
        } catch (FsmException ex) {
        	// This is expected
        }
    }
    
    @Test
    public void shouldThrowOnStartWithoutInitialState() {
        try {
            final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>();
            sm.setContext(new TestContext());
            
            sm.state(TestState1.class)
            	.on(TestTrigger.STATE1_SUCCESS, ctx -> { return false; }).goesTo(TestState2.class);
            sm.start();
            Assert.fail("Could start without initial state");
        } catch (FsmException ex) {
        	// This is expected
        }
    }
    
    @Test
    public void shouldGoCorrectPathWithoutGuards() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        sm.state(TestState2.class)
                .on(TestTrigger.STATE2_SUCCESS).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_FAIL).goesTo(TestState4.class);
        // No config for TestState3 or TestState4, we should just end up in one of them
        
        sm.start();

        Assert.assertEquals(sm.getCurrentState().getClass(), TestState1.class);
        sm.trigger(TestTrigger.STATE1_SUCCESS);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState2.class);
        sm.trigger(TestTrigger.STATE2_FAIL);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState4.class);
    }
    
    @Test
    public void shouldGoCorrectGuardedPath() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        sm.state(TestState2.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return !ctx.getTestSwitch();
                }).goesTo(TestState4.class);
        // No config for TestState3 or TestState4, we should just end up in one of them
        
        sm.start();
        
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState1.class);
        sm.trigger(TestTrigger.STATE1_SUCCESS);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState2.class);

        // Leave test switch in context to false, so we should end up in test state 4
        
        sm.trigger(TestTrigger.STATE2_SUCCESS);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState4.class);
    }

    @Test
    public void shouldGoCorrectGuardedPath2() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        sm.state(TestState2.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return !ctx.getTestSwitch();
                }).goesTo(TestState4.class);
        // No config for TestState3 or TestState4, we should just end up in one of them
        
        sm.start();
        
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState1.class);
        sm.trigger(TestTrigger.STATE1_SUCCESS);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState2.class);

        // Set test switch to true, which should make us end up in another state
        // than the previous test
        testContext.setTestSwitch(true);
        
        sm.trigger(TestTrigger.STATE2_SUCCESS);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState3.class);
    }

    /**
     * Tests that any exception thrown in the guard of a trigger is handled
     * correctly, e.g. rethrown with the initial exception set as cause.
     */
    @Test
    public void shouldHandleExceptionThrownInGuard() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState2.class).isInitialState()
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getErrorThrowingValue();
                }).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return !ctx.getErrorThrowingValue();
                }).goesTo(TestState4.class);
        sm.start();

        try {
            sm.trigger(TestTrigger.STATE2_SUCCESS);
            Assert.fail("Error thrown in guard wasn't caught");
        } catch(FsmException ex) {
        	Assert.assertTrue(ex.getCause() instanceof ArithmeticException);
        }
    }
    
    /**
     * Tests that an exception is thrown if MULTIPLE guarded transitions have
     * guards that evaluate to true.
     */
    @Test
    public void shouldHandleMultipleGuardsThatEvaluateToTrue() {
    	// Setup state machine and context
        final TestContext testContext = new TestContext();
        testContext.setTestSwitch(true);
        
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState2.class).isInitialState()
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState4.class);
        sm.start();

        try {
            sm.trigger(TestTrigger.STATE2_SUCCESS);
            Assert.fail("Case of multiple true guards wasn't handled correctly");
        } catch(FsmException ex) {
        	// Expected
        }
    }
    
    /**
     * Tests that an exception is thrown if NO guarded transitions have
     * guards that evaluate to true.
     */
    @Test
    public void shouldHandleNoGuardsThatEvaluateToTrue() {
    	// Setup state machine and context
        final TestContext testContext = new TestContext();
        
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState2.class).isInitialState()
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState3.class)
                .on(TestTrigger.STATE2_SUCCESS, ctx -> {
                	return ctx.getTestSwitch();
                }).goesTo(TestState4.class);
        sm.start();

        try {
            sm.trigger(TestTrigger.STATE2_SUCCESS);
            Assert.fail("Case of no true guards wasn't handled correctly");
        } catch(FsmException ex) {
        	// Expected
        }
    }

    /**
     * Tests that the default InvalidTriggerHandler throws an exception if no
     * matching trigger is found for the current state.
     */
    @Test
    public void shouldThrowOnNoMatchingTriggerAndDefaultHandler() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        
        sm.start();
        
        try {
        	sm.trigger(TestTrigger.STATE2_SUCCESS);
        	Assert.fail("Invalid trigger wasn't handled correctly");
        } catch (FsmException ex) {
        	// Expected
        }
    }
    
    /**
     * Tests that an invalid trigger can be ignored be nullifying the
     * invalid trigger handler.
     */
    @Test
    public void shouldCorrectlyHandleAbsentInvalidTriggerHandler() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.setInvalidTriggerHandler(null);
        
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        
        sm.start();

        Assert.assertEquals(sm.getCurrentState().getClass(), TestState1.class);
        sm.trigger(TestTrigger.STATE2_SUCCESS);
     	
     	// State should just remain the same as before
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState1.class);
    }
    
    /**
     * Tests that a state class without a parameterless constructor is correctly handled.
     * 
     * The default state resolver requires that all states have a parameterless constructor
     * to they can be instantiated through the Constructor.newInstance method. If this is not
     * the case, an exception should be thrown.
     */
    @Test
    public void shouldThrowOnNonParameterlessStateConstructor() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        sm.state(TestState1.class).isInitialState()
                .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState6.class)
                .on(TestTrigger.STATE1_FAIL).goesTo(TestState4.class);
        
        sm.start();
        
        try {
        	sm.trigger(TestTrigger.STATE1_SUCCESS);
        } catch (FsmException ex) {
        	Assert.assertTrue(ex.getCause() instanceof NoSuchMethodException);
        }
    }
    
    /**
     * Tests that the default state resolver can be replaced with a custom implementation,
     * e.g. to support dependency injection in state classes.  
     */
    @Test
    public void shouldHandleNonDefaultStateResolver() {
    	// Setup state machine
        final TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        
        // Setup a bogus state resolver that replaces TestState5 with an instance of TestState6
        sm.setStateResolver(new StateResolver<TestTrigger, TestContext>() {
			@Override
			public FsmState<TestTrigger, TestContext> resolve(Class<? extends FsmState<TestTrigger, TestContext>> clss) {
				if (clss == TestState5.class) {
					return new TestState6(false);
				} else {
			        Constructor<? extends FsmState<TestTrigger, TestContext>> ctor;
			        try {
			            ctor = clss.getConstructor();
			            return (FsmState<TestTrigger, TestContext>) ctor.newInstance();
			        } catch (final Exception e) {
			            throw new FsmException(String.format("Could not instantiate state class %s! No parameterless constructor?",
			                    clss.getName()), e);
			        }
				}
			}
		});
    	
        sm.state(TestState1.class).isInitialState()
	        .on(TestTrigger.STATE1_SUCCESS).goesTo(TestState2.class)
	        .on(TestTrigger.STATE1_FAIL).goesTo(TestState5.class);

        sm.start();
        
        sm.trigger(TestTrigger.STATE1_FAIL);
        Assert.assertEquals(sm.getCurrentState().getClass(), TestState6.class);
    }

    /**
     * Tests that the exiting() method of the previous state always 
     * is called before entering() the next one, and that exiting()
     * on the last state is called when the state machine is stopped.
     */
    @Test
    public void shouldKeepCorrectOrderOnEntersAndExits() {
    	TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        
        sm.state(TestState1.class).isInitialState()
        	.on(TestTrigger.STATE1_SUCCESS).goesTo(TestState7.class)
        	.on(TestTrigger.STATE1_FAIL).goesTo(TestState3.class);
        sm.state(TestState7.class)
        	.on(TestTrigger.STATE7_SUCCESS).goesTo(TestState8.class);
        sm.state(TestState8.class)
    		.on(TestTrigger.STATE8_SUCCESS).goesTo(TestState2.class);
        
        sm.start();
        sm.trigger(TestTrigger.STATE1_SUCCESS);
        sm.stop();
        
        String[] logs = testContext.getLogs();
        
        Assert.assertEquals(8, logs.length);
        Assert.assertEquals("Entering fsm.states.TestState1", logs[0]);
        Assert.assertEquals("Exiting fsm.states.TestState1", logs[1]);
        Assert.assertEquals("Entering fsm.states.TestState7", logs[2]);
        Assert.assertEquals("Exiting fsm.states.TestState7", logs[3]);
        Assert.assertEquals("Entering fsm.states.TestState8", logs[4]);
        Assert.assertEquals("Exiting fsm.states.TestState8", logs[5]);
        Assert.assertEquals("Entering fsm.states.TestState2", logs[6]);
        Assert.assertEquals("Exiting fsm.states.TestState2", logs[7]);
    }
    
    /**
     * Tests that a trigger that points back to the current state is
     * handled correctly, e.g. the exiting() and the entering() method
     * of the same state are called.
     */
    @Test
    public void shouldHandleCircularTrigger() {
    	TestContext testContext = new TestContext();
        final StateMachine<TestTrigger, TestContext> sm = new StateMachine<TestTrigger, TestContext>(testContext);
        
        sm.state(TestState1.class).isInitialState()
        	.on(TestTrigger.STATE1_SUCCESS).goesTo(TestState1.class)
        	.on(TestTrigger.STATE1_FAIL).goesTo(TestState3.class);
        sm.state(TestState3.class)
        	.on(TestTrigger.STATE3_SUCCESS).goesTo(TestState4.class);
        
        sm.start();
        sm.trigger(TestTrigger.STATE1_SUCCESS);
        
        String[] logs = testContext.getLogs();
    	
        Assert.assertEquals(3, logs.length);
        Assert.assertEquals("Entering fsm.states.TestState1", logs[0]);
        Assert.assertEquals("Exiting fsm.states.TestState1", logs[1]);
        Assert.assertEquals("Entering fsm.states.TestState1", logs[2]);
    }
}
