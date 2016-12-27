package TimerDescriptionLanguage;

import org.joda.time.LocalTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 18/12/16.
 */
public class TimeParserTest {

    @Test
    public void testParseSimpleTime12Hour() throws InvalidSyntaxException {
        assertEquals(TimeParser.parse("9am"), new LocalTime(9, 0, 0));
        assertEquals(TimeParser.parse("10am"), new LocalTime(10, 0, 0));
        assertEquals(TimeParser.parse("10a.m."), new LocalTime(10, 0, 0));
        assertEquals(TimeParser.parse("10a.M."), new LocalTime(10, 0, 0));
        assertEquals(TimeParser.parse("6pm"), new LocalTime(18, 0, 0));
    }

    @Test
    public void testParseSimpleTime12HourWithSpace() throws InvalidSyntaxException {
        assertEquals(TimeParser.parse("9 am"), new LocalTime(9, 0, 0));
        assertEquals(TimeParser.parse("10 am"), new LocalTime(10, 0, 0));
        assertEquals(TimeParser.parse("8 PM"), new LocalTime(20, 0, 0));
        assertEquals(TimeParser.parse("8 P.M."), new LocalTime(20, 0, 0));
        assertEquals(TimeParser.parse("eight pm"), new LocalTime(20, 0, 0));
    }

    @Test
    public void testParseTime12HourWithMinutes() throws InvalidSyntaxException {
        assertEquals(TimeParser.parse("9:30 am"), new LocalTime(9, 30, 0));
        assertEquals(TimeParser.parse("22:30 pm"), new LocalTime(22, 30, 0));
        assertEquals(TimeParser.parse("seven twenty am"), new LocalTime(7, 20, 0));
        assertEquals(TimeParser.parse("11:25am"), new LocalTime(11, 25, 0));
        //assertEquals(TimeParser.parse("8 o'clock PM"), new LocalTime(20, 0, 0)); //not supported yet.
        assertEquals(TimeParser.parse("eight forty five P.M."), new LocalTime(20, 45, 0));
        assertEquals(TimeParser.parse("eight forty-five P.M."), new LocalTime(20, 45, 0));
        assertEquals(TimeParser.parse("9:30 pm"), new LocalTime(21, 30, 0));
    }

    @Test
    public void testParseWordTimeWithoutMeridiemThrowsException() {
        try {
            TimeParser.parse("ten thirty");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Time is ambiguous, provide a meridiem (ie AM or PM).");
            assertEquals(ex.getCode(), "ten thirty");
            assertEquals(ex.getParseExceptionIndex(), "ten thirty".length());
        }
        try {
            TimeParser.parse("eleven");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Time is ambiguous, provide a meridiem (ie AM or PM).");
            assertEquals(ex.getCode(), "eleven");
            assertEquals(ex.getParseExceptionIndex(), "eleven".length());
        }
    }

    @Test
    public void testParse24hTimeWithMeridiemThrowsException() {
        try {
            TimeParser.parse("13:20 am");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Time doesn't make sense.  Hour 13 is pm, not am as specified.");
            assertEquals(ex.getCode(), "13:20 am");
            assertEquals(ex.getParseExceptionIndex(), 5);
        }
        try {
            TimeParser.parse("23:50AM");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Time doesn't make sense.  Hour 23 is pm, not am as specified.");
            assertEquals(ex.getCode(), "23:50AM");
            assertEquals(ex.getParseExceptionIndex(), 5);
        }
    }

    @Test
    public void testParseInvalidTimes() {
        try {
            TimeParser.parse("ten sixty am");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Could not parse time.");
            assertEquals(ex.getCode(), "ten sixty am");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }

        try {
            TimeParser.parse("25:10 am");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Value 25 for hourOfDay must be in the range [0,23]");
            assertEquals(ex.getCode(), "25:10 am");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }

        try {
            TimeParser.parse("Potato Cat fish pm");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Could not parse time.");
            assertEquals(ex.getCode(), "Potato Cat fish pm");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }

        try {
            TimeParser.parse("11:61 am");
            fail("Should have thrown exception.");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getMessage(), "Value 61 for minuteOfHour must be in the range [0,59]");
            assertEquals(ex.getCode(), "11:61 am");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }
    }






}