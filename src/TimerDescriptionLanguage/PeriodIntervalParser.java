package TimerDescriptionLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcochrane on 17/11/16.
 */
public class PeriodIntervalParser {

    public static List<PeriodInterval> parse(String text) throws InvalidSyntaxException {
        text = text.toLowerCase().trim();
        if (text.startsWith("for ")) {
            return parseFor(text);
        } else if (text.startsWith("between ")) {
            return parseBetween(text);
        } else if (text.startsWith("on ")) {
            return parseOn(text);
        }
        throw new InvalidSyntaxException(text, 0, "Cannot parse period interval");
    }

    private static List<PeriodInterval> parseFor(String text) throws InvalidSyntaxException {
        final String FOR_ = "for ";
        if (!text.startsWith(FOR_)) {
            throw new InvalidSyntaxException(text, 0, "Cannot parse 'for' period interval, does not start with 'for'");
        }
        text = text.substring(FOR_.length()); //remove the "for " from the start

        String lastWord;
        int number;
        if (text.contains(" ")) {
            lastWord = text.substring(text.lastIndexOf(' ') + 1);
            String numberStr = text.substring(0, text.lastIndexOf(' '));
            number = IntegerParser.parseAny(numberStr).value;
        } else {
            lastWord = text;
            number = 1;
        }

        return makeIntervalFromType(lastWord, 0, number, true);
    }

    private static List<PeriodInterval> parseBetween(String text) throws InvalidSyntaxException {
        final String BETWEEN_ = "between ";
        if (!text.startsWith(BETWEEN_)) {
            throw new InvalidSyntaxException(text, 0, "Cannot parse 'between' period interval, does not start with 'between'");
        }
        text = text.substring(BETWEEN_.length()); //remove the "between " from the start

        List<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(text.split(" ")));
        String periodType;
        boolean periodTypeMustBePlural = false;

        if (isValidPeriodType(words.get(0))) {
            //eg. between second ten and twenty
            periodType = words.get(0);
            words.remove(0);
        } else if(isValidPeriodType(words.get(words.size()-1))) {
            //eg. between ten and twenty seconds
            //should be a plural
            periodType = words.get(words.size()-1);
            periodTypeMustBePlural = true;
            words.remove(words.size()-1);
        } else {
            //TODO: is it implicit? i.e. 'between March and June', 'between the 1st and the 8th'?
            throw new InvalidSyntaxException(text, 0, "period type not found");
        }

        String fromStr = null;
        String toStr = null;

        //get fromStr and toStr
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).trim().equals("and")) {
                fromStr = String.join(" ", words.subList(0, i));
                toStr = String.join(" ", words.subList(i+1, words.size()));
                break;
            }
        }

        //if we didn't find an 'and'
        if (fromStr == null) {
            throw new InvalidSyntaxException(text, 0, "Between statement had no 'and'.  Between what and what?");
        }

        //actually do the parsing
        IntegerParser.Result fromResult = IntegerParser.parseAny(fromStr);
        IntegerParser.Result toResult = IntegerParser.parseAny(toStr);

        if (fromResult.type == IntegerParser.ResultType.DefiniteOrdinal) {
            periodTypeMustBePlural = false;
        }

        if (toResult.type == IntegerParser.ResultType.DefiniteOrdinal &&
                fromResult.type == IntegerParser.ResultType.Ordinal) {
            throw new InvalidSyntaxException(text, 0, "Period interval doesn't make sense.  Don't use a 'the' on the second value if you didn't use one on the first");
        }

        if (periodTypeMustBePlural && !periodType.endsWith("s")) {
            throw new InvalidSyntaxException(text, 0, periodType + " should be a plural");
        }
        return makeIntervalFromType(periodType, fromResult.value, toResult.value, false);
    }

    private static List<PeriodInterval> parseOn(String text) throws InvalidSyntaxException {
        final String ON_ = "on ";
        if (!text.startsWith(ON_)) {
            throw new InvalidSyntaxException(text, 0, "Cannot parse 'on' period interval, does not start with 'on'");
        }
        text = text.substring(ON_.length()); //remove the "on " from the start

        List<String> words = new ArrayList<>();
        words.addAll(Arrays.asList(text.split(" ")));
        String periodType;

        if (isValidPeriodType(words.get(0))) {
            //eg. on hours 1, 5, 8
            periodType = words.get(0);
            words.remove(0);
        } else if (isValidPeriodType(words.get(words.size()-1))) {
            //eg. on 1, 2, 3 minutes
            periodType = words.get(words.size() - 1);
            words.remove(words.size() - 1);
        } else if (words.get(words.size() - 1).equals("of") &&
                isValidPeriodType(words.get(words.size() - 2))) {
            //eg. on the 1st, 8th, 10th day of
            periodType = words.get(words.size() - 2);
            words.remove(words.size() - 1);
            words.remove(words.size() - 1);
        } else {
            //TODO: is it implicit? i.e. 'between March and June', 'between the 1st and the 8th'?
            throw new InvalidSyntaxException(text, 0, "period type not found");
        }

        List<IntegerParser.Result> values = new ArrayList<>();
        int start_index = 0;

        for (int i = 0; i < words.size(); i++) {
            String w = words.get(i).trim();
            if (w.trim().endsWith(",")) {
                String numberString = String.join(" ", words.subList(start_index, i+1));
                numberString = numberString.substring(0, numberString.length()-1); //remove ',' from end
                values.add(IntegerParser.parseAny(numberString));
                start_index = i+1;
            }
        }
        String numberString = String.join(" ", words.subList(start_index, words.size()));
        values.add(IntegerParser.parseAny(numberString));

        int[] intValues = new int[values.size()];

        for (int i = 0; i < values.size(); i++) {
            intValues[i] = values.get(i).value;
        }

        return onType(periodType, intValues);
    }

    private static List<PeriodInterval> onType(String type, int ... values) throws InvalidSyntaxException {
        //checks, what if type is "" or null?
        //boolean isPlural = false;
        if (type.endsWith("s")) { //all terms only end in 's' if they are a plural
            //isPlural = true;
            type = type.substring(0, type.length()-1);
        }
        switch (type) {
            case "second":
                return TimeHelper.onSeconds(values);
            case "minute":
                return TimeHelper.onMinutes(values);
            case "hour":
                return TimeHelper.onHours(values);
            case "day":
                return TimeHelper.onDays(values);
            case "week":
                //return TimeHelper.onWeeks(values);
                throw new InvalidSyntaxException(type, 0, "Weeks not supported in on statement");
            case "month":
                return TimeHelper.onMonths(values);
//            case "year":
//                return TimeHelper.betweenYears(start, end);
            default:
                //throw an error!? want detailed description of what went wrong so the user can correct
                //make this non-static and have the original input string saved, then we can return an
                //exception that states where in the input the error was and why it's bad.
                throw new InvalidSyntaxException(type, 0, "Unknown interval type");
        }
    }

    private static List<PeriodInterval> makeIntervalFromType(String type, int start, int end, boolean allZeroIndexed) throws InvalidSyntaxException {
        //checks, what if type is "" or null?
        //boolean isPlural = false;
        if (type.endsWith("s")) { //all terms only end in 's' if they are a plural
            //isPlural = true;
            type = type.substring(0, type.length()-1);
        }
        int allZeroIndexedOffset = allZeroIndexed?1:0;
        switch (type) {
            case "second":
                return TimeHelper.betweenSeconds(start, end);
            case "minute":
                return TimeHelper.betweenMinutes(start, end);
            case "hour":
                return TimeHelper.betweenHours(start, end);
            case "day":
                return TimeHelper.betweenDays(start+allZeroIndexedOffset, end+allZeroIndexedOffset);
            case "week":
                return TimeHelper.betweenWeeks(start+allZeroIndexedOffset, end+allZeroIndexedOffset);
            case "month":
                return TimeHelper.betweenMonths(start+allZeroIndexedOffset, end+allZeroIndexedOffset);
//            case "year":
//                return TimeHelper.betweenYears(start, end);
            default:
                //throw an error!? want detailed description of what went wrong so the user can correct
                //make this non-static and have the original input string saved, then we can return an
                //exception that states where in the input the error was and why it's bad.
                throw new InvalidSyntaxException(type, 0, "Unknown interval type");
        }
    }

    public static final String[] PERIOD_TYPES = {"second", "minute", "hour", "day", "week", "month", "year"};

    private static boolean isValidPeriodType(String type) {
        type = type.trim().toLowerCase();
        if (type.endsWith("s")) { //all terms only end in 's' if they are a plural
            //isPlural = true;
            type = type.substring(0, type.length() - 1);
        }
        for (String pt : PERIOD_TYPES) {
            if (type.equals(pt)) return true;
        }
        return false;
    }
}
