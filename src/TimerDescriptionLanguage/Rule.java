package TimerDescriptionLanguage;

import org.joda.time.*;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mcochrane on 13/11/16.
 */
public class Rule {
    private int id = -1;
    private String name;
    private Action action;
    private List<PeriodInterval> intervals;
    private Period period;

    private boolean enabled; //If this is false, we don't update the rule
    private boolean enabledOnStart;

    private DateTime startOfFirstPeriod;
    private Period startOfFirstPeriodEventDelay = null;
    private String startOfFirstPeriodEventName = null;

    boolean hasBeenRunThisStep = false;

    //Internal Time Keeping Variables
    private List<Interval> currentIntervals = new ArrayList<>();
    private DateTime startOfCurrentPeriod = null;
    private DateTime lastUpdateTime = null;

    public class InvalidIntervalException extends Exception {};

    public Rule(String name, Action action, List<PeriodInterval> intervals,
                Period period, boolean enabled)
            throws InvalidIntervalException, Rules.RuleAlreadyExists {
        this(name, action, intervals, period, enabled, null);
    }

    public Rule(String name, Action action, List<PeriodInterval> intervals,
                Period period, boolean enabled, DateTime startOfFirstPeriod)
            throws InvalidIntervalException, Rules.RuleAlreadyExists {
        this.action = action;
        this.intervals = intervals;
        this.period = period;
        this.enabledOnStart = enabled;
        this.enabled = enabled;
        this.name = name;
        this.startOfFirstPeriod = startOfFirstPeriod;

        //checkIntervalsValid();

        //Creating a rule automatically adds it to the Rules singleton.
        id = Rules.getInstance().count();
        Rules.getInstance().add(this.name, this);
    }

    public Rule(String name, Action action, List<PeriodInterval> intervals,
                Period period, boolean enabled, String eventName, Period eventDelay)
            throws InvalidIntervalException, Rules.RuleAlreadyExists {
        this.action = action;
        this.intervals = intervals;
        this.period = period;
        this.enabledOnStart = enabled;
        this.enabled = enabled;
        this.name = name;
        this.startOfFirstPeriod = null;
        this.startOfFirstPeriodEventName = eventName;
        this.startOfFirstPeriodEventDelay = eventDelay;

        //checkIntervalsValid();

        //Creating a rule automatically adds it to the Rules singleton.
        id = Rules.getInstance().count();
        Rules.getInstance().add(this.name, this);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void enable() {
        this.enabled = true;
        resetInternalTimeKeepingVariables(); //reset and they'll be updated next time we call update;
    }

    public void disable() {
        this.enabled = false;
        action.stop(new DateTime());
    }

    public int getId() {
        return id;
    }

    public RuleState getOutputState() {
        return action.getState();
    }

    public String getName() {
        return name;
    }

    public DateTime getLastStateChangeTime() {
        //getStartOfNextPeriodForEvent(now)
        return getLastStateChangeTimeNoEvents();
    }

    private DateTime getLastStateChangeTimeNoEvents() {
        if (intervals.isEmpty()) return null;

        Interval currentInterval = getCurrentInterval();

        //If we're in a PeriodInterval, find when that period interval started.
        if (currentInterval != null) {
            //If that Interval started before the current Period then return the start of the Period.
            if (currentInterval.getStart().isBefore(startOfCurrentPeriod)) {
                return startOfCurrentPeriod;
            } else {
                return currentInterval.getStart();
            }
        }

        //If we're not in a PeriodInterval, return the end of the last PeriodInterval
        Interval lastInterval = getLastInterval();

        if (lastInterval != null) {
            //If that PeriodInterval starts before the current Period then return the start of the Period.
            if (lastInterval.getEnd().isBefore(startOfCurrentPeriod)) {
                return startOfCurrentPeriod;
            } else {
                return lastInterval.getEnd();
            }
        }

        //Bonus: If we're in a PeriodInterval, and any PeriodInterval starts before this PeriodInterval
        //       ends, then recurse this statement until we're in a PeriodInterval that doesn't have
        //       another one starting within it and return the end of that PeriodInterval.

        //Otherwise it doesn't change during this period.  It may change in a later period though so
        //return the end of the current period.
        return startOfCurrentPeriod;
    }

    public DateTime getNextStateChangeTime() {
        DateTime retVal = getNextStateChangeTimeNoEvents();
        //if (retVal == null) return null;

        DateTime eventStart = null;
        if (isTriggeredByEvent()) {
            eventStart = getStartOfNextPeriodForEvent(lastUpdateTime);
        }

        if (eventStart != null && retVal == null) return eventStart;
        if (eventStart != null && eventStart.isBefore(retVal)) return eventStart;

        return retVal;
    }

    private DateTime getNextStateChangeTimeNoEvents() {
        if (intervals.isEmpty()) return null;

        Interval currentInterval = getCurrentInterval();

        //If we're in a PeriodInterval, find when that period interval ends.
        if (currentInterval != null) {
            //If that PeriodInterval ends after the current Period then return the end of the Period.
            if (currentInterval.getEnd().isAfter(getEndOfCurrentPeriod())) {
                return getEndOfCurrentPeriod();
            } else {
                return currentInterval.getEnd();
            }
        }

        //If we're not in a PeriodInterval, return the start of the next PeriodInterval
        Interval nextInterval = getNextInterval();

        if (nextInterval != null) {
            //If that PeriodInterval ends after the current Period then return the end of the Period.
            if (nextInterval.getStart().isAfter(getEndOfCurrentPeriod())) {
                return getEndOfCurrentPeriod();
            } else {
                return nextInterval.getStart();
            }
        }

        //Bonus: If we're in a PeriodInterval, and any PeriodInterval starts before this PeriodInterval
        //       ends, then recurse this statement until we're in a PeriodInterval that doesn't have
        //       another one starting within it and return the end of that PeriodInterval.

        //Otherwise it doesn't change during this period.  It may change in a later period though so
        //return the end of the current period.
        return getEndOfCurrentPeriod();
    }

    //Get the interval that starts next (closest start time to lastUpdateTime)
    private Interval getNextInterval() {
        long minDistanceToInterval = Long.MAX_VALUE;
        Interval closestInterval = null;
        //Find next interval
        for (Interval interval : currentIntervals) {
            long distanceToInterval = interval.getStartMillis() - lastUpdateTime.getMillis();
            if (distanceToInterval < minDistanceToInterval && distanceToInterval > 0) {
                minDistanceToInterval = distanceToInterval;
                closestInterval = interval;
            }
        }
        return closestInterval;
    }

    //Get the interval that starts previous (closest start time to lastUpdateTime)
    private Interval getLastInterval() {
        long minDistanceToInterval = Long.MAX_VALUE;
        Interval closestInterval = null;
        //Find next interval
        for (Interval interval : currentIntervals) {
            long distanceToInterval = lastUpdateTime.getMillis() - interval.getEndMillis();
            if (distanceToInterval < minDistanceToInterval && distanceToInterval > 0) {
                minDistanceToInterval = distanceToInterval;
                closestInterval = interval;
            }
        }
        return closestInterval;
    }

    private Interval getCurrentInterval() {
        for (Interval interval : currentIntervals) {
            //Check if now is between calculated start/stop.
            if ((lastUpdateTime.isAfter(interval.getStart()) || lastUpdateTime.equals(interval.getStart()))
                    && lastUpdateTime.isBefore(interval.getEnd())) {
                return interval;
            }
        }
        return null;
    }

    private void runParentRulesIfNecessary(DateTime now) {
        List<Rule> parentRules = findParentRules();
        for (Rule r : parentRules) {
            if (!r.hasBeenRunThisStep) r.update(now);
        }
    }

    private List<Rule> findParentRules() {
        List<Rule> retList = new ArrayList<>();
        for (Rule r : Rules.getInstance().getRules().values()) {
            if (r.getAction().getActivator() instanceof RuleActivator) {
                Rule rule = ((RuleActivator)r.getAction().getActivator()).getRule();
                if (rule == this) retList.add(r);
            }
        }
        return retList;
    }

    public void update(DateTime now) {
        if (isEnabled()) {
            runParentRulesIfNecessary(now);
            updateThisRuleOnly(now);
        }
    }

    public void updateThisRuleAndParentsIfNecessary(DateTime now) {
        runParentRulesIfNecessary(now);
        updateThisRuleOnly(now);
    }

    public void updateThisRuleOnly(DateTime now) {
        boolean isActive = false;
        updateInternalTimeKeepingVariables(now);
        for (Interval interval : currentIntervals) {
            //Check if now is between calculated start/stop.
            if ((now.isAfter(interval.getStart()) || now.equals(interval.getStart()))
                    && now.isBefore(interval.getEnd())) {
                isActive = true;
                break;
            }
        }
        if (isActive) {
            action.start(now);
        } else {
            action.stop(now);
        }
        lastUpdateTime = now;
        hasBeenRunThisStep = true;
    }

    public void reset() {
        resetInternalTimeKeepingVariables();
        action.stop(new DateTime());
        enabled = enabledOnStart;
    }

//    private void checkIntervalsValid() throws InvalidIntervalException {
//        for (PeriodInterval interval : intervals) {
//            intervalFitsInPeriod(interval);
//        }
//    }

    private void updateCurrentPeriodIntervals() {
        currentIntervals = new ArrayList<>();
        for (PeriodInterval periodInterval : intervals) {
            currentIntervals.add(periodInterval.toInterval(startOfCurrentPeriod));
        }
    }

    private void resetInternalTimeKeepingVariables() {
        startOfCurrentPeriod = null;
        currentIntervals = new ArrayList<>();
        lastUpdateTime = null;
    }

    private DateTime getEndOfCurrentPeriod() {
        if (startOfCurrentPeriod == null) return null;
        return startOfCurrentPeriod.plus(period);
    }

    private void updateInternalTimeKeepingVariables(DateTime now) {
        if (startOfCurrentPeriod == null) {
            updateInitialStartOfCurrentPeriod(now);
            if (startOfCurrentPeriod != null)
                updateCurrentPeriodIntervals();
        } else {
            if (isTriggeredByEvent()) {
                DateTime startOfNext;

                startOfNext = getStartOfNextPeriodForEvent(lastUpdateTime);
                while (now.isAfter(startOfNext) || now.isEqual(startOfNext)) {
                    startOfCurrentPeriod = startOfNext;
                    if (startOfNext.getYear() >= 1000000) break;
                    startOfNext = getStartOfNextPeriodForEvent(startOfNext);
                }
                updateCurrentPeriodIntervals();
            }
            DateTime endOfCurrentPeriod = getEndOfCurrentPeriod();
            if ( startOfFirstPeriod != null && now.isBefore(startOfFirstPeriod)) {
                action.stop(now);
            } else if (now.isBefore(startOfCurrentPeriod)) {
                //should this really be possible???
                //if (startOfCurrentPeriod.getYear() >= 1000000) return;
                //TODO: handle this!
                throw new Error("TODO: handle this!");
                //updateCurrentPeriodIntervals
            } else if (now.isAfter(endOfCurrentPeriod) || now.equals(endOfCurrentPeriod)) {
                //TODO: break out/abort if it's taking too long...
                while (now.isAfter(endOfCurrentPeriod) || now.equals(endOfCurrentPeriod)) {
                    startOfCurrentPeriod = endOfCurrentPeriod;
                    endOfCurrentPeriod = startOfCurrentPeriod.plus(period);
                }
                updateCurrentPeriodIntervals();
//            } else {
//                startOfCurrentPeriod = getStartOfNextPeriodForEvent(now);
//                updateCurrentPeriodIntervals();
            }
        }
    }

    private DateTime getStartOfNextPeriodForEvent(DateTime now) {
        //find next event
        DateTime nextEventTime = findNextInList(now, SimulatedEvents.getInstance().findEventTimes(startOfFirstPeriodEventName));

        if (nextEventTime == null) {
            return new DateTime(1000000, 1, 1, 0, 0, 0); //year 1000000
        }

        if (startOfFirstPeriodEventDelay != null) {
            return nextEventTime.plus(startOfFirstPeriodEventDelay);
        }

        return nextEventTime;
    }

    private DateTime getStartOfPreviousPeriodForEvent(DateTime now) {
        //find next event
        DateTime previousEventTime = findPreviousInList(now, SimulatedEvents.getInstance().findEventTimes(startOfFirstPeriodEventName));

        if (previousEventTime == null) {
            return null;//new DateTime(-1000000, 1, 1, 0, 0, 0); //year -1000000
        }

        if (startOfFirstPeriodEventDelay != null) {
            return previousEventTime.plus(startOfFirstPeriodEventDelay);
        }

        return previousEventTime;
    }

    //if find closest to now in the future
    private DateTime findNextInList(DateTime now, List<DateTime> times) {
        Collections.sort(times);
        long bestDist = Long.MAX_VALUE;
        DateTime bestTime = null;

        for (DateTime t : times) {
            long dist = t.getMillis() - now.getMillis();
            if (dist < bestDist && dist > 0) {
                bestDist = dist;
                bestTime = t;
            }
        }

        return bestTime;
    }

    //if find closest to now in the past
    private DateTime findPreviousInList(DateTime now, List<DateTime> times) {
        Collections.sort(times);
        long bestDist = Long.MIN_VALUE;
        DateTime bestTime = null;

        for (DateTime t : times) {
            long dist = t.getMillis() - now.getMillis();
            if (dist > bestDist && dist < 0) {
                bestDist = dist;
                bestTime = t;
            }
        }

        return bestTime;
    }

    private void updateInitialStartOfCurrentPeriod(DateTime now) {
        if (startOfFirstPeriod != null) {
            startOfCurrentPeriod = startOfFirstPeriod;
            action.stop(new DateTime());
            return;
        }

        if (isTriggeredByEvent()) {
            startOfCurrentPeriod = getStartOfPreviousPeriodForEvent(now);
            return;
        }

        if (period.equals(TimeHelper.infinitePeriod())) {
            startOfCurrentPeriod = now;
            //startOfFirstPeriod = new DateTime(startOfCurrentPeriod);
            return;
        }

        DurationFieldType dft = TimeHelper.getLongestDurationFieldType(period);
        TimeHelper.makePeriodCustom(1, dft);

        if (dft == DurationFieldType.years()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), 1, 1, 0, 0, 0);
        } else if (dft == DurationFieldType.months()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(), 1, 0, 0, 0);
        } else if (dft == DurationFieldType.weeks()) {
            startOfCurrentPeriod = now.minusSeconds(now.getSecondOfDay() + (now.getDayOfWeek() - 1) * 24 * 60 * 60);
        } else if (dft == DurationFieldType.days()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), 0, 0, 0);
        } else if (dft == DurationFieldType.hours()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), 0, 0);
        } else if (dft == DurationFieldType.minutes()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(), 0);
        } else if (dft == DurationFieldType.seconds()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(),
                    now.getSecondOfMinute());
        } else {
            startOfCurrentPeriod = now;
        }
        //startOfFirstPeriod = new DateTime(startOfCurrentPeriod);
    }

    //Compile
    public List<UByte> compile() {
        List <UByte> compiledRule = new ArrayList<>();
        compiledRule.addAll(action.compile());
        compiledRule.addAll(PeriodCompilier.compilePeriod(period));
        compiledRule.addAll(compileIntervals());


        return compiledRule;
    }

    private List<UByte> compileIntervals() {
        List<UByte> compiledIntervalsList = new ArrayList<>();

        compiledIntervalsList.add(UByte.valueOf(intervals.size()));
        for (PeriodInterval i : intervals) {
            compiledIntervalsList.addAll(i.compile());
        }

        return compiledIntervalsList;
    }

    //Package level access:
    Action getAction() {
        return action;
    }

    List<PeriodInterval> getIntervals() {
        return intervals;
    }

    Period getPeriod() {
        return period;
    }

    DateTime getStartOfFirstPeriod() { return startOfFirstPeriod; }

    boolean isTriggeredByEvent() {
        return startOfFirstPeriodEventName != null && !startOfFirstPeriodEventName.isEmpty();
    }

    String getStartOfFirstPeriodEventName() {
        return startOfFirstPeriodEventName;
    }

    Period getStartOfFirstPeriodEventDelay() {
        return startOfFirstPeriodEventDelay;
    }


}
