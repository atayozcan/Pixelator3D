package artcreator.statemachine.port;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface State {
    boolean isSubStateOf(State state);

    boolean isSuperStateOf(State state);

    enum S implements State {
        PIXELATED, IMAGE_LOADED(PIXELATED), HOME(IMAGE_LOADED);
        public static final S INITIAL_STATE = HOME;

        private final List<State> subStates;

        S(State... subS) {
            this.subStates = new ArrayList<>(Arrays.asList(subS));
        }

        @Override
        public boolean isSuperStateOf(State s) {
            boolean result = (s == null) || (this == s); // self-contained
            for (State state : this.subStates) // or
                result |= state.isSuperStateOf(s); // contained in a substate!
            return result;
        }

        @Override
        public boolean isSubStateOf(State state) {
            return (state != null) && state.isSuperStateOf(this);
        }
    }
}
