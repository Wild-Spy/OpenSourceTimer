package TimerDescriptionLanguage;

import org.joda.time.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 13/11/16.
 */
public class RuleTest {
    @BeforeMethod
    public void setUp() throws Exception {
        Rules.resetInstance();
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testSimpleRuleCreation() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);
        List<PeriodInterval> intervals = new ArrayList<>();
        //need to ensure it starts at 1/1/1970 and that timezone is UTC
        intervals.add(new PeriodInterval(Period.days(0), Period.days(1)));


        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                intervals, new Period(Period.months(1), PeriodType.months()), true);

        r.update(new DateTime(2016, 3, 1, 10, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 1, 23, 50, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 2, 0, 0, 1));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testMultipleIntervals() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.onDays(1, 7),
                TimeHelper.makePeriodMonths(1), true);

        r.update(new DateTime(2016, 3, 1, 10, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 1, 23, 50, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 2, 0, 0, 1));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 3, 7, 12, 0, 1));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 8, 12, 0, 1));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testRuleOnMinutesPeriod() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.join(TimeHelper.betweenSeconds(10, 20),
                        TimeHelper.onMinutes(3, 4)),
                TimeHelper.makePeriodMinutes(5), true);

        //First Period
        r.update(new DateTime(2016, 1, 1, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0, 15));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0, 21));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 3, 30));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 4, 1));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        //Second Period
        r.update(new DateTime(2016, 1, 1, 0, 5, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 5, 10));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
    }

    @Test
    public void testBetweenSecondsBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenSeconds(10, 20),
                TimeHelper.makePeriodMinutes(5), true);

        r.update(new DateTime(2016, 1, 1, 0, 0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0,  9, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0, 10));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0, 19, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 0, 20, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenSecondsThrowsExceptionOnNegativeStartSecond() {
        TimeHelper.betweenSeconds(-1, 10);
    }

    @Test
    public void testBetweenSecondsDoesNotThrowExceptionZeroStartSecond() throws InvalidParameterException {
        TimeHelper.betweenSeconds(0, 1);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenSecondsThrowsExceptionOnEndSecondLessThanStartSecond() {
        TimeHelper.betweenSeconds(10, 9);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenSecondsThrowsExceptionOnEndSecondEqualsStartSecond() {
        TimeHelper.betweenSeconds(10, 10);
    }

    @Test
    public void testBetweenMinutesBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenMinutes(10, 20),
                TimeHelper.makePeriodHours(5), true);

        r.update(new DateTime(2016, 1, 1, 0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0,  9, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 10,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 19, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 0, 20,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMinutesThrowsExceptionOnNegativeStartMinute() {
        TimeHelper.betweenMinutes(-1, 10);
    }

    @Test
    public void testBetweenMinutesDoesNotThrowExceptionZeroStartMinute() throws InvalidParameterException {
        TimeHelper.betweenMinutes(0, 1);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMinutesThrowsExceptionOnEndMinuteLessThanStartMinute() {
        TimeHelper.betweenMinutes(10, 9);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMinutesThrowsExceptionOnEndMinuteEqualsStartMinute() {
        TimeHelper.betweenMinutes(10, 10);
    }

    @Test
    public void testBetweenHoursBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(10, 20),
                TimeHelper.makePeriodDays(5), true);

        r.update(new DateTime(2016, 1, 1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1,  9, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 1, 10,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 19, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 1, 20,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        r.update(new DateTime(2016, 1, 6,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 6,  9, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 6, 10,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 6, 19, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 6, 20,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenHoursThrowsExceptionOnNegativeStartHour() {
        TimeHelper.betweenHours(-1, 10);
    }

    @Test
    public void testBetweenHoursDoesNotThrowExceptionZeroStartHour() throws InvalidParameterException {
        TimeHelper.betweenHours(0, 1);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenHoursThrowsExceptionOnEndHourLessThanStartHour() {
        TimeHelper.betweenHours(10, 9);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenHoursThrowsExceptionOnEndHourEqualsStartHour() {
        TimeHelper.betweenHours(10, 10);
    }

    @Test
    public void testBetweenDaysBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenDays(10, 20),
                TimeHelper.makePeriodMonths(5), true);

        r.update(new DateTime(2016, 1,  1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1,  9, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 10,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 19, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 20,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenDaysThrowsExceptionOnZeroStartDay() {
        TimeHelper.betweenDays(0, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenDaysThrowsExceptionOnNegativeStartDay() {
        TimeHelper.betweenDays(-1, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenDaysThrowsExceptionOnEndDayEqualsStartDay() {
        TimeHelper.betweenDays(10, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenDaysThrowsExceptionOnEndDayLessThanStartDay() {
        TimeHelper.betweenDays(10, 9);
    }

    @Test
    public void testBetweenDaysDoesNotThrowExceptionWhenStartDayIsOne() throws InvalidParameterException {
        TimeHelper.betweenDays(1, 10);
    }

    @Test
    public void testBetweenWeeksBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenWeeks(2, 4),
                TimeHelper.makePeriodMonths(5), true);

        r.update(new DateTime(2016, 1,  1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1,  7, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1,  8,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 21, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 22,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenWeeksThrowsExceptionOnZeroStartWeek() {
        TimeHelper.betweenWeeks(0, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenWeeksThrowsExceptionOnNegativeStartWeek() {
        TimeHelper.betweenWeeks(-1, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenWeeksThrowsExceptionOnEndWeekEqualsStartWeek() {
        TimeHelper.betweenWeeks(10, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenWeeksThrowsExceptionOnEndWeekLessThanStartWeek() {
        TimeHelper.betweenWeeks(10, 9);
    }

    @Test
    public void testBetweenWeeksDoesNotThrowExceptionWhenStartWeekIsOne() throws InvalidParameterException {
        TimeHelper.betweenWeeks(1, 10);
    }
    
    @Test
    public void testBetweenMonthsBoundaries() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenMonths(2, 4),
                TimeHelper.makePeriodYears(5), true);

        r.update(new DateTime(2016, 1,  20,  0,  0,  0)); //Shouldn't matter when we start in the month.
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 31, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,  1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3, 31, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 4,  1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMonthsThrowsExceptionOnZeroStartMonth() {
        TimeHelper.betweenMonths(0, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMonthsThrowsExceptionOnNegativeStartMonth() {
        TimeHelper.betweenMonths(-1, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMonthsThrowsExceptionOnEndMonthEqualsStartMonth() {
        TimeHelper.betweenMonths(10, 10);
    }

    @Test(expectedExceptions = InvalidParameterException.class)
    public void testBetweenMonthsThrowsExceptionOnEndMonthLessThanStartMonth() {
        TimeHelper.betweenMonths(10, 9);
    }

    @Test
    public void testBetweenMonthsDoesNotThrowExceptionWhenStartMonthIsOne() throws InvalidParameterException {
        TimeHelper.betweenMonths(1, 10);
    }

    @Test
    public void testTimeHelper() throws Exception {
        List<PeriodInterval> intervals = TimeHelper.onDays(1, 2, 3);
        assertEquals(intervals.size(), 3);

        //TimeHelper.makeInterval(TimeHelper.makePeriod.days())
    }

    @Test
    public void testCanGetRule() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenMonths(2, 4),
                TimeHelper.makePeriodYears(5), false);

        Rule rFetch = Rules.getInstance().get("Test");

        assertEquals(rFetch, r);
    }

    @Test
    public void testGetNonExistentRuleReturnsNull() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule rFetch = Rules.getInstance().get("NonExistentRuleName");
        assertNull(rFetch);
    }

    @Test
    public void testOnDaysAlwaysOnMultipleDaysInARow() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.onDays(3, 4, 5),
                TimeHelper.makePeriodMonths(1), true);

        r.update(new DateTime(2016, 1, 1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 2, 23, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 1, 3,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 4,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 4,  0, 23, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 5,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1, 6,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testGetNextStateChangeTimeEverySecondHour() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(0, 1),
                TimeHelper.makePeriodHours(2), true);

        DateTime nextUpdate;

        r.update(new DateTime(2016, 1, 1, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 1, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 2, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 3, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testGetNextStateChangeTimeEveryThirdMinute() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenMinutes(0, 1),
                TimeHelper.makePeriodMinutes(3), true);

        DateTime nextUpdate;

        r.update(new DateTime(2016, 1, 1, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 0, 1, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 0, 3, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 1, 0, 4, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testGetNextStateChangeTimeOnDays() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.onDays(3, 5, 6, 10),
                TimeHelper.makePeriodMonths(1), true);

        DateTime nextUpdate;

        r.update(new DateTime(2016, 1, 1, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 3, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 4, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 5, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 6, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 7, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 10, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 1, 11, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        //Start of second period
        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 2, 1, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        nextUpdate = r.getNextStateChangeTime();
        r.update(nextUpdate);
        assertEquals(nextUpdate, new DateTime(2016, 2, 3, 0, 0, 0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
    }

    @Test
    public void testInfiniteLengthRule() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(0, 1),
                TimeHelper.infinitePeriod(), true);

        r.update(new DateTime(2016, 1,  20,  0,  0,  0)); //Shouldn't matter when we start in the month.
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1,  20,  0,  0,  0, 1));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1,  20,  0, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 1,  20,  1,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,  13, 20, 32,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(99999,1,   1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testRuleWithStart() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(0, 1),
                TimeHelper.makePeriodDays(1), true,
                new DateTime(2016, 2, 1, 10, 0, 0));

        r.update(new DateTime(2016, 1,  20,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1,  9, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1, 10,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 10,  59,  59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 11,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        r.update(new DateTime(2016, 3,   1,  9, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 3,   1, 10,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3,   1, 10,  59,  59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3,   1, 11,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testRuleWithStartNoAlign() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(0, 1),
                TimeHelper.makePeriodDays(1), true,
                new DateTime(2016, 2, 1, 10, 30, 30));

        r.update(new DateTime(2016, 1,  20,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1, 10, 29, 29, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1, 10, 30, 30));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 11, 29, 29, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 11, 30, 30));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);

        r.update(new DateTime(2016, 3,   1, 10, 29, 29, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 3,   1, 10, 30, 30));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3,   1, 11, 29, 29, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 3,   1, 11, 30, 30));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

    @Test
    public void testRuleWithStartAndInfiniteLength() throws Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Activator chAct = new ChannelActivator("1", ActivatorState.DISABLED);

        Rule r = new Rule("Test", new Action(ActivatorState.ENABLED, chAct),
                TimeHelper.betweenHours(0, 1),
                TimeHelper.infinitePeriod(), true,
                new DateTime(2016, 2, 1, 10, 0, 0));

        r.update(new DateTime(2016, 1,  20,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1,  9, 59, 59, 999));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(2016, 2,   1, 10,  0,  0));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 10,  59,  59, 999));
        assertEquals(r.getOutputState(), RuleState.ACTIVE);
        r.update(new DateTime(2016, 2,   1, 11,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        r.update(new DateTime(99999,1,   1,  0,  0,  0));
        assertEquals(r.getOutputState(), RuleState.INACTIVE);
    }

//    @Test
//    public void testMultiLevelRules() throws Exception {
//        Activator chAct = new ChannelActivator(ActivatorState.DISABLED);
//        List<Interval> intervals = new ArrayList<>();
//        //need to ensure it starts at 1/1/1970 and that timezone is UTC
//        //on the 1st of the month
//        intervals.add(new Interval(new DateTime(0).plus(Period.hours(6)).getMillis(),
//                new DateTime(0).plus(Period.hours(7)).getMillis(), DateTimeZone.UTC));
//
////        intervals.add (new Interval(Period.days(0).toStandardSeconds().getSeconds()*1000,
////                Period.days(1).toStandardSeconds().getSeconds()*1000, DateTimeZone.UTC));
////        intervals.add(new Interval(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeZone.UTC),
////                new DateTime(1970, 1, 1, 23, 59, 59, DateTimeZone.UTC)));
//
//        Rule r = new Rule(new Action(ActivatorState.ENABLED, chAct),
//                intervals, new Period(Period.days(1), PeriodType.days()), true);
//
//        RuleActivator ruleAct = new RuleActivator(ActivatorState.DISABLED, r);
//        List<Interval> intervals1 = new ArrayList<>();
//        //from 6am to 7am
//        intervals1.add(new Interval(new DateTime(0).plus(Period.days(0)).getMillis(),
//                new DateTime(0).plus(Period.days(1)).getMillis(), DateTimeZone.UTC));
//
//        Rule r1 = new Rule(new Action(ActivatorState.ENABLED, ruleAct),
//                intervals1, new Period(Period.months(1), PeriodType.months()), false);
//
//        r.update(new DateTime(2016, 3, 1, 10, 0, 0));
//        assertEquals(r.getOutputState(), RuleState.ACTIVE);
//        r.update(new DateTime(2016, 3, 1, 23, 50, 0));
//        assertEquals(r.getOutputState(), RuleState.ACTIVE);
//        r.update(new DateTime(2016, 3, 2, 0, 0, 1));
//        assertEquals(r.getOutputState(), RuleState.INACTIVE);
//        r.update(new DateTime(2016, 3, 7, 12, 0, 1));
//        assertEquals(r.getOutputState(), RuleState.ACTIVE);
//        r.update(new DateTime(2016, 3, 8, 12, 0, 1));
//        assertEquals(r.getOutputState(), RuleState.INACTIVE);
//    }

}