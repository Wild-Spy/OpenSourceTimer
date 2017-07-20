package TimerDescriptionLanguage;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 18/11/16.
 */
public class RuleParserTest {
    @BeforeMethod
    public void setUp() throws Exception {
        Rules.resetInstance();
    }

    @Test
    public void testParseSimpleRule() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour every day");

        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        assertEquals(r.getAction().getActivatorState(), ActivatorState.DISABLED);
        assertEquals(r.getIntervals().size(), 1);
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.makePeriodDays(1));
    }

    @Test
    public void testParseRuleDefaultNames() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r1 = RuleParser.parse("enable channel 1 for one hour every day");
        Rule r2 = RuleParser.parse("enable channel 1 for one hour every day");
        Rule r3 = RuleParser.parse("enable channel 1 for one hour every day");
        Rule r4 = RuleParser.parse("enable channel 1 for one hour every day");

        assertEquals(r1.getName(), "Rule_1");
        assertEquals(r2.getName(), "Rule_2");
        assertEquals(r3.getName(), "Rule_3");
        assertEquals(r4.getName(), "Rule_4");
    }

    @Test
    public void testParseRuleAction() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("disable channel 4 for one hour every day");

        assertEquals(r.getOutputState(), RuleState.INACTIVE);
        assertEquals(r.getAction().getActivatorState(), ActivatorState.ENABLED);
        assertEquals(r.getAction().getActivator().getDefaultState(), ActivatorState.ENABLED);
        assertEquals(r.getAction().getActivatorStateWhenRunning(), ActivatorState.DISABLED);
        assertTrue(r.getAction().getActivator() instanceof ChannelActivator);
        ChannelActivator activator = (ChannelActivator) r.getAction().getActivator();
        assertEquals(activator.getChannel(), Channels.getInstance().get("4"));

        Rule r1 = RuleParser.parse("enable channel 2 for one hour every day");

        assertEquals(r1.getOutputState(), RuleState.INACTIVE);
        assertEquals(r1.getAction().getActivatorState(), ActivatorState.DISABLED);
        assertEquals(r1.getAction().getActivator().getDefaultState(), ActivatorState.DISABLED);
        assertEquals(r1.getAction().getActivatorStateWhenRunning(), ActivatorState.ENABLED);
        assertTrue(r1.getAction().getActivator() instanceof ChannelActivator);
        ChannelActivator activator1 = (ChannelActivator) r1.getAction().getActivator();
        assertEquals(activator1.getChannel(), Channels.getInstance().get("2"));
    }

    @Test
    public void testParseRuleIntervals() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour every day");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));

        Rule r1 = RuleParser.parse("enable channel 1 between second 10 and 20 every minute");
        assertEquals(r1.getIntervals().get(0), TimeHelper.betweenSeconds(10, 20).get(0));

        Rule r2 = RuleParser.parse("enable channel 1 on the 1st, 8th, 10th day of every month");
        assertEquals(r2.getIntervals().size(), 3);
        assertEquals(r2.getIntervals().get(0), TimeHelper.onDays(1, 8, 10).get(0));
        assertEquals(r2.getIntervals().get(1), TimeHelper.onDays(1, 8, 10).get(1));
        assertEquals(r2.getIntervals().get(2), TimeHelper.onDays(1, 8, 10).get(2));
    }

    @Test
    public void testParseRulePeriod() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour every 2 days");
        assertEquals(r.getPeriod(), TimeHelper.makePeriodDays(2));

        Rule r1 = RuleParser.parse("enable channel 1 between the 10th and 20th second every minute");
        assertEquals(r1.getPeriod(), TimeHelper.makePeriodMinutes(1));

        Rule r2 = RuleParser.parse("enable channel 1 on the 1st, 8th, 10th day of every third month");
        assertEquals(r2.getPeriod(), TimeHelper.makePeriodMonths(3));
    }

    @Test
    public void testMultipleWhitespace() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable    channel   1  for one hour            every 2 days");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.makePeriodDays(2));

        Rule r1 = RuleParser.parse("enable channel 1      for one        hour every 2        days");
        assertEquals(r1.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r1.getPeriod(), TimeHelper.makePeriodDays(2));
    }

    @Test
    public void testNameARule() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("CUSTOM_RULE_NAME: enable channel 1 for one hour every 2 hours");
        assertEquals(r.getName(), "CUSTOM_RULE_NAME");
    }

    @Test
    public void parseMultipleRules()  throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        List<Rule> r = RuleParser.parseMultiple("enable rule AAA on the 1st, 5th, 10th, 11th, 12th, 15th day of every month\r\nAAA: enable channel 1 for 1 hour every 2 hours");
        assertEquals(r.size(), 2);
        assertEquals(r.get(1).getName(), "AAA");
    }

    @Test
    public void testParseInfiniteRule() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.infinitePeriod());
    }

    @Test
    public void testParseInfiniteRuleWithStart() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour starting at 1am on 3/3/2017");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.infinitePeriod());
        assertEquals(r.getStartOfFirstPeriod(), new DateTime(2017, 3, 3, 1, 0, 0));
    }

    @Test
    public void testParseRuleWithStart() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour every two hours starting at 1am on 3/3/2017");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.makePeriodHours(2));
        assertEquals(r.getStartOfFirstPeriod(), new DateTime(2017, 3, 3, 1, 0, 0));
    }

    @Test
    public void testParseRuleWithStartAfterEvent() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour starting 1 hour after event event1");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.infinitePeriod());
        assertEquals(r.getStartOfFirstPeriod(), null);
        assertEquals(r.getStartOfFirstPeriodEventName(), "event1");
        assertEquals(r.getStartOfFirstPeriodEventDelay(), new Period(0, 0, 0, 0, 1, 0, 0, 0)); //1 hour
    }

    @Test
    public void testParseRuleWithStartOnEvent() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        Rule r = RuleParser.parse("enable channel 1 for one hour starting on event event1");
        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(0, 1).get(0));
        assertEquals(r.getPeriod(), TimeHelper.infinitePeriod());
        assertEquals(r.getStartOfFirstPeriod(), null);
        assertEquals(r.getStartOfFirstPeriodEventName(), "event1");
        assertEquals(r.getStartOfFirstPeriodEventDelay(), null); //1 hour
    }

    @Test
    public void testParseRulesWithInvalidSyntax() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "enable channel 1 for one hour starting");
            assertEquals(e.getParseExceptionIndex(), 38);
            assertEquals(e.getMessage(), "Expected a 'starting at [date/time]', 'starting on event [event name]' or 'starting [time] after event [event name]");
        }

        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting at chicken");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "chicken");
            assertEquals(e.getParseExceptionIndex(), 0);
            assertEquals(e.getMessage(), "Invalid date.");
        }

        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting event");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "event");
            assertEquals(e.getParseExceptionIndex(), 0);
            assertEquals(e.getMessage(), "Cannot parse event name.");
        }

        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting carrot after event event1");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "carrot");
            assertEquals(e.getParseExceptionIndex(), 0);
            assertEquals(e.getMessage(), "Invalid syntax for period.");
        }

        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting carrot event event1");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "carrot event event1");
            assertEquals(e.getParseExceptionIndex(), 19);
            assertEquals(e.getMessage(), "Expected the word 'after' before event keyword, eg. 'starting 1 hour *after* event'.");
        }

        try {
            Rule r = RuleParser.parse("enable channel 1 for one hour starting 5 hours event event1");
            fail("Should have thrown exception");
        } catch (InvalidSyntaxException e) {
            assertEquals(e.getCode(), "5 hours event event1");
            assertEquals(e.getParseExceptionIndex(), 20);
            assertEquals(e.getMessage(), "Expected the word 'after' before event keyword, eg. 'starting 1 hour *after* event'.");
        }
    }

//    @Test
//    public void testParseEverySecondHour() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
//        Rule r = RuleParser.parse("enable channel 1 every second hour");
//        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(1, 2).get(0));
//        assertEquals(r.getPeriod(), TimeHelper.makePeriodHours(2));
//    }
//
//    @Test
//    public void testParseEveryOtherHour() throws InvalidSyntaxException, Rule.InvalidIntervalException, Rules.RuleAlreadyExists {
//        Rule r = RuleParser.parse("enable channel 1 every other day");
//        assertEquals(r.getIntervals().get(0), TimeHelper.betweenHours(1, 2).get(0));
//        assertEquals(r.getPeriod(), TimeHelper.makePeriodHours(2));
//    }

}