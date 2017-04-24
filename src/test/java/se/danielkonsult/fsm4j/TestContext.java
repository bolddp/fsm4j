package se.danielkonsult.fsm4j;

import java.util.ArrayList;
import java.util.List;

public class TestContext {

    private boolean testSwitch;
    private List<String> logs = new ArrayList<>();

	public void addLog(String text) {
		logs.add(text);
	}

    public boolean getTestSwitch() {
        return testSwitch;
    }

    public String[] getLogs() {
    	return logs.toArray(new String[logs.size()]);
    }
    
    /**
     * Getter that will throw an error, used for testing how
     * exceptions that are thrown in a guard is handled.
     */
    public boolean getErrorThrowingValue() {
    	return ((1 / 0) == 2);
    }

    public void setTestSwitch(final boolean testSwitch) {
        this.testSwitch = testSwitch;
    }
}
