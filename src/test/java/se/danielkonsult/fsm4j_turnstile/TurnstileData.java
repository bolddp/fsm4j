package se.danielkonsult.fsm4j_turnstile;

/**
 * Acts as context for the turnstile state machine, keeping tracking of
 * how many times it has been opened, and how many forced attempts
 * (trying to pass without paying) that have been made.
 */
public class TurnstileData {

	private int passages = 0;
	private int forcedAttempts = 0;
	
	public void addPassage() {
		passages++;
	}

	public void addForcedAttempt() {
		forcedAttempts++;
	}
	
	public int getPassages() {
		return passages;
	}

	public int getForcedAttempts() {
		return forcedAttempts;
	}
}
