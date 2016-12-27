package TimerDescriptionLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import customwidgets.GraphPoint;
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
        setAllRulesNotRunThisStep();
        for (Rule r : getAllRuleList()) {
            r.update(now);
        }
        RuleRunner.currentGlobalTime = now;
    }

    /**
     * Generate all graph points in a time interval.
     * @param window
     */
    public static List<List<GraphPoint>> generateGraphPoints(DateTime startingTime, Interval window) {
        List<List<GraphPoint>> pointsList = new ArrayList<>();

        for (int i = 0; i < Channels.getInstance().getCount(); i++) {
            pointsList.add(new ArrayList<>());
        }
        for (Rule r : Rules.getInstance().getRules().values()) {
            pointsList.add(new ArrayList<>());
        }

        RuleRunner.startGraph(startingTime, window.getStart());
        addCurrentGraphPointToList(pointsList, true);
        do {
            step();
            addCurrentGraphPointToList(pointsList, false);
        } while (getNow().isBefore(window.getEnd()));

        //Add one more so we fill the full graph.
        step();
        addCurrentGraphPointToList(pointsList, true);

        return pointsList;
    }

    private static boolean addCurrentGraphPointToList(List<List<GraphPoint>> pointsList, boolean forceAdd) {
        boolean addedOneOrMorePoints = false;
        int channelCount = Channels.getInstance().getCount();

        // Add Channels
        for (int i = 0; i < channelCount; i++) {
            List<GraphPoint> channelPoints = pointsList.get(i);
            boolean added = addCurrentGraphPointToListForChannel(channelPoints, Channels.getInstance().get(i), forceAdd);
            if (added) addedOneOrMorePoints = true;
        }

        // Add Rules
        int ruleIndex = 0;
        for (Rule r : Rules.getInstance().getRules().values()) {
            List<GraphPoint> rulePoints = pointsList.get(channelCount + ruleIndex);
            boolean added = addCurrentGraphPointToListForRule(rulePoints, r, forceAdd);
            if (added) addedOneOrMorePoints = true;
            ruleIndex++;
        }

        printStatusToConsole(RuleRunner.currentGlobalTime);

        return addedOneOrMorePoints;
    }

    private static boolean addCurrentGraphPointToListForChannel(List<GraphPoint> channelPoints, Channel chan, boolean forceAdd) {
        ChannelState currentState = chan.getState();
        if (channelPoints.size() > 0 && !forceAdd) {
            ChannelState oldState = channelPoints.get(channelPoints.size() - 1).getState();
            if (oldState.equals(currentState)) return false;
        }
        GraphPoint newPoint = new GraphPoint(getNow(), currentState);
        channelPoints.add(newPoint);
        return true;
    }

    private static boolean addCurrentGraphPointToListForRule(List<GraphPoint> channelPoints, Rule rule, boolean forceAdd) {
        //RuleState currentState = rule.getOutputState();
        boolean currentState = rule.isEnabled();
        if (channelPoints.size() > 0 && !forceAdd) {
//            RuleState oldState = channelStateToRuleState(channelPoints.get(channelPoints.size() - 1).getState());
            boolean oldState = channelStateToBoolean(channelPoints.get(channelPoints.size() - 1).getState());
            //if (oldState.equals(currentState)) return false;
            if (oldState == currentState) return false;
        }
        GraphPoint newPoint = new GraphPoint(getNow(), currentState);
        channelPoints.add(newPoint);
        return true;
    }

    private static RuleState channelStateToRuleState(ChannelState state) {
        if (state.equals(ChannelState.DISABLED)) {
            return RuleState.INACTIVE;
        } else {
            return RuleState.ACTIVE;
        }
    }

    private static boolean channelStateToBoolean(ChannelState state) {
        if (state.equals(ChannelState.DISABLED)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Starts the rule-running simulation at the specified startingTime
     * @param startingTime the time that the rule-running simulation is started
     */
    public static void start(DateTime startingTime) {
        resetAll(startingTime);
        RuleRunner.startingTime = startingTime;
        stepTo(startingTime);
    }

    /**
     * Starts the rule-running simulation at the specified startingTime and
     * fast-forwards to the time specified by graphStart.
     * @param startingTime the simulation starting time
     * @param graphStart the time that the plotted graph starts, time is
     *                   fast-forwarded to this moment.
     */
    public static void startGraph(DateTime startingTime, DateTime graphStart) {
        start(startingTime);
        if (graphStart.isAfter(startingTime)) {
            fastForwardToGraphStart(graphStart);
        }
    }

    private static void fastForwardToGraphStart(DateTime now) {
        stepTo(now);
        DateTime calcStart = findOverallEarliestPreviousStateChangeTime();
        if (calcStart == null || calcStart.isBefore(RuleRunner.deploymentTime)) calcStart = deploymentTime;
        DateTime startingTime = RuleRunner.startingTime;
        resetAll(deploymentTime);
        start(startingTime);
        stepTo(calcStart);
    }

    private static void setAllRulesNotRunThisStep() {
        for (Rule r : Rules.getInstance().getRules().values()) {
            r.hasBeenRunThisStep = false;
        }
    }

    public static DateTime step() {
        DateTime nextStateChangeTime = findOverallNextStateChangeTime();

        setAllRulesNotRunThisStep();
        //Not working, need to run in the correct order for chains?
        //Ie, if a rule depends on another rule, you MUST run that rule before running it.
        //find rules
        for (Rule r : getAllRuleList()) {
            r.update(nextStateChangeTime);
        }

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

    private static List<Rule> getAllRuleList() {
        return new ArrayList<>(Rules.getInstance().getRules().values());
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

    public static DateTime findOverallNextStateChangeTime() {
        DateTime minNext = new DateTime(Long.MAX_VALUE);
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.isEnabled()) {
                DateTime next = r.getNextStateChangeTime();
                if (next == null) continue;
                if (next.isBefore(minNext)) {
                    minNext = next;
                }
            }
        }
        return minNext;
    }

    private static DateTime findOverallEarliestPreviousStateChangeTime() {
        DateTime minLast = new DateTime(Long.MAX_VALUE);
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.isEnabled()) {
                DateTime last = r.getLastStateChangeTime();
                if (last == null) continue;
                if (last.isBefore(minLast)) {
                    minLast = last;
                }
            }
        }
        if (minLast.equals(new DateTime(Long.MAX_VALUE))) return null;
        return minLast;
    }

    public static void printStatusToConsole(DateTime now) {
//        System.out.println(now.toString());
//        System.out.println("Channels:");
//        for (Map.Entry<String, Channel> entry : Channels.getInstance().getChannels().entrySet()) {
//            System.out.println("\t" + entry.getKey() + " " + (entry.getValue().getState()==ChannelState.ENABLED?"ENABLED":"DISABLED") );
//        }
//        System.out.println("Rules:");
//        for (Map.Entry<String, Rule> entry : Rules.getInstance().getRules().entrySet()) {
//            System.out.println("\t" + entry.getKey() + " " + (entry.getValue().isEnabled()?"ENABLED":"DISABLED") + ", " + (entry.getValue().getAction().getState()==RuleState.ACTIVE?"ACTIVE":"INACTIVE"));
//        }
//        System.out.println();
    }

}
