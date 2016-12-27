package TimerDescriptionLanguage;

import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 14/11/16.
 */
public class PeriodParserTest {

    @Test
    public void testParseSecond() throws Exception {
        Period p = PeriodParser.parse("second");

        Assert.assertEquals(p, TimeHelper.makePeriodSeconds(1));
    }

    @Test
    public void testParseMinute() throws Exception {
        Period p = PeriodParser.parse("minute");

        assertEquals(p, TimeHelper.makePeriodMinutes(1));
    }

    @Test
    public void testParseHour() throws Exception {
        Period p = PeriodParser.parse("hour");

        assertEquals(p, TimeHelper.makePeriodHours(1));
    }

    @Test
    public void testParseDay() throws Exception {
        Period p = PeriodParser.parse("day");

        assertEquals(p, TimeHelper.makePeriodDays(1));
    }

    @Test
    public void testParseWeek() throws Exception {
        Period p = PeriodParser.parse("week");

        assertEquals(p, TimeHelper.makePeriodWeeks(1));
    }

    @Test
    public void testParseMonth() throws Exception {
        Period p = PeriodParser.parse("month");

        assertEquals(p, TimeHelper.makePeriodMonths(1));
    }

    @Test
    public void testParseYear() throws Exception {
        Period p = PeriodParser.parse("years");

        assertEquals(p, TimeHelper.makePeriodYears(1));
    }

    @Test
    public void testParseSecondYear() throws Exception {
        Period p = PeriodParser.parse("second year");

        assertEquals(p, TimeHelper.makePeriodYears(2));
    }

    @Test
    public void testParseThreeYears() throws Exception {
        Period p = PeriodParser.parse("three years");

        assertEquals(p, TimeHelper.makePeriodYears(3));
    }

    @Test
    public void testParserIgnoresPlurals() throws Exception {
        Period p = PeriodParser.parse("weeks");
        assertEquals(p, TimeHelper.makePeriodWeeks(1));

        p = PeriodParser.parse("days");
        assertEquals(p, TimeHelper.makePeriodDays(1));

        p = PeriodParser.parse("hours");
        assertEquals(p, TimeHelper.makePeriodHours(1));
    }


    @Test(expectedExceptions = Exception.class)
    public void testParserThrowsExceptionOnInvalidSpecifier() throws Exception {
        Period p = PeriodParser.parse("weeksaaa");
    }

    @Test
    public void testParse2Seconds() throws Exception {
        Period p = PeriodParser.parse("2 seconds");

        assertEquals(p, TimeHelper.makePeriodSeconds(2));
    }

    @Test
    public void testParseTwoSeconds() throws Exception {
        Period p = PeriodParser.parse("two seconds");

        assertEquals(p, TimeHelper.makePeriodSeconds(2));
    }

    @Test
    public void testParse10Seconds() throws Exception {
        Period p = PeriodParser.parse("10 seconds");

        assertEquals(p, TimeHelper.makePeriodSeconds(10));
    }

    @Test
    public void testParseFifthMonths() throws Exception {
        Period p = PeriodParser.parse("fifth month");

        assertEquals(p, TimeHelper.makePeriodMonths(5));
    }

    @Test
    public void testParseTwentyFourHours() throws Exception {
        Period p = PeriodParser.parse("twenty four hours");

        assertEquals(p, TimeHelper.makePeriodHours(24));
    }

    @Test
    public void testParseMinutesAndSeconds() throws Exception {
        Period p = PeriodParser.parse("three minutes and 24 seconds");

        assertEquals(p, TimeHelper.makePeriodCustom(
                TimeHelper.makeTimeTypePair(3, DurationFieldType.minutes()),
                TimeHelper.makeTimeTypePair(24, DurationFieldType.seconds())
        ));
    }

    @Test
    public void testParseLotsOfParts() throws Exception {
        Period p = PeriodParser.parse("year, 22 days, three minutes and 24 seconds");

        assertEquals(p, TimeHelper.makePeriodCustom(
                TimeHelper.makeTimeTypePair(1, DurationFieldType.years()),
                TimeHelper.makeTimeTypePair(22, DurationFieldType.days()),
                TimeHelper.makeTimeTypePair(3, DurationFieldType.minutes()),
                TimeHelper.makeTimeTypePair(24, DurationFieldType.seconds())
        ));
    }

    @Test
    public void testParseInvalidThrowsException() throws Exception {
        try {
            Period p = PeriodParser.parse("one minute cactus bogus words");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "one minute cactus bogus words");
            assertEquals(e.getParseExceptionIndex(), 10);
            assertEquals(e.getMessage(), "Could not parse due to extra words at the end of the period.");
        }

        try {
            Period p = PeriodParser.parse("spaghetti one minute");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "spaghetti one");
            assertEquals(e.getParseExceptionIndex(), 0);
            assertEquals(e.getMessage(), "Could not parse as number or ordinal");
        }
    }



//    @Test
//    public void testParseMinutesAndSeconds() throws Exception {
//        Period p = PeriodParser.parse("three minutes and 24 seconds");
//
////        TimeHelper.makePeriodMinutes()
//
//        assertEquals(p, TimeHelper.makePeriodHours(24));
//    }

}