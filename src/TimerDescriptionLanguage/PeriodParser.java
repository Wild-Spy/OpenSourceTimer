package TimerDescriptionLanguage;

import org.joda.time.Period;

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

    public static Period parse(String text) throws InvalidSyntaxException {
        String lastWord;
        int count;

        text = text.toLowerCase().trim();

        if (text.contains(" ")) { //if there's more than one word
            lastWord = text.substring(text.lastIndexOf(' ') + 1);
            String number = text.substring(0, text.lastIndexOf(' '));
            count = IntegerParser.parseAny(number).value;
        } else {
            lastWord = text;
            count = 1;
        }

        return parsePeriodType(lastWord, count);
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
