package TimerDescriptionLanguage;

import customwidgets.GraphPoint;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 19/12/16.
 */
public class EndToEndTests {
    @BeforeMethod
    public void setUp() throws Exception {
        Rules.resetInstance();
        Channels.getInstance().allOff();
        SimulatedEvents.resetInstance();
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    public void assertStateAtTimeEquals(List<GraphPoint> points, int index, DateTime expectedTime, ChannelState expectedState) {
        GraphPoint point = points.get(index);
        if (!expectedTime.equals(point.getDateTime()) || !expectedState.equals(point.getState())) {
            fail("Fail on point " + index + ".  \n" +
                    "Time was: " + point.getDateTime().toString() + "\n" +
                    "Expected: " + expectedTime.toString() + "\n" +
                    "State was: " + point.getState().toString() + "\n" +
                    "Expected : " + expectedState.toString() + "\n");
        }
    }

    public void assertStateAtTimeToggled(List<GraphPoint> points, int index, DateTime expectedTime) {
        assert index > 0;
        ChannelState nextState = points.get(index-1).getState();
        if (nextState == ChannelState.ENABLED) nextState = ChannelState.DISABLED; else nextState = ChannelState.ENABLED;
        assertStateAtTimeEquals(points, index, expectedTime, nextState);
    }

    @Test
    public void testOneHourEveryTwoHours()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parse("A: Enable channel 1 for one hour every two hours");
        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.hours(10);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime = deploymentTime;

        assertStateAtTimeEquals(points, i, testTime, ChannelState.ENABLED);

        while (testTime.isBefore(deploymentTime.plus(graphPeriod))) {
            testTime = testTime.plus(Period.hours(1));
            i++;
            assertStateAtTimeToggled(points, i, testTime);
        }
    }

    @Test
    public void testOneHourBetweenNineAndFiveEveryDay()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parse("A: Enable channel 1 between 9am and 5pm every day");
        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.days(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime = deploymentTime;

        DateTime dayStart = deploymentTime;
        assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

        while (testTime.isBefore(deploymentTime.plus(graphPeriod))) {
            testTime = dayStart.plusHours(9);  //9am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) return;

            testTime = dayStart.plusHours(17); //5pm
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

            dayStart = dayStart.plusDays(1);
        }
    }

    @Test
    public void testOnHourOneFiveSixEightEveryTwoDays()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parse("A: Enable channel 1 on hour 1, 5, 6, 8 every two days");
        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.days(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime = deploymentTime;

        DateTime periodStart = deploymentTime;
        assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

        while (testTime.isBefore(deploymentTime.plus(graphPeriod))) {
            testTime = periodStart.plusHours(1);  //1am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) return;

            testTime = periodStart.plusHours(2); //2am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
            if (i >= points.size()) return;

            testTime = periodStart.plusHours(5); //5am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) return;

            testTime = periodStart.plusHours(7); //7am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
            if (i >= points.size()) return;

            testTime = periodStart.plusHours(8); //8am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) return;

            testTime = periodStart.plusHours(9); //9am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

            periodStart = periodStart.plusDays(2);
        }
    }

    @Test
    public void testTwoLevelRule()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parseMultiple("a: Enable channel 1 for one hour every eight hours\n" +
                                 "B: Enable rule a on the 3rd day of every month");

        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.months(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime = deploymentTime;

        DateTime periodStart = deploymentTime;
        assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

        while (testTime.isBefore(deploymentTime.plus(graphPeriod))) {
            DateTime dayStart = periodStart.plusDays(2); //the 3rd

            testTime = dayStart.plusHours(0);  //0am - midnight
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) break;

            testTime = dayStart.plusHours(1); //1am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
            if (i >= points.size()) break;

            testTime = dayStart.plusHours(8); //8am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) break;

            testTime = dayStart.plusHours(9); //9am
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
            if (i >= points.size()) break;

            testTime = dayStart.plusHours(16); //4pm
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
            if (i >= points.size()) break;

            testTime = dayStart.plusHours(17); //5pm
            assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);


            periodStart = periodStart.plusMonths(1);
        }
    }

    @Test
    public void testThreeLevelRule()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parseMultiple("a: Enable channel 1 for one hour every eight hours\n" +
                                 "b: Enable rule a on the 3rd day of every month\n" +
                                 "C: Enable rule b for one year every second year");

        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.years(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime = deploymentTime;

        DateTime yearPeriodStart = deploymentTime;
        DateTime monthPeriodStart = yearPeriodStart;
        assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

        while (yearPeriodStart.isBefore(deploymentTime.plus(graphPeriod))) {
            monthPeriodStart = yearPeriodStart;
            while (monthPeriodStart.isBefore(yearPeriodStart.plusYears(1))) {

                DateTime dayStart = monthPeriodStart.plusDays(2); //the 3rd

                testTime = dayStart.plusHours(0);  //0am - midnight
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
                if (i >= points.size()) break;

                testTime = dayStart.plusHours(1); //1am
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
                if (i >= points.size()) break;

                testTime = dayStart.plusHours(8); //8am
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
                if (i >= points.size()) break;

                testTime = dayStart.plusHours(9); //9am
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);
                if (i >= points.size()) break;

                testTime = dayStart.plusHours(16); //4pm
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.ENABLED);
                if (i >= points.size()) break;

                testTime = dayStart.plusHours(17); //5pm
                assertStateAtTimeEquals(points, i++, testTime, ChannelState.DISABLED);

                monthPeriodStart = monthPeriodStart.plusMonths(1);
            }
            //skip forward a year
            yearPeriodStart = monthPeriodStart.plusYears(1);
        }
    }

    @Test
    public void testRuleOneHourAfterEvent()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parseMultiple("enable channel 1 for one hour starting 1 hour after event event1");

        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 1, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 10, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 3, 1, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event77", //different event - shouldn't be in results
                new DateTime(2017, 2, 1, 0, 0, 0)));

        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.years(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        assertStateAtTimeEquals(points, i++,
                deploymentTime,
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 10, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 10, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 3, 1, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 3, 1, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(1000000, 1, 1, 0, 0, 0),
                ChannelState.ENABLED);
    }

    @Test
    public void testRuleWithPeriodOneHourAfterEvent()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parseMultiple("enable channel 1 for one hour every seven hours starting 1 hour after event event1");

        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 1, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 2, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 3, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event77", //different event - shouldn't be in results
                new DateTime(2017, 2, 1, 0, 0, 0)));

        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.years(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        assertStateAtTimeEquals(points, i++,
                deploymentTime,
                ChannelState.DISABLED);


        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 8, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 9, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 15, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 16, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 22, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 1, 23, 0, 0),
                ChannelState.DISABLED);


        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 8, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 9, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 15, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 16, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 22, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 2, 23, 0, 0),
                ChannelState.DISABLED);


        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 1, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 2, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 8, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 9, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 15, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 16, 0, 0),
                ChannelState.DISABLED);

        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 22, 0, 0),
                ChannelState.ENABLED);
        assertStateAtTimeEquals(points, i++,
                new DateTime(2017, 1, 3, 23, 0, 0),
                ChannelState.DISABLED);

    }

    @Test
    public void testRuleWithTwoLevelEvent()
            throws InvalidSyntaxException, Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        RuleParser.parseMultiple("b: enable channel 1 for one minute every 2 minutes\n" +
                "A: enable rule b for one hour starting 1 hour after event event1");

        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 1, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 2, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 5, 10, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event77", //different event - shouldn't be in results
                new DateTime(2017, 2, 1, 0, 0, 0)));

        DateTime deploymentTime = new DateTime(2016, 1, 1, 0, 0);
        Period graphPeriod = Period.years(3);

        List<List<GraphPoint>> pointsLists =
                RuleRunner.generateGraphPoints(deploymentTime,
                        new Interval(deploymentTime, deploymentTime.plus(graphPeriod)));

        List<GraphPoint> points = pointsLists.get(0);

        int i = 0;
        DateTime testTime;

        assertStateAtTimeEquals(points, i++,
                deploymentTime,
                ChannelState.DISABLED);

        testTime = new DateTime(2017, 1, 1, 1, 0, 0);
        while (testTime.isBefore(new DateTime(2017, 1, 1, 2, 0, 0))) {
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.ENABLED);
            testTime = testTime.plusMinutes(1);
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.DISABLED);
            testTime = testTime.plusMinutes(1);
        }

        testTime = new DateTime(2017, 1, 2, 1, 0, 0);
        while (testTime.isBefore(new DateTime(2017, 1, 2, 2, 0, 0))) {
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.ENABLED);
            testTime = testTime.plusMinutes(1);
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.DISABLED);
            testTime = testTime.plusMinutes(1);
        }

        testTime = new DateTime(2017, 1, 5, 11, 0, 0);
        while (testTime.isBefore(new DateTime(2017, 1, 5, 12, 0, 0))) {
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.ENABLED);
            testTime = testTime.plusMinutes(1);
            assertStateAtTimeEquals(points, i++,
                    testTime,
                    ChannelState.DISABLED);
            testTime = testTime.plusMinutes(1);
        }

        assertStateAtTimeEquals(points, i++,
                new DateTime(1000000, 1, 1, 0, 0, 0),
                ChannelState.ENABLED);
    }
}
