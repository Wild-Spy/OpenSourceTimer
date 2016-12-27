package TimerDescriptionLanguage;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeParserBucket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by mcochrane on 16/12/16.
 */
public class DateTimeParser {

    private DateTimeParser() {

    }

    public static DateTime parse(String text) throws InvalidSyntaxException {
        DateTime result;
        text = text.toLowerCase();

        String[] parts = splitIntoDateAndTime(text);
        String dateText = parts[0];
        String timeText = parts[1];

        if (isSimpleDateFormat(dateText)) {
            result = parseSimpleDate(dateText);
        } else {
            result = parseWordedDate(dateText);
        }

        if (!timeText.equals("")) {
            LocalTime time = TimeParser.parse(timeText);
            result = result.plus(time.getMillisOfDay());
        }

        return result;
    }

    /**
     * Splits a string containing a date, time or both into two strings,
     * one containing just a date and the other containing just a time.
     * @param text the text to split into date and time
     * @return an array of two strings.  The first contains the date part
     *         and the second contains the time part.  For example:
     *         ["20/3/1920", "11am"]
     */
    private static String[] splitIntoDateAndTime(String text) {
        // date at time
        // or
        // time on date

        //try for "date at time"
        String[] parts = text.split(" at ");
        if (parts.length == 2) {
            parts[0] = parts[0].trim();
            parts[1] = parts[1].trim();
            return parts;
        }

        //try for "time on date"
        parts = text.split(" on ");
        if (parts.length == 2) {
            // swap the order
            String tmpSwp = parts[0].trim();
            parts[0] = parts[1].trim();
            parts[1] = tmpSwp;
            if (parts[0].startsWith("the ")) {
                parts[0] = parts[0].substring(4);
            }
            return parts;
        }

        //otherwise is it just a time or just a date?
        //for now let's say it's just a date
        parts = new String[2];
        parts[0] = text;
        parts[1] = "";
        return parts;
    }

    private static DateTime parseWordedDate(String text) throws InvalidSyntaxException {
        List<String> wordList = new ArrayList<>(Arrays.asList(text.split(" ")));
        Integer monthWordIndex = findMonthIndex(wordList);
        if (monthWordIndex == null) throw new InvalidSyntaxException(text, 0, "Invalid date.");

        if (monthWordIndex == 0) {
            return parseMDYWordedDate(wordList, monthWordIndex);
        } else {
            return parseDMYWordedDate(wordList, monthWordIndex);
        }
    }

    private static DateTime parseDMYWordedDate(List<String> wordList, int monthWordIndex) throws InvalidSyntaxException {
        //Format: day ([of]) month year

        Month month = Month.fromString(wordList.get(monthWordIndex));

        //everything before month is day
        int lastDayWord = monthWordIndex-1;
        if (wordList.get(lastDayWord).equals("of")) lastDayWord--;
        String dayStr = String.join(" ", wordList.subList(0, lastDayWord+1));
        IntegerParser.Result day = IntegerParser.parseAny(dayStr);

        //everything after month is year
        String yearStr = String.join(" ", wordList.subList(monthWordIndex+1, wordList.size()));
        Integer year = YearParser.parse(yearStr);

        return new DateTime(year, month.getNumber(), day.value, 0, 0, 0);
    }

    private static DateTime parseMDYWordedDate(List<String> wordList, int monthWordIndex) throws InvalidSyntaxException {
        //Format: month day year

        //If there's a comma, than that separates the month and the year...
        Integer commaWordIndex = findWordWithTrailingComma(wordList);
        if (commaWordIndex == null) {
            // No comma found
            return parseMDYWordedDateWithoutComma(wordList, monthWordIndex);
        } else {
            // Comma found
            return parseMDYWordedDateWithComma(wordList, monthWordIndex, commaWordIndex);
        }
    }

    private static DateTime parseMDYWordedDateWithComma(List<String> wordList, int monthWordIndex, int commaWordIndex) throws InvalidSyntaxException {
        //Format: month day, year
        Month month = Month.fromString(wordList.get(monthWordIndex));

        String dayStr = String.join(" ", wordList.subList(1, commaWordIndex+1)).replace(",", "");
        IntegerParser.Result day = IntegerParser.parseAny(dayStr);

        //everything after month is year
        String yearStr = String.join(" ", wordList.subList(commaWordIndex+1, wordList.size()));
        Integer year = YearParser.parse(yearStr);

        return new DateTime(year, month.getNumber(), day.value, 0, 0, 0);
    }

    private static DateTime parseMDYWordedDateWithoutComma(List<String> wordList, int monthWordIndex) throws InvalidSyntaxException {
        //Format: month day year
        Month month = Month.fromString(wordList.get(monthWordIndex));

        //Just try every combination.... :)
        for (int i = 1; i < wordList.size()-1; i++) {
            try {
                return parseMDYWordedDateWithSeperatorLocation(wordList, month, i);
            } catch (InvalidSyntaxException ex) {
                //ignore for now
            }
        }

        throw new InvalidSyntaxException(String.join(" ", wordList.subList(0, wordList.size())), wordList.get(0).length() + 1, "Could not parse day and year...  Not sure it makes sense.");
    }

    private static DateTime parseMDYWordedDateWithSeperatorLocation(List<String> wordList, Month month, int lastDayWordIndex) throws InvalidSyntaxException {
        //Noting that the first word is the month
        //Format: month day year

        String dayStr = String.join(" ", wordList.subList(1, lastDayWordIndex+1)).replace(",", "");
        IntegerParser.Result day = IntegerParser.parseAny(dayStr);

        //everything after month is year
        String yearStr = String.join(" ", wordList.subList(lastDayWordIndex+1, wordList.size()));
        Integer year = YearParser.parse(yearStr);

        return new DateTime(year, month.getNumber(), day.value, 0, 0, 0);
    }


    private static Integer findWordWithTrailingComma(List<String> wordList) {
        for (int i = 0; i < wordList.size(); i++) {
            if (wordList.get(i).trim().endsWith(",")) return i;
        }
        return null;
    }

    private static Integer findMonthIndex(List<String> wordList) {
        Month month;
        for (int i = 0; i < wordList.size(); i++) {
            month = Month.fromString(wordList.get(i));
            if (month != null) return i;
        }
        return null;
    }

    private static boolean isSimpleDateFormat(String text) {
        String[] parts = text.split("/");
        return (parts.length == 3);
    }

    private static DateTime parseSimpleDate(String text) throws InvalidSyntaxException {
        String[] parts = text.split("/");

        IntegerParser.Result day = IntegerParser.parseAny(parts[0]);
        IntegerParser.Result month = IntegerParser.parseAny(parts[1]);
        IntegerParser.Result year = IntegerParser.parseAny(parts[2]);

        if (day.type != IntegerParser.ResultType.Digits) {
            int errIndex = 0;
            throw new InvalidSyntaxException(text, errIndex, "In this format, day '" + parts[0] + "' is not valid.  Try replacing it with '" + String.valueOf(day.value) + "'." );
        }

        if (month.type != IntegerParser.ResultType.Digits) {
            int errIndex = parts[0].length() + 1;
            throw new InvalidSyntaxException(text, errIndex, "In this format, month '" + parts[1] + "' is not valid.  Try replacing it with '" + String.valueOf(month.value) + "'." );
        }

        if (year.type != IntegerParser.ResultType.Digits) {
            int errIndex = parts[0].length() + 1 + parts[1].length() + 1;
            throw new InvalidSyntaxException(text, errIndex, "In this format, year '" + parts[2] + "' is not valid.  Try replacing it with '" + String.valueOf(year.value) + "'." );
        }

        try {
            return new DateTime(year.value, month.value, day.value, 0, 0, 0);
        } catch (IllegalFieldValueException ex) {
            int errIndex = 0;
            if (ex.getFieldName().equals("dayOfMonth")) {
                errIndex = 0;
            } else if (ex.getFieldName().equals("monthOfYear")) {
                errIndex = parts[0].length() + 1;
            } else if (ex.getFieldName().equals("year")) {
                errIndex = parts[0].length() + 1 + parts[1].length() + 1;
            }
            throw new InvalidSyntaxException(text, errIndex, ex.getMessage());
        }
    }
}
