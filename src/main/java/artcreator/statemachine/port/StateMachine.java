package artcreator.statemachine.port;

public interface StateMachine {
	State getState();
	void setState(State state);
}
