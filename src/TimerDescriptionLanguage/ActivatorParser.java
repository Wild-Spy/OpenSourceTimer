package TimerDescriptionLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcochrane on 17/11/16.
 */
public class ActivatorParser {

    public static Activator parse(String text) throws InvalidSyntaxException {
        text = text.toLowerCase().trim();
        List<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(text.split(" ")));
        String verb = words.get(0).trim();
        ActivatorState defaultState;
        if (words.size() != 3) throw new InvalidSyntaxException(text, 0, "invalid activator, should be exactly 3 words (syntax is: [VERB] channel/rule [NAME])");
        if (verb.equals("enable")) {
            defaultState = ActivatorState.DISABLED;
        } else if (verb.equals("disable")) {
            defaultState = ActivatorState.ENABLED;
        } else {
            throw new InvalidSyntaxException(text, 0, "invalid verb " + verb);
        }

        //eg "disable rule myRule" - name is 3rd word
        String name = words.get(2);
        String activatorType = words.get(1);

        if (activatorType.equals("channel")) {
            return new ChannelActivator(Channels.getInstance().get(name), defaultState);
        } else if (activatorType.equals("rule")) {
            return new RuleActivator(Rules.getInstance().get(name), defaultState);
        } else {
            throw new InvalidSyntaxException(text, 0, "invalid activator type " + activatorType);
        }
    }
}
