package TimerDescriptionLanguage;

/**
 * Created by mcochrane on 18/11/16.
 */
public class ActionParser {

    public static Action parse(String text) throws InvalidSyntaxException {
        Activator activator = ActivatorParser.parse(text);
        ActivatorState activatorStateWhenRunning;
        if (activator.getDefaultState() == ActivatorState.ENABLED) {
            activatorStateWhenRunning = ActivatorState.DISABLED;
        } else if (activator.getDefaultState() == ActivatorState.DISABLED) {
            activatorStateWhenRunning = ActivatorState.ENABLED;
        } else {
            throw new InvalidSyntaxException(text, 0, "Invalid verb!?");
        }
        return new Action(activatorStateWhenRunning, activator);
    }
}
