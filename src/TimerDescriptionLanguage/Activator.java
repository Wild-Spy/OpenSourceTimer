package TimerDescriptionLanguage;

import org.joda.time.DateTime;

/**
 * Created by mcochrane on 13/11/16.
 */
public class Activator {
    private ActivatorState defaultState;
    private ActivatorState currentState;

    Activator(ActivatorState defaultState) {
        this.defaultState = defaultState;
        this.currentState = this.defaultState;
    }

    public void enable(DateTime now) {
        currentState = ActivatorState.ENABLED;
    }

    public void disable() {
        currentState = ActivatorState.DISABLED;
    }

    public ActivatorState getDefaultState() {
        return this.defaultState;
    }

    public ActivatorState getState() {
        return this.currentState;
    }

    public boolean isInDefaultState() {
        return this.currentState == this.defaultState;
    }
}
