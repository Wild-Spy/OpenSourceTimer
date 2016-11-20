package TimerDescriptionLanguage;

import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 14/11/16.
 */
public final class TimeHelper {

    //Periods
    public static Period makePeriodMillis(int millis) {
        return new Period(Period.millis(millis),
                PeriodType.millis());
    }

    public static Period makePeriodSeconds(int seconds) {
        return new Period(Period.seconds(seconds),
                PeriodType.seconds());
    }

    public static Period makePeriodMinutes(int minutes) {
        return new Period(Period.minutes(minutes),
                PeriodType.minutes());
    }

    public static Period makePeriodHours(int hours) {
        return new Period(Period.hours(hours),
                PeriodType.hours());
    }

    public static Period makePeriodDays(int days) {
        return new Period(Period.days(days),
                PeriodType.days());
    }

    public static Period makePeriodWeeks(int weeks) {
        return new Period(Period.weeks(weeks),
                PeriodType.weeks());
    }

    public static Period makePeriodMonths(int months) {
        return new Period(Period.months(months),
                PeriodType.months());
    }

    public static Period makePeriodYears(int years) {
        return new Period(Period.years(years),
                PeriodType.years());
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
     *     <li>does not include April 1st @ 00:00:00.000 (and that's for real, not an April Fools prank.)</li>
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
