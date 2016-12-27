package TimerDescriptionLanguage;

import org.joda.time.DurationFieldType;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by mcochrane on 14/11/16.
 *
 * Parses Periods.  For example:
 * (every) month
 * (every) second month
 * (every) third month
 * (every) fifth week
 * (every) 8th month
 * (every) 2nd hour
 *
 */
public class PeriodParser {

    public enum PeriodPartTypes {
        YEAR("year"),
        MONTH("month"),
        WEEK("week"),
        DAY("day"),
        HOUR("hour"),
        MINUTE("minute"),
        SECOND("second")
        ;

        private final String text;

        private PeriodPartTypes(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static String[] getValues() {
            PeriodPartTypes[] periodPartTypes = values();
            String[] values = new String[periodPartTypes.length];

            for (int i = 0; i < periodPartTypes.length; i++) {
                values[i] = periodPartTypes[i].text;
            }

            return values;
        }
    }

    public static Period parse(String text) throws InvalidSyntaxException {
        text = text.toLowerCase().trim();

        String[] words = text.split("\\s+");

        List<List<String>> parts = splitIntoParts(words);
        //List<TimeHelper.TimeTypePair> timeTypePairs = new ArrayList<>();

        TimeHelper.TimeTypePair[] timeTypePairs = new TimeHelper.TimeTypePair[parts.size()];

        for (int i = 0; i < parts.size(); i++) {
            timeTypePairs[i] = parsePart(parts.get(i));
        }

        Period result = TimeHelper.makePeriodCustom(timeTypePairs);
        if (result == null) throw new InvalidSyntaxException(text, 0, "Invalid syntax for period.");
        return result;
    }

    private static TimeHelper.TimeTypePair parsePart(List<String> part) throws InvalidSyntaxException {
        if (part.size() == 0) throw new InvalidSyntaxException("", 0, "Invalid period type");
        String lastWord = part.get(part.size()-1);
        int count;
        if (part.size() == 1) {
            count = 1;
        } else {
            String number = String.join(" ", part.subList(0, part.size()-1));
            count = IntegerParser.parseAny(number).value;
        }

        lastWord = removeTrailingIfExists(lastWord, ",");
        DurationFieldType type = durationFieldTypeFromString(lastWord);
        return TimeHelper.makeTimeTypePair(count, type);
    }

    private static List<List<String>> splitIntoParts(String[] words)
            throws InvalidSyntaxException {
        List<List<String>> retList = new ArrayList<>();
        List<String> wordList = new ArrayList<>(Arrays.asList(words));
        String[] allPeriodPartTypes = PeriodPartTypes.getValues();

        if (words.length == 2 && words[0].equals("second")) {
            //special case - "every second year/month/day/etc" second is also
            //a period type so without this it messes up.
            retList.add(new ArrayList<>(wordList));
            return retList;
        }

        int indexScannedUpTo = 0;

        //Want to split after a period type keyword
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            if (equalsOneOf(word, allPeriodPartTypes)) {
                retList.add(new ArrayList<>(wordList.subList(indexScannedUpTo, i+1)));
                indexScannedUpTo = i+1;
            }
        }

        if (indexScannedUpTo != wordList.size() && indexScannedUpTo > 0) {
            throw new InvalidSyntaxException(String.join(" ", words),
                    String.join(" ", wordList.subList(0, indexScannedUpTo)).length(),
                    "Could not parse due to extra words at the end of the period.");
        }

        //if a list starts with 'and' then remove the 'and'
        for (List<String> l : retList) {
            if (l.get(0).equals("and")) l.remove(0);
        }
        //TODO: Throw an exception if we use and before the last item?

        return retList;
    }

    private static boolean equalsOneOf(String word, String[] these) {
        word = removeTrailingIfExists(word, ",");
        word = removeTrailingIfExists(word, "s");
        for (String t : these) {
            if (word.equals(t)) return true;
        }
        return false;
    }

    private static String removeTrailingIfExists(String fullString, String trailingSequenceToRemove) {
        if (fullString.endsWith(trailingSequenceToRemove)) fullString = fullString.substring(0, fullString.length()-trailingSequenceToRemove.length());
        return fullString;
    }

    private static DurationFieldType durationFieldTypeFromString(String type) throws InvalidSyntaxException {
        type = removeTrailingIfExists(type, "s");
        switch (type) {
            case "second":
                return DurationFieldType.seconds();
            case "minute":
                return DurationFieldType.minutes();
            case "hour":
                return DurationFieldType.hours();
            case "day":
                return DurationFieldType.days();
            case "week":
                return DurationFieldType.weeks();
            case "month":
                return DurationFieldType.months();
            case "year":
                return DurationFieldType.years();
            default:
                //throw an error!? want detailed description of what went wrong so the user can correct
                //make this non-static and have the original input string saved, then we can return an
                //exception that states where in the input the error was and why it's bad.
                throw new InvalidSyntaxException(type, 0, "Invalid period type");
        }
    }

    private static Period parsePeriodType(String type, int count) throws InvalidSyntaxException {
        //checks, what if type is "" or null?
        //boolean isPlural = false;
        if (type.endsWith("s")) { //all terms only end in 's' if they are a plural
            //isPlural = true;
            type = type.substring(0, type.length()-1);
        }
        switch (type) {
            case "second":
                return TimeHelper.makePeriodSeconds(count);
            case "minute":
                return TimeHelper.makePeriodMinutes(count);
            case "hour":
                return TimeHelper.makePeriodHours(count);
            case "day":
                return TimeHelper.makePeriodDays(count);
            case "week":
                return TimeHelper.makePeriodWeeks(count);
            case "month":
                return TimeHelper.makePeriodMonths(count);
            case "year":
                return TimeHelper.makePeriodYears(count);
            default:
                //throw an error!? want detailed description of what went wrong so the user can correct
                //make this non-static and have the original input string saved, then we can return an
                //exception that states where in the input the error was and why it's bad.
                throw new InvalidSyntaxException(type, 0, "Invalid period type");
        }
    }
}
