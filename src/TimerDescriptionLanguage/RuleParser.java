package TimerDescriptionLanguage;

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

        text = text.trim();

        if (text.contains(":")) {
            int ind = text.indexOf(":");
            name = text.substring(0, ind);
            text = text.substring(ind+1);
        }

        text = normalizeSpace(text.toLowerCase());

        //First 3 words makes up the Action (eg. "Enable channel 1")
        List<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(text.split(SPACE)));

        Integer indexOfEvery = findIndexOfWordInList("every", words);
        if (indexOfEvery == null) throw new InvalidSyntaxException(text, 0, "\"every\" keywordnot found ");
        String actionPart = String.join(" ", words.subList(0, 3));
        String intervalPart = String.join(" ", words.subList(3, indexOfEvery));
        String periodPart = String.join(" ", words.subList(indexOfEvery + 1, words.size())); //don't include the 'every'

        Action action = ActionParser.parse(actionPart);
        List<PeriodInterval> intervals = PeriodIntervalParser.parse(intervalPart);
        Period period = PeriodParser.parse(periodPart);

        if (name == null) name = Rules.getInstance().getUniqueName();

        return new Rule(name, action, intervals, period, false);
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
