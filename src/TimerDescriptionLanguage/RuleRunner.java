package TimerDescriptionLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.*;

/**
 * Created by mcochrane on 14/11/16.
 */
public class RuleRunner {

    private static DateTime startingTime;
    private static DateTime currentGlobalTime;
    private static DateTime deploymentTime;

    private RuleRunner() {}

    public static void resetAll(DateTime deploymentTime) {
        for (Rule r : Rules.getInstance().getRules().values()) {
            r.reset();
        }
        startingTime = null;
        RuleRunner.deploymentTime = deploymentTime;
    }

    public static void disableAll() {
        for (Rule r : Rules.getInstance().getRules().values()) {
            r.disable();
        }
    }

    public static DateTime getNow() {
        return RuleRunner.currentGlobalTime;
    }

    public static void stepTo(DateTime now) {
        for (Rule r : getActiveRuleList()) {
            r.update(now);
        }
        RuleRunner.currentGlobalTime = now;
    }

    public static void start(DateTime startingTime) {
        RuleRunner.startingTime = startingTime;
        stepTo(startingTime);
    }

    public static void startGraph(DateTime now) {
        stepTo(now);
        DateTime calcStart = findOverallEarliestPreviousStateChangeTime();
        if (calcStart.isBefore(RuleRunner.deploymentTime)) calcStart = deploymentTime;
        DateTime startingTime = RuleRunner.startingTime;
        resetAll(deploymentTime);
        start(startingTime);
        stepTo(calcStart);
    }

    public static DateTime step() {
        DateTime nextStateChangeTime = findOverallNextStateChangeTime();

        List<Rule> activeRules;

        activeRules = getActiveRuleList();

        for (Rule r : activeRules) {
            r.update(nextStateChangeTime);
        }

//        List<Rule> changedRules;
//        changedRules = getChangedRuleList(activeRules);
//        activeRules = getActiveRuleList();
//
//        while (changedRules.size() > 0) {
//            for (Rule r : changedRules) {
//                if (r.isEnabled()) {
//                    r.update(nextStateChangeTime);
//                }
//            }
//        }
//
//        while () {
//            for (Rule r : Rules.getInstance().getRules().values()) {
//                if (r.isEnabled()) {
//                    r.update(nextStateChangeTime);
//                }
//            }
//        }

        RuleRunner.currentGlobalTime = nextStateChangeTime;

        return nextStateChangeTime;
    }

    private static List<Rule> getActiveRuleList() {
        List<Rule> activeRules = new ArrayList<>();
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.isEnabled()) {
                activeRules.add(r);
            }
        }
        return activeRules;
    }

    private static List<Rule> getChangedRuleList(List<Rule> previouslyActiveRules) {
        List<Rule> changedRules = new ArrayList<>();
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (previouslyActiveRules.contains(r)) {
                if (!r.isEnabled()) changedRules.add(r);
            } else {
                if (r.isEnabled()) changedRules.add(r);
            }
        }
        return changedRules;
    }

    private static DateTime findOverallNextStateChangeTime() {
        DateTime minNext = new DateTime(Long.MAX_VALUE);
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.isEnabled()) {
                DateTime next = r.getNextStateChangeTime();
                if (next.isBefore(minNext)) {
                    minNext = next;
                }
            }
        }
        return minNext;
    }

    private static DateTime findOverallEarliestPreviousStateChangeTime() {
        DateTime minNext = new DateTime(Long.MAX_VALUE);
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.isEnabled()) {
                DateTime next = r.getLastStateChangeTime();
                if (next.isBefore(minNext)) {
                    minNext = next;
                }
            }
        }
        return minNext;
    }

    public static void printStatusToConsole(DateTime now) {
        System.out.println(now.toString());
        System.out.println("Channels:");
        for (Map.Entry<String, Channel> entry : Channels.getInstance().getChannels().entrySet()) {
            System.out.println("\t" + entry.getKey() + " " + (entry.getValue().getState()==ChannelState.ENABLED?"ENABLED":"DISABLED") );
        }
        System.out.println("Rules:");
        for (Map.Entry<String, Rule> entry : Rules.getInstance().getRules().entrySet()) {
            System.out.println("\t" + entry.getKey() + " " + (entry.getValue().isEnabled()?"ENABLED":"DISABLED") + ", " + (entry.getValue().getAction().getState()==RuleState.ACTIVE?"ACTIVE":"INACTIVE"));
        }
        System.out.println();
    }

}
