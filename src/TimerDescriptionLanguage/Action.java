package TimerDescriptionLanguage;

import min.SerialHandler;
import org.joda.time.DateTime;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.List;

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

    public static int ACTION_TARGET_CHANNEL = 0;
    public static int ACTION_TARGET_RULE = 1;

    //For compiling
    private int getActionTargetType() {
        if (activator instanceof RuleActivator) {
            return ACTION_TARGET_RULE;
        } else if (activator instanceof ChannelActivator) {
            return ACTION_TARGET_CHANNEL;
        } else {
            return -1;
        }
    }

    private UByte compileDefaultState() {
        if (activator.getDefaultState().equals(ActivatorState.DISABLED)) {
            return UByte.valueOf(0);
        } else if (activator.getDefaultState().equals(ActivatorState.ENABLED)) {
            return UByte.valueOf(1);
        } else {
            return UByte.valueOf(255);
        }
    }

    List<UByte> compile() {
        List <UByte> compiledAction = new ArrayList<>();
        int targetType = getActionTargetType();
        if (targetType == -1) throw new Error("Invalid Target Type!");

        compiledAction.add(UByte.valueOf(targetType));
        compiledAction.addAll(SerialHandler.min_encode_16((short)activator.getTargetId()));
        compiledAction.add(compileDefaultState());

        return compiledAction;
    }

    //Package private (for unit testing)
    Activator getActivator() {
        return activator;
    }
    ActivatorState getActivatorStateWhenRunning() {
        return activatorStateWhenRunning;
    }
}
