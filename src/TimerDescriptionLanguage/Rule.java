package TimerDescriptionLanguage;

import org.joda.time.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 13/11/16.
 */
public class Rule {
    private String name;
    private Action action;
    private List<PeriodInterval> intervals;
    private Period period;
    private boolean enabled;
    //Internal Time Keeping Variables
    private List<Interval> currentPeriodIntervals = null;
    private DateTime startOfCurrentPeriod = null;
    private DateTime lastUpdateTime = null;

    public class InvalidIntervalException extends Exception {};

    public Rule(String name, Action action, List<PeriodInterval> intervals, Period period, boolean enabled)
            throws InvalidIntervalException, Rules.RuleAlreadyExists {
        this.action = action;
        this.intervals = intervals;
        this.period = period;
        this.enabled = enabled;
        this.name = name;

        //checkIntervalsValid();

        //Creating a rule automatically adds it to the Rules singleton.
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

    public RuleState getOutputState() {
        return action.getState();
    }

    public String getName() {
        return name;
    }

    public DateTime getLastStateChangeTime() {
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
        for (Interval interval : currentPeriodIntervals) {
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
        for (Interval interval : currentPeriodIntervals) {
            long distanceToInterval = lastUpdateTime.getMillis() - interval.getEndMillis();
            if (distanceToInterval < minDistanceToInterval && distanceToInterval > 0) {
                minDistanceToInterval = distanceToInterval;
                closestInterval = interval;
            }
        }
        return closestInterval;
    }

    private Interval getCurrentInterval() {
        for (Interval interval : currentPeriodIntervals) {
            //Check if now is between calculated start/stop.
            if ((lastUpdateTime.isAfter(interval.getStart()) || lastUpdateTime.equals(interval.getStart()))
                    && lastUpdateTime.isBefore(interval.getEnd())) {
                return interval;
            }
        }
        return null;
    }

    public void update(DateTime now) {
        boolean isActive = false;
        updateInternalTimeKeepingVariables(now);
        for (Interval interval : currentPeriodIntervals) {
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
    }

    public void reset() {
        resetInternalTimeKeepingVariables();
        action.stop(new DateTime());
    }

//    private void checkIntervalsValid() throws InvalidIntervalException {
//        for (PeriodInterval interval : intervals) {
//            intervalFitsInPeriod(interval);
//        }
//    }

    private void updateCurrentPeriodIntervals() {
        currentPeriodIntervals = new ArrayList<>();
        for (PeriodInterval periodInterval : intervals) {
            currentPeriodIntervals.add(periodInterval.toInterval(startOfCurrentPeriod));
        }
    }

    private void resetInternalTimeKeepingVariables() {
        startOfCurrentPeriod = null;
        currentPeriodIntervals = null;
        lastUpdateTime = null;
    }

    private DateTime getEndOfCurrentPeriod() {
        return startOfCurrentPeriod.plus(period);
    }

    private void updateInternalTimeKeepingVariables(DateTime now) {
        if (startOfCurrentPeriod == null) {
            updateInitialStartOfCurrentPeriod(now);
            updateCurrentPeriodIntervals();
        } else {
            DateTime endOfCurrentPeriod = getEndOfCurrentPeriod();
            if (now.isBefore(startOfCurrentPeriod)) {
                //should this really be possible???
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
            }
        }
    }

    private void updateInitialStartOfCurrentPeriod(DateTime now) {
        if (period.getPeriodType() == PeriodType.years()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), 1, 1, 0, 0, 0);
        } else if (period.getPeriodType() == PeriodType.months()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(), 1, 0, 0, 0);
        } else if (period.getPeriodType() == PeriodType.weeks()) {
            startOfCurrentPeriod = now.minusSeconds(now.getSecondOfDay() + (now.getDayOfWeek() - 1) * 24 * 60 * 60);
        } else if (period.getPeriodType() == PeriodType.days()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), 0, 0, 0);
        } else if (period.getPeriodType() == PeriodType.hours()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), 0, 0);
        } else if (period.getPeriodType() == PeriodType.minutes()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(), 0);
        } else if (period.getPeriodType() == PeriodType.seconds()) {
            startOfCurrentPeriod = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour(),
                    now.getSecondOfMinute());
        } else {
            startOfCurrentPeriod = now;
        }
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

}
