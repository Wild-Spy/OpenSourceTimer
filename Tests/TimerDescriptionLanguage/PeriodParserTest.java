package TimerDescriptionLanguage;

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


}