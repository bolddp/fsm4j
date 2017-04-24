package fsm.states;

/**
 * Test state that has no parameterless constructor, which should make
 * it impossible to use with the default state resolver. 
 */
public class TestState6 extends BaseState {

	private final boolean irrelevantValue;
	
	public TestState6(boolean irrelevantValue) {
		this.irrelevantValue = irrelevantValue;
	}

	public boolean isIrrelevantValue() {
		return irrelevantValue;
	}
}
