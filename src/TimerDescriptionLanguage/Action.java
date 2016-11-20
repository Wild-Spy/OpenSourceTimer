package TimerDescriptionLanguage;

import org.joda.time.DateTime;

/**
 * Created by mcochrane on 13/11/16.
 */
public class Action {
    private Activator activator;
    private ActivatorState activatorStateWhenRunning;
    private RuleState state;

    Action(ActivatorState activatorStateWhenRunning, Activator activator) {
        this.activatorStateWhenRunning = activatorStateWhenRunning;
        this.activator = activator;
        state = RuleState.INACTIVE;
    }

    public void start(DateTime now) {
        if (this.state == RuleState.ACTIVE) return;
        this.state = RuleState.ACTIVE;
        if (activatorStateWhenRunning == ActivatorState.ENABLED) {
            activator.enable(now);
        } else {
            activator.disable();
        }
    }

    public void stop(DateTime now) {
        if (this.state == RuleState.INACTIVE) return;
        this.state = RuleState.INACTIVE;
        if (activatorStateWhenRunning == ActivatorState.ENABLED) {
            activator.disable();
        } else {
            activator.enable(now);
        }
    }

    public void toggle(DateTime now) {
        if (this.state == RuleState.ACTIVE) {
            this.stop(now);
        } else {
            this.start(now);
        }
    }

    public RuleState getState() {
        return this.state;
    }

    public ActivatorState getActivatorState() {
        return activator.getState();
    }

    //Package private (for unit testing)
    Activator getActivator() {
        return activator;
    }
    ActivatorState getActivatorStateWhenRunning() {
        return activatorStateWhenRunning;
    }
}
