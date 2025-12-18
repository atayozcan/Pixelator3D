package artcreator.statemachine.port;

@FunctionalInterface
public interface Observer {
	void update(State currentState);
}
