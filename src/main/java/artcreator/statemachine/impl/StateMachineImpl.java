package artcreator.statemachine.impl;

import artcreator.statemachine.port.Observer;
import artcreator.statemachine.port.State;
import artcreator.statemachine.port.State.S;

import java.util.ArrayList;
import java.util.List;

public class StateMachineImpl {
    private final List<Observer> observers = new ArrayList<>();

    private State currentState = S.INITIAL_STATE;

    public void attach(Observer obs) {
        this.observers.add(obs);
        obs.update(this.currentState);
    }

    public void detach(Observer obs) {
        if (this.observers.remove(obs)) obs.update(this.currentState);
    }

    public State getState() {
        return this.currentState;
    }

    public void setState(State state) {
        if (state == null) return;
        this.currentState = state;
        this.observers.forEach(obs -> obs.update(this.currentState));
    }
}
