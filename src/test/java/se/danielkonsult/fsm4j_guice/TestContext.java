package se.danielkonsult.fsm4j_guice;

public class TestContext {

	private boolean concreteService01Visited;
	private String service02Message;

	public boolean isConcreteService01Visited() {
		return concreteService01Visited;
	}

	public void setConcreteService01Visited(boolean concreteService01Visited) {
		this.concreteService01Visited = concreteService01Visited;
	}

	public String getService02Message() {
		return service02Message;
	}

	public void setService02Message(String message) {
		this.service02Message = message;
	}
}
