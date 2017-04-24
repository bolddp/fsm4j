package se.danielkonsult.fsm4j_guice.services;

import com.google.inject.Inject;

public class ConcreteService01 {

	private final SubService01 subService;
	
	@Inject
	public ConcreteService01(SubService01 subService) {
		this.subService = subService;
	}

	public String getMessage() {
		return String.format("This is Concrete01, relaying from sub service: %s", subService.getMessage());
	}
}
