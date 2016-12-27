package TimerDescriptionLanguage;

import TimerDescriptionLanguage.InvalidSyntaxException;
import TimerDescriptionLanguage.PeriodInterval;
import TimerDescriptionLanguage.PeriodIntervalParser;
import TimerDescriptionLanguage.TimeHelper;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 17/11/16.
 */
public class PeriodIntervalParserTest {

    public static void assertEqualsPeriodIntervalList(List<PeriodInterval> actual, List<PeriodInterval> expected) {
        assertEquals(actual.size(), expected.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(actual.get(i), expected.get(i));
        }
    }

    @Test
    public void testParseSimple() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("for 3 hours"),
                TimeHelper.betweenHours(0, 3));
    }

    @Test
    public void testForMonths() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("for 6 months"),
                TimeHelper.betweenMonths(1, 7));
    }

    @Test
    public void testForWithWordNumber() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("for twenty seconds"),
                TimeHelper.betweenSeconds(0, 20));
    }

    @Test
    public void testBetweenSeconds() throws InvalidSyntaxException {
        //Two ways it should work.
        //period type before
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between ten and twenty seconds"),
                TimeHelper.betweenSeconds(10, 20));
        //period type after
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between second ten and twenty"),
                TimeHelper.betweenSeconds(10, 20));
    }

    @Test
    public void testBetweenDays() throws InvalidSyntaxException {
        //Two ways it should work.
        //period type before
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between 8 and nineteen days"),
                TimeHelper.betweenDays(8, 19));
        //period type after
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between day five and thirty"),
                TimeHelper.betweenDays(5, 30));
    }

    @Test
    public void testBetweenOrdinals() throws InvalidSyntaxException {
        //note that the 'day' doesn't have to be plural here.
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between the first and the tenth day"),
                TimeHelper.betweenDays(1, 10));
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("between the first and tenth day"),
                TimeHelper.betweenDays(1, 10));
    }

    //let's allow this for now.
//    @Test(expectedExceptions = InvalidSyntaxException.class)
//    public void testBetweenOrdinalsWithPeriodAfterTypeThrowsException() throws InvalidSyntaxException {
//        PeriodIntervalParser.parse("between day the first and the tenth");
//    }

    @Test
    public void testOnDays() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on days 1, 5, 8"),
                TimeHelper.onDays(1, 5, 8));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th day"),
                TimeHelper.onDays(1, 5, 8));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th days"),
                TimeHelper.onDays(1, 5, 8));
    }


//    @Test
//    public void testOnSecondsWithHundredsAndExtraAnd() throws InvalidSyntaxException {
//        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on seconds one, one hundred and five and one hundred and eleven"),
//                TimeHelper.onDays(1, 105, 111));
//
//        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on seconds one, one hundred and five"),
//                TimeHelper.onDays(1, 105, 111));
//
//        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th day"),
//                TimeHelper.onDays(1, 5, 8));
//
//        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th and 8th days"),
//                TimeHelper.onDays(1, 5, 8));
//    }


    @Test
    public void testOnDayImplicitPeriod() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st", TimeHelper.makePeriodMonths(1)),
                TimeHelper.onDays(1));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th", TimeHelper.makePeriodMonths(1)),
                TimeHelper.onDays(1, 5, 8));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 23rd", TimeHelper.makePeriodMonths(1)),
                TimeHelper.onDays(1, 5, 23));
    }

    @Test
    public void testOnMinute() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on minute 1, 5, 8"),
                TimeHelper.onMinutes(1, 5, 8));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th minute"),
                TimeHelper.onMinutes(1, 5, 8));

        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th minute"),
                TimeHelper.onMinutes(1, 5, 8));
    }

    @Test
    public void testOnOf() throws InvalidSyntaxException {
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th day of"),
                TimeHelper.onDays(1, 5, 8));
        assertEqualsPeriodIntervalList(PeriodIntervalParser.parse("on the 1st, 5th, 8th month of"),
                TimeHelper.onMonths(1, 5, 8));
    }
}