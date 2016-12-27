package TimerDescriptionLanguage;

import org.joda.time.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 14/11/16.
 */
public final class TimeHelper {

    //Periods
    public static Period makePeriodMillis(int millis) {
        return Period.millis(millis);
    }

    public static Period makePeriodSeconds(int seconds) {
        return Period.seconds(seconds);
    }

    public static Period makePeriodMinutes(int minutes) {
        return Period.minutes(minutes);
    }

    public static Period makePeriodHours(int hours) {
        return Period.hours(hours);
    }

    public static Period makePeriodDays(int days) {
        return Period.days(days);
    }

    public static Period makePeriodWeeks(int weeks) {
        return Period.weeks(weeks);
    }

    public static Period makePeriodMonths(int months) {
        return Period.months(months);
    }

    public static Period makePeriodYears(int years) {
        return Period.years(years);
    }

    public static Period infinitePeriod() {
        /*
         * A very large number of years, though the number of
         * milliseconds still fits in a signed Long.
         */
        return makePeriodYears(1000000); //1 million years in the future...
    }

    public static class TimeTypePair {
        public int quantity;
        public DurationFieldType type;
        TimeTypePair(int quantity, DurationFieldType type) {
            this.quantity = quantity;
            this.type = type;
        }
    }

    public static TimeTypePair makeTimeTypePair(int quantity, DurationFieldType type) {
        return new TimeTypePair(quantity, type);
    }

    public static Period makePeriodCustom(TimeTypePair ... pairs) {
        if (pairs.length == 0) return null;
        Period period = new Period();
        for( TimeTypePair p : pairs ) {
            period = period.withField(p.type, p.quantity);
        }
        return period;
    }

    public static Period makePeriodCustom(TimeTypePair pair) {
        return makePeriodCustom(pair.quantity, pair.type);
    }

    public static Period makePeriodCustom(int quantity, DurationFieldType type) {
        return new Period().withField(type, quantity);
//        if (type == DurationFieldType.centuries()) {
//            return makePeriodYears(quantity*100);
//        } else if (type == DurationFieldType.years()) {
//            return makePeriodYears(quantity);
//        } else if (type == DurationFieldType.months()) {
//            return makePeriodMonths(quantity);
//        } else if (type == DurationFieldType.weeks()) {
//            return makePeriodWeeks(quantity);
//        } else if (type == DurationFieldType.days()) {
//            return makePeriodDays(quantity);
//        } else if (type == DurationFieldType.hours()) {
//            return makePeriodHours(quantity);
//        } else if (type == DurationFieldType.minutes()) {
//            return makePeriodMinutes(quantity);
//        } else if (type == DurationFieldType.seconds()) {
//            return  makePeriodSeconds(quantity);
//        } else if (type == DurationFieldType.millis()) {
//            return makePeriodMillis(quantity);
//        }
//        return null;
    }

    public static DurationFieldType getLongestDurationFieldType(Period period) {
        DateTime durationStartTime = new DateTime(2015, 1, 1, 0, 0, 0);
        DurationFieldType types[] = period.getFieldTypes();
        Period longest_duration = makePeriodMillis(0);
        DurationFieldType longest_duration_type = null;
        for (DurationFieldType t : types) {
            Period tp = makePeriodCustom(period.get(t), t);
            if (tp.toDurationFrom(durationStartTime).getMillis() >
                    longest_duration.toDurationFrom(durationStartTime).getMillis()) {
                longest_duration = tp;
                longest_duration_type = t;
            }
        }

//        if (longest_duration_type == null) {
//            //error....
//            //perhaps return ms?
//        }

        return longest_duration_type;
    }


    //PeriodIntervals
    public static List<PeriodInterval> join(List<PeriodInterval> ... parts) {
        List<PeriodInterval> retList = new ArrayList<>();
        for (List<PeriodInterval> part : parts) {
            retList.addAll(part);
        }
        return retList;
    }

    public static List<PeriodInterval> between(Period start, Period end) {
        List<PeriodInterval> retList = new ArrayList<>();
        retList.add(new PeriodInterval(start, end));
        return retList;
    }

    /**
     * Specifies a {@link PeriodInterval} from startSecond seconds until endSecond
     * seconds.
     * <p>
     * startSecond is included in the {@link PeriodInterval} <br>
     * endSecond is excluded from the {@link PeriodInterval}
     * <p>
     * If startSecond is 10 and endSecond is 20 then the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include 00:00:09.999</li>
     *     <li>does include 00:00:10.000</li>
     *     <li>does include 00:00:19.999</li>
     *     <li>does not include 00:20:00.000</li>
     * </ul>
     *
     * @param startSecond   inclusive starting second
     * @param endSecond     exclusive ending second (must be > startSecond)
     * @return              a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenSeconds(int startSecond, int endSecond) {
        if (startSecond < 0 || endSecond < 0) throw new InvalidParameterException();
        if (endSecond <= startSecond) throw new InvalidParameterException();
        return between(Period.seconds(startSecond), Period.seconds(endSecond));
    }

    /**
     * Specifies a {@link PeriodInterval} from startMinute minutes until endMinute
     * minutes.
     * <p>
     * startMinute is included in the {@link PeriodInterval} <br>
     * endMinute is excluded from the {@link PeriodInterval}
     * <p>
     * If startMinute is 10 and endMinute is 20 then the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include 00:09:59.999</li>
     *     <li>does include 00:10:00.000</li>
     *     <li>does include 00:19:59.999</li>
     *     <li>does not include 00:20:00.000</li>
     * </ul>
     *
     * @param startMinute   inclusive starting minute
     * @param endMinute     exclusive ending minute (must be > startMinute)
     * @return              a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenMinutes(int startMinute, int endMinute) {
        if (startMinute < 0 || endMinute < 0) throw new InvalidParameterException();
        if (endMinute <= startMinute) throw new InvalidParameterException();
        return between(Period.minutes(startMinute), Period.minutes(endMinute));
    }

    /**
     * Specifies a {@link PeriodInterval} from startHour hours until endHour
     * hours.
     * <p>
     * startHour is included in the {@link PeriodInterval} <br>
     * endHour is excluded from the {@link PeriodInterval}
     * <p>
     * If startHour is 10 and endHour is 20 then the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include 09:59:59.999</li>
     *     <li>does include 10:00:00.000</li>
     *     <li>does include 19:59:59.999</li>
     *     <li>does not include 20:00:00.000</li>
     * </ul>
     *
     * @param startHour   inclusive starting hour
     * @param endHour     exclusive ending hour (must be > startHour)
     * @return            a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenHours(int startHour, int endHour) {
        if (startHour < 0 || endHour < 0) throw new InvalidParameterException();
        if (endHour <= startHour) throw new InvalidParameterException();
        return between(Period.hours(startHour), Period.hours(endHour));
    }

    /**
     * Specifies a {@link PeriodInterval} from startDay until endDay.
     * The values are one-indexed unlike {@link TimeHelper#betweenSeconds},
     * {@link TimeHelper#betweenMinutes} and {@link TimeHelper#betweenHours}.
     * Therefore, referencing from day 1 to day 5 would mean from the first
     * day of a period to the 5th day.  If the period was a month then this
     * would represent from the 1st of the month to the 5th of the month.
     * Passing zero to startDay or endDay is not valid and will throw a
     * {@link InvalidParameterException}.
     * <p>
     * startDay is included in the {@link PeriodInterval} <br>
     * endDay is excluded from the {@link PeriodInterval}
     * <p>
     * If startDay is 10 and endDay is 20 then the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include the 9th @ 23:59:59.999</li>
     *     <li>does include the 10th @ 00:00:00.000</li>
     *     <li>does include the 19th @ 23:59:59.999</li>
     *     <li>does not include the 20th @ 00:00:00.000</li>
     * </ul>
     *
     * @param startDay  inclusive starting day
     * @param endDay    exclusive ending day (must be > startDay)
     * @return          a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenDays(int startDay, int endDay) {
        if (startDay <= 0 || endDay <= 0) throw new InvalidParameterException();
        if (endDay <= startDay) throw new InvalidParameterException();
        return between(Period.days(startDay-1), Period.days(endDay-1));
    }

    /**
     * Specifies a {@link PeriodInterval} from startWeek until endWeek.
     * The values are one-indexed unlike {@link TimeHelper#betweenSeconds},
     * {@link TimeHelper#betweenMinutes} and {@link TimeHelper#betweenHours}.
     * Therefore, referencing from week 2 to week 4 would mean from the second
     * week in a period to the 4th week in a period.  If the period was a
     * month then this would represent from the 2nd week of the month (from the
     * 8th day of the month) to the 4th week of the month (22nd day of the month).
     * Passing zero to startWeek or endWeek is not valid and will throw a
     * {@link InvalidParameterException}.
     * <p>
     * startWeek is included in the {@link PeriodInterval} <br>
     * endWeek is excluded from the {@link PeriodInterval}
     * <p>
     * If startWeek is 2 and endWeek is 4 then the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include the 7th day @ 23:59:59.999</li>
     *     <li>does include the 8th day @ 00:00:00.000</li>
     *     <li>does include the 21st day @ 23:59:59.999</li>
     *     <li>does not include the 22nd day @ 00:00:00.000</li>
     * </ul>
     *
     * @param startWeek     inclusive starting week
     * @param endWeek       exclusive ending week (must be > startWeek)
     * @return              a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenWeeks(int startWeek, int endWeek) {
        if (startWeek <= 0 || endWeek <= 0) throw new InvalidParameterException();
        if (endWeek <= startWeek) throw new InvalidParameterException();
        return between(Period.weeks(startWeek-1), Period.weeks(endWeek-1));
    }

    /**
     * Specifies a {@link PeriodInterval} from startMonth until endMonth.
     * The values are one-indexed unlike {@link TimeHelper#betweenSeconds},
     * {@link TimeHelper#betweenMinutes} and {@link TimeHelper#betweenHours}.
     * Therefore, referencing from month 2 to month 4 would mean from the second
     * month in a period to the 4th month in a period.  If the period was a
     * year and started some time in January, then this would represent from
     * February to April.
     * Passing zero to startMonth or endMonth is not valid and will throw a
     * {@link InvalidParameterException}.
     * <p>
     * startMonth is included in the {@link PeriodInterval} <br>
     * endMonth is excluded from the {@link PeriodInterval}
     * <p>
     * If startMonth is 2 and endMonth is 4 and our Period starts in January then
     * the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include January 31st @ 23:59:59.999</li>
     *     <li>does include February 1st @ 00:00:00.000</li>
     *     <li>does include March 31st @ 23:59:59.999</li>
     *     <li>does not include April 1st @ 00:00:00.000 (and that's not an April Fools prank!)</li>
     * </ul>
     *
     * @param startMonth    inclusive starting month
     * @param endMonth      exclusive ending month (must be > startMonth)
     * @return              a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenMonths(int startMonth, int endMonth) {
        if (startMonth <= 0 || endMonth <= 0) throw new InvalidParameterException();
        if (endMonth <= startMonth) throw new InvalidParameterException();
        return between(Period.months(startMonth-1), Period.months(endMonth-1));
    }

    /**
     * Specifies a {@link PeriodInterval} from startYear until endYear.
     * Passing anything less than zero to startYear or endYear is not valid and will throw an
     * {@link InvalidParameterException}.
     * <p>
     * startYear is included in the {@link PeriodInterval} <br>
     * endYear is excluded from the {@link PeriodInterval}
     * <p>
     * If startYear is 0 and endYear is 2 and our Period starts at the beginning of 2016 then
     * the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include December 31st, 2015 @ 23:59:59.999</li>
     *     <li>does include January 1st, 2016 @ 00:00:00.000</li>
     *     <li>does include December 31st, 2017 @ 23:59:59.999</li>
     *     <li>does not include January 1st, 2018 @ 00:00:00.000</li>
     * </ul>
     *
     * @param startYear    inclusive starting year
     * @param endYear      exclusive ending year (must be > startYear)
     * @return             a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenYears(int startYear, int endYear) {
        if (startYear < 0 || endYear < 0) throw new InvalidParameterException();
        if (endYear <= startYear) throw new InvalidParameterException();
        return between(Period.years(startYear), Period.years(endYear));
    }

    /**
     * Specifies a {@link PeriodInterval} from startTime until endTime.
     * Passing a startTime which is after endTime is not valid and will throw a
     * {@link InvalidParameterException}.
     * <p>
     * startTime is included in the {@link PeriodInterval} <br>
     * endTime is excluded from the {@link PeriodInterval}
     * <p>
     * If startTime is 2am and endTime is 4pm then our Period must start at midnight
     * and the {@link PeriodInterval}:
     * <ul>
     *     <li>does not include 01:59:59.999</li>
     *     <li>does include 02:00:00.000</li>
     *     <li>does include 13:59:59.999</li>
     *     <li>does not include 14:00:00.000</li>
     * </ul>
     *
     * @param startTime     inclusive starting time of day
     * @param endTime       exclusive ending time of day (must be > startTime)
     * @return              a list containing a single {@link PeriodInterval}
     */
    public static List<PeriodInterval> betweenTimesOfDay(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime)) throw new InvalidParameterException();
        Period startPeriod = localTimeToPeriod(startTime);
        Period endPeriod = localTimeToPeriod(endTime);
        return between(startPeriod, endPeriod);
    }

    /**
     * Generates a period from a {@link LocalTime}
     * @param timeOfDay the time to convert to a period
     * @return the converted time as a period
     */
    public static Period localTimeToPeriod(LocalTime timeOfDay) {
        return new Period(timeOfDay.getHourOfDay(), timeOfDay.getMinuteOfHour(), timeOfDay.getSecondOfMinute(), timeOfDay.getMillisOfSecond());
    }

    public static List<PeriodInterval> onSeconds(int ... seconds)
            throws IllegalArgumentException {
        List<PeriodInterval> retList = new ArrayList<>();
        for (int second : seconds) {
            retList.add(onSecond(second));
        }
        return retList;
    }

    public static List<PeriodInterval> onMinutes(int ... minutes)
            throws IllegalArgumentException {
        List<PeriodInterval> retList = new ArrayList<>();
        for (int minute : minutes) {
            retList.add(onMinute(minute));
        }
        return retList;
    }

    public static List<PeriodInterval> onHours(int ... hours)
            throws IllegalArgumentException {
        List<PeriodInterval> retList = new ArrayList<>();
        for (int hour : hours) {
            retList.add(onHour(hour));
        }
        return retList;
    }

    public static List<PeriodInterval> onDays(int ... days)
            throws IllegalArgumentException {
        List<PeriodInterval> retList = new ArrayList<>();
        for (int day : days) {
            retList.add(onDay(day));
        }
        return retList;
    }

    public static List<PeriodInterval> onMonths(int ... months)
            throws IllegalArgumentException {
        List<PeriodInterval> retList = new ArrayList<>();
        for (int month : months) {
            retList.add(onMonth(month));
        }
        return retList;
    }


    private static PeriodInterval onSecond(int second)
            throws IllegalArgumentException {
        if (second < 0) {
            throw new IllegalArgumentException("second " +
                    String.valueOf(second) + " not valid");
        }
        return new PeriodInterval(Period.seconds(second),
                Period.minutes(second+1));
    }

    private static PeriodInterval onMinute(int minute)
            throws IllegalArgumentException {
        if (minute < 0) {
            throw new IllegalArgumentException("minute " +
                    String.valueOf(minute) + " not valid");
        }
        return new PeriodInterval(Period.minutes(minute),
                Period.minutes(minute+1));
    }

    private static PeriodInterval onHour(int hour)
            throws IllegalArgumentException {
        if (hour < 0) {
            throw new IllegalArgumentException("hour " +
                    String.valueOf(hour) + " not valid");
        }
        return new PeriodInterval(Period.hours(hour),
                Period.hours(hour+1));
    }

    private static PeriodInterval onDay(int day)
            throws IllegalArgumentException {
        if (day < 1)
        {
            throw new IllegalArgumentException("day " +
                    String.valueOf(day) + " not valid");
        }
        return new PeriodInterval(Period.days(day-1),
                Period.days(day));
    }

    private static PeriodInterval onMonth(int month)
            throws IllegalArgumentException {
        if (month < 1) {
            throw new IllegalArgumentException("month " +
                    String.valueOf(month) + " not valid");
        }
        return new PeriodInterval(Period.months(month-1),
                Period.months(month));
    }



    private TimeHelper() throws Exception {throw new Exception("Cannot be instantiated.");}
}
