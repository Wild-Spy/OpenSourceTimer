package TimerDescriptionLanguage;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcochrane on 14/11/16.
 */
public class RuleParser {

    private static final String SPACE = " ";

    private RuleParser() {

    }

    public static List<Rule> parseMultiple(String text) throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        String[] ruleStrings = text.split("\n");
        List<Rule> rules = new ArrayList<>();
        for (String s : ruleStrings) {
            rules.add(parse(s));
        }
        return rules;
    }

    public static Rule parse(String text) throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        String name = null;
        boolean ruleIsEnabled = false;

        text = text.trim();

        name = getRuleName(text);
        ruleIsEnabled = ruleIsEnabled(name);

        text = removeRuleNameIfPresent(text);
        text = normalizeSpace(text.toLowerCase());

        //First 3 words makes up the Action (eg. "Enable channel 1")
        List<String> words = new ArrayList<>(Arrays.asList(text.split(SPACE)));

        Integer indexOfEvery = findIndexOfWordInList("every", words);
        Integer indexOfStartingAt = findIndexOfWordInList("starting", words);
        //if (indexOfEvery == null) throw new InvalidSyntaxException(text, 0, "\"every\" keywordnot found ");
        String actionPart = String.join(" ", words.subList(0, 3));

        Action action = ActionParser.parse(actionPart);

        String intervalPart;
        Period period;
        int periodEndWord;
        if (indexOfStartingAt != null) periodEndWord = indexOfStartingAt;
        else periodEndWord = words.size();
        if (indexOfEvery != null) {
            intervalPart = String.join(" ", words.subList(3, indexOfEvery));
            String periodPart = String.join(" ", words.subList(indexOfEvery + 1, periodEndWord)); //don't include the 'every'
            period = PeriodParser.parse(periodPart);
        } else {
            intervalPart = String.join(" ", words.subList(3, periodEndWord));
            period = TimeHelper.infinitePeriod();
        }

        DateTime startOfFirstPeriod = null;
        boolean hasEvent = false;
        String eventName = null;
        Period eventStartPeriod = null;
        if (indexOfStartingAt != null) {
            String startingAtPart;
            if (words.size() <= indexOfStartingAt+1)
                throw new InvalidSyntaxException(text, String.join(" ", words).length(), "Expected a 'starting at [date/time]', 'starting on event [event name]' or 'starting [time] after event [event name]");
            if (words.get(indexOfStartingAt+1).equals("at")) {
                startingAtPart = String.join(" ", words.subList(indexOfStartingAt + 2, words.size()));
                startOfFirstPeriod = DateTimeParser.parse(startingAtPart);
            } else {
                startingAtPart = String.join(" ", words.subList(indexOfStartingAt + 1, words.size()));
                eventName = getEventName(startingAtPart);
                eventStartPeriod = getEventStartPeriod(startingAtPart);
                if (eventName != null) hasEvent = true;
            }
        }

        List<PeriodInterval> intervals = PeriodIntervalParser.parse(intervalPart);

        if (name == null) name = Rules.getInstance().getUniqueName();

        if (hasEvent) {
            return new Rule(name, action, intervals, period, ruleIsEnabled, eventName, eventStartPeriod);
        } else if (startOfFirstPeriod == null) {
            return new Rule(name, action, intervals, period, ruleIsEnabled);
        } else {
            return new Rule(name, action, intervals, period, ruleIsEnabled, startOfFirstPeriod);
        }
    }

    private static Period getEventStartPeriod(String eventPart) throws InvalidSyntaxException {
        String[] parts = eventPart.split(" event ");
        if (parts.length == 2) {
            if (parts[0].trim().equals("on")) return null; //no period "starting on event event1"
            if (!parts[0].trim().endsWith("after"))
                throw new InvalidSyntaxException(eventPart, eventPart.length(), "Expected the word 'after' before event keyword, eg. 'starting 1 hour *after* event'.");
            parts[0] = parts[0].trim().substring(0, parts[0].trim().length()-6);
            return PeriodParser.parse(parts[0]);
        } else {
            throw new InvalidSyntaxException(eventPart, 0, "Cannot parse event start offset.");
        }
    }

    private static String getEventName(String eventPart) throws InvalidSyntaxException {
        String[] parts = eventPart.split(" event ");
        if (parts.length == 2) {
            return parts[1];
        } else {
            throw new InvalidSyntaxException(eventPart, 0, "Cannot parse event name.");
        }
    }

    private static boolean ruleNamePresent(String text) {
        return text.contains(":");
    }

    private static boolean ruleIsEnabled(String ruleName) {
        return startsWithCapital(ruleName);
    }

    private static String getRuleName(String text) {
        if (ruleNamePresent(text)) {
            int ind = text.indexOf(":");
            return text.substring(0, ind);
        } else {
            return Rules.getInstance().getUniqueName();
        }
    }

    private static String removeRuleNameIfPresent(String text) {
        if (ruleNamePresent(text)) {
            int ind = text.indexOf(":");
            return text.substring(ind+1);
        }
        return text;
    }

    private static boolean startsWithCapital(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    private static Integer findIndexOfWordInList(String word, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(word)) {
                return i;
            }
        }
        return null;
    }

    private static String normalizeSpace(String text) {
        return text.trim().replaceAll("\t", " ").replaceAll(" +", " ");
    }
}
