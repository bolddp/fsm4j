package fsm;

public class FsmException extends RuntimeException {
    static final long serialVersionUID = -7034827348623434639L; // 100% made up on the fly

	public FsmException(String message) {
		super(message);
	}

	public FsmException(String message, Throwable cause) {
		super(message, cause);
	}
}
