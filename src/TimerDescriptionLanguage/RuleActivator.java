package TimerDescriptionLanguage;

import org.joda.time.DateTime;

/**
 * Created by mcochrane on 13/11/16.
 */
public class RuleActivator extends Activator {
    private Rule target;

    RuleActivator(Rule target, ActivatorState defaultState) {
        super(defaultState);
        this.target = target;
    }

    @Override
    public void enable(DateTime now) {
        super.enable(now);
        target.enable();
        target.updateThisRuleAndParentsIfNecessary(now);
    }

    @Override
    public void disable() {
        super.disable();
        target.disable();
    }

    Rule getRule() {
        return this.target;
    }
}
