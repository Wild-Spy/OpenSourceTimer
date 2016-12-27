package TimerDescriptionLanguage;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mcochrane on 18/12/16.
 */
public class TimeParser {

    private String textToParse;
    LocalTime result = null;

    private final static String[] AM = {"am", "a.m."};
    private final static String[] PM = {"pm", "p.m."};

    private TimeParser(String textToParse) {
        this.textToParse = textToParse;
    }

    public static LocalTime parse(String text) throws InvalidSyntaxException {
        TimeParser parser = new TimeParser(text);
        parser.parse();
        LocalTime result = parser.getResult();
        if (result == null) throw new InvalidSyntaxException(text, 0, "Unknown error occurred.");
        return result;
    }

    private enum AMPMIndicator {
        None,
        AM,
        PM
    }

    private void parse() throws InvalidSyntaxException {
        String parseText = textToParse.toLowerCase();

        AMPMIndicator ampmIndicator = getAMPMIndicator(parseText);
        parseText = removeAMPMIndicatorFromEnd(parseText);

        if (isNumberFormatTime(parseText)) {
            this.result = parseNumberFormatTime(parseText, ampmIndicator);
        } else {
            this.result = parseWordFormatTime(parseText, ampmIndicator);
        }
    }

    private AMPMIndicator getAMPMIndicator(String parseText) {
        if (hasAMIndicator(parseText)) {
            return AMPMIndicator.AM;
        } else if (hasPMIndicator(parseText)) {
            return AMPMIndicator.PM;
        }
        return AMPMIndicator.None;
    }

    private String removeAMPMIndicatorFromEnd(String parseText) {
        if (hasAMIndicator(parseText)) {
            return ParseHelper.removeAnyFromEnd(parseText, AM).trim();
        } else if (hasPMIndicator(parseText)) {
            return ParseHelper.removeAnyFromEnd(parseText, PM).trim();
        }
        return parseText;
    }

    private boolean hasAMIndicator(String parseText) {
        return ParseHelper.endsWithAnyOf(parseText, AM);
    }

    private boolean hasPMIndicator(String parseText) {
        return ParseHelper.endsWithAnyOf(parseText, PM);
    }

    private boolean isNumberFormatTime(String parseText) {
        return parseText.contains(":");
    }

    private LocalTime parseWordFormatTime(String parseText, AMPMIndicator ampmIndicator) throws InvalidSyntaxException {
        Integer hour;

        if (ampmIndicator == AMPMIndicator.None) {
            throw new InvalidSyntaxException(this.textToParse, this.textToParse.length(),
                    "Time is ambiguous, provide a meridiem (ie AM or PM).");
        }
        try {
            hour = IntegerParser.parseNumber(parseText).value;
            hour += (ampmIndicator == AMPMIndicator.PM ? 12 : 0); // add 12 hours for PM if necessary
            return createLocalTimeRethrowException(hour, 0, 0);
        } catch (InvalidSyntaxException ex) {
            List<String> wordList = new ArrayList<>(Arrays.asList(parseText.split(" ")));
            for (int i = 0; i < wordList.size(); i++) {
                try {
                    return parseTimeWithHoursAndMinutesSplitAt(wordList, i, ampmIndicator);
                } catch (InvalidSyntaxException ignored) {}
            }
            throw new InvalidSyntaxException(this.textToParse, 0, "Could not parse time.");
        }
    }

    private LocalTime parseNumberFormatTime(String parseText, AMPMIndicator ampmIndicator) throws InvalidSyntaxException {
        String hourStr;
        String minuteStr;
        Integer hour = null;
        Integer minute = 0;

        String[] parts = parseText.split(":");
        hourStr = parts[0];
        minuteStr = parts[1];
        try {
            hour = Integer.valueOf(hourStr);
        } catch (NumberFormatException ex) {

        }
        try {
            minute = Integer.valueOf(minuteStr);
        } catch (NumberFormatException ex) {

        }

        if (hour > 12 & hour < 24) {
            if (ampmIndicator != null && ampmIndicator == AMPMIndicator.AM) {
                throw new InvalidSyntaxException(this.textToParse,
                        parseText.length(),
                        "Time doesn't make sense.  Hour " + Integer.toString(hour) + " is pm, not am as specified.");
            }
            ampmIndicator = AMPMIndicator.None; // so we don't add another 12 hours
        }

        hour += (ampmIndicator == AMPMIndicator.PM ? 12 : 0); // add 12 hours for PM if necessary
        return createLocalTimeRethrowException(hour, minute, 0);
    }

    private LocalTime parseTimeWithHoursAndMinutesSplitAt(List<String> wordList, int lastHourWordIndex, AMPMIndicator ampmIndicator) throws InvalidSyntaxException {
        //Noting that the first word is the month
        //Format: hour

        String hourStr = String.join(" ", wordList.subList(0, lastHourWordIndex+1)).replace(",", "");
        IntegerParser.Result hour = IntegerParser.parseAny(hourStr);

        //everything after hour is minute
        String minuteStr = String.join(" ", wordList.subList(lastHourWordIndex+1, wordList.size()));
        Integer minute = YearParser.parse(minuteStr.replace("-", " "));

        hour.value += (ampmIndicator == AMPMIndicator.PM ? 12 : 0); // add 12 hours for PM if necessary
        return createLocalTimeRethrowException(hour.value, minute, 0);
    }

    private LocalTime createLocalTimeRethrowException(int hourOfDay, int minuteOfHour, int secondOfMinute) throws InvalidSyntaxException {
        try {
            return new LocalTime(hourOfDay, minuteOfHour, secondOfMinute);
        } catch (IllegalFieldValueException ex) {
            throw new InvalidSyntaxException(this.textToParse, 0, ex.getMessage());
        }
    }

    private LocalTime getResult() {
        return this.result;
    }

}
