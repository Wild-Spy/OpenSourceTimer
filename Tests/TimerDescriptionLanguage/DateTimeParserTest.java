package TimerDescriptionLanguage;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 16/12/16.
 */
public class DateTimeParserTest {
    @Test
    public void testParseSimpleDates() throws InvalidSyntaxException {
        assertEquals(DateTimeParser.parse("22/12/1988"), new DateTime(1988, 12, 22, 0, 0, 0));
        assertEquals(DateTimeParser.parse("16/12/2016"), new DateTime(2016, 12, 16, 0, 0, 0));
        assertEquals(DateTimeParser.parse("29/2/2020"), new DateTime(2020, 2, 29, 0, 0, 0));
    }

    @Test
    public void testParseDateThatDoesNotExist() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("30/2/2020");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "30/2/2020");
            assertEquals(ex.getMessage(), "Value 30 for dayOfMonth must be in the range [1,29]");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }
    }

    @Test
    public void testParseDateWithInvalidMonthThrowsException() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("30/13/2020");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "30/13/2020");
            assertEquals(ex.getMessage(), "Value 13 for monthOfYear must be in the range [1,12]");
            assertEquals(ex.getParseExceptionIndex(), 3);
        }
    }

    @Test
    public void testParseDateWithWordedNumberDayThrowsException() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("tenth/12/2020");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "tenth/12/2020");
            assertEquals(ex.getMessage(), "In this format, day 'tenth' is not valid.  Try replacing it with '10'.");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }
    }

    @Test
    public void testParseDateWithWordedNumberMonthThrowsException() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("10/second/2020");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "10/second/2020");
            assertEquals(ex.getMessage(), "In this format, month 'second' is not valid.  Try replacing it with '2'.");
            assertEquals(ex.getParseExceptionIndex(), 3);
        }
    }

    @Test
    public void testParseDateWithWordedNumberYearThrowsException() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("10/2/two thousand and seventeen");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "10/2/two thousand and seventeen");
            assertEquals(ex.getMessage(), "In this format, year 'two thousand and seventeen' is not valid.  Try replacing it with '2017'.");
            assertEquals(ex.getParseExceptionIndex(), 5);
        }
    }

    @Test
    public void testParseDateWithNonsenseWordDayThrowsException() throws InvalidSyntaxException {
        try {
            DateTimeParser.parse("apple/12/2020");
            fail("Did not throw exception!");
        } catch (InvalidSyntaxException ex) {
            assertEquals(ex.getCode(), "apple");
            assertEquals(ex.getMessage(), "Could not parse as number or ordinal");
            assertEquals(ex.getParseExceptionIndex(), 0);
        }
    }

    @Test
    public void testParseWordedDate() throws InvalidSyntaxException {
        assertEquals(DateTimeParser.parse("twenty second of december 1988"), new DateTime(1988, 12, 22, 0, 0, 0));
        assertEquals(DateTimeParser.parse("sixteenth of december twenty sixteen"), new DateTime(2016, 12, 16, 0, 0, 0));
        assertEquals(DateTimeParser.parse("twentieth of August twenty twenty five"), new DateTime(2025, 8, 20, 0, 0, 0));
    }

    @Test
    public void testParseWordedDateReverseOrder() throws InvalidSyntaxException {
        assertEquals(DateTimeParser.parse("March third, 2017"), new DateTime(2017, 3, 3, 0, 0, 0));
        assertEquals(DateTimeParser.parse("April sixth, nineteen ninety two"), new DateTime(1992, 4, 6, 0, 0, 0));

        assertEquals(DateTimeParser.parse("March third 2017"), new DateTime(2017, 3, 3, 0, 0, 0));
        assertEquals(DateTimeParser.parse("April sixth nineteen ninety two"), new DateTime(1992, 4, 6, 0, 0, 0));
        assertEquals(DateTimeParser.parse("June five 1991"), new DateTime(1991, 6, 5, 0, 0, 0));
        assertEquals(DateTimeParser.parse("June twenty one one hundred"), new DateTime(100, 6, 21, 0, 0, 0));
        assertEquals(DateTimeParser.parse("April twenty twenty sixteen"), new DateTime(2016, 4, 20, 0, 0, 0));
        assertEquals(DateTimeParser.parse("April twenty one"), new DateTime(1, 4, 20, 0, 0, 0));  //Ambiguous! Did we want april 20 (year 1) or april 20 (no year specified) probably the second..
        assertEquals(DateTimeParser.parse("April twenty, one"), new DateTime(1, 4, 20, 0, 0, 0));  //Ambiguous! Did we want april 20 (year 1) or april 20 (no year specified) probably the second..
        //assertEquals(DateTimeParser.parse("April twenty one hundred BC"), new DateTime(-100, 4, 20, 0, 0, 0));  //Ambiguous! Did we want april 20 (year 1) or april 20 (no year specified) probably the second..
        assertEquals(DateTimeParser.parse("April twenty two thousand"), new DateTime(2000, 4, 20, 0, 0, 0));
        //assertEquals(DateTimeParser.parse("June five 91'"), new DateTime(1991, 6, 5, 0, 0, 0));
        //assertEquals(DateTimeParser.parse("October 2000"), new DateTime(2000, 10, 1, 0, 0, 0));
    }

    @Test
    public void testParseDateWithHour() throws InvalidSyntaxException {
        assertEquals(DateTimeParser.parse("twenty second of december 1988 at 9am"), new DateTime(1988, 12, 22, 9, 0, 0));
        assertEquals(DateTimeParser.parse("10am on sixteenth of december twenty sixteen"), new DateTime(2016, 12, 16, 10, 0, 0));
        assertEquals(DateTimeParser.parse("10 am on sixteenth of december twenty sixteen"), new DateTime(2016, 12, 16, 10, 0, 0));
//        assertEquals(DateTimeParser.parse("eleven oclock pm on twentieth of August twenty twenty five"), new DateTime(2025, 8, 20, 0, 0, 0));
//        assertEquals(DateTimeParser.parse("eleven o'clock on twentieth of August twenty twenty five"), new DateTime(2025, 8, 20, 11, 0, 0));
        assertEquals(DateTimeParser.parse("twenty second of december 1988 at 10pm"), new DateTime(1988, 12, 22, 22, 0, 0));
        assertEquals(DateTimeParser.parse("twenty second of december 1988 at ten pm"), new DateTime(1988, 12, 22, 22, 0, 0));
//        assertEquals(DateTimeParser.parse("midday on the 1/1/2017"), new DateTime(2017, 1, 1, 12, 0, 0));
        assertEquals(DateTimeParser.parse("1/1/2017 at 11pm"), new DateTime(2017, 1, 1, 23, 0, 0));
//        assertEquals(DateTimeParser.parse("12/1/2017 in the afternoon"), new DateTime(2017, 1, 12, 23, 0, 0));
        assertEquals(DateTimeParser.parse("12/1/2017 at 11:00"), new DateTime(2017, 1, 12, 11, 0, 0));
        assertEquals(DateTimeParser.parse("12/1/2017 at 21:00"), new DateTime(2017, 1, 12, 21, 0, 0));
        //perhaps add 'eleven hundred hours'
    }


}