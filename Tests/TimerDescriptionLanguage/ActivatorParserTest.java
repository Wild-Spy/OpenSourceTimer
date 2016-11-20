package TimerDescriptionLanguage;

import TimerDescriptionLanguage.*;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by mcochrane on 17/11/16.
 */
public class ActivatorParserTest {

    @Test
    public void testParseActivatorSimple() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Enable channel 1");

        assertTrue(act instanceof ChannelActivator);

        ChannelActivator actCh = (ChannelActivator) act;

        assertEquals(actCh.getChannel(), Channels.getInstance().get("1"));
        assertEquals(actCh.getState(), ActivatorState.DISABLED);
        assertEquals(actCh.getDefaultState(), ActivatorState.DISABLED);
    }

    @Test
    public void testParseActivatorDisable() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Disable channel 1");

        assertTrue(act instanceof ChannelActivator);

        ChannelActivator actCh = (ChannelActivator) act;

        assertEquals(actCh.getChannel(), Channels.getInstance().get("1"));
        assertEquals(actCh.getState(), ActivatorState.ENABLED);
        assertEquals(actCh.getDefaultState(), ActivatorState.ENABLED);
    }

    @Test
    public void testParseActivatorDifferentChannel() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Disable channel 3");

        assertTrue(act instanceof ChannelActivator);

        ChannelActivator actCh = (ChannelActivator) act;

        assertEquals(actCh.getChannel(), Channels.getInstance().get("3"));
        assertEquals(actCh.getState(), ActivatorState.ENABLED);
        assertEquals(actCh.getDefaultState(), ActivatorState.ENABLED);
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseActivatorTooLongThrowsExecption() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Disable channel 3 yes");
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseActivatorTooShortThrowsExecption() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Disable channel");
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseActivatorBadActivatorTypeThrowsExecption() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("Disable bogus 5");
    }

    @Test(expectedExceptions = InvalidSyntaxException.class)
    public void testParseActivatorBadVerbThrowsExecption() throws InvalidSyntaxException {
        Activator act = ActivatorParser.parse("bogus channel 5");
    }

    @Test
    public void testParseActivatorEnableRule() throws InvalidSyntaxException,
            Rules.RuleAlreadyExists, Rule.InvalidIntervalException {
        Rules.resetInstance();
        Rule r = new Rule("AAA", null, null, null, false); //This adds it to the rule list too
        Activator act = ActivatorParser.parse("Enable rule AAA");

        assertTrue(act instanceof RuleActivator);

        RuleActivator ruleActivator = (RuleActivator) act;

        assertEquals(ruleActivator.getRule(), r);
        assertEquals(ruleActivator.getState(), ActivatorState.DISABLED);
        assertEquals(ruleActivator.getDefaultState(), ActivatorState.DISABLED);
    }

}