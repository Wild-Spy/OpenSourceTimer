package TimerDescriptionLanguage;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 14/11/16.
 * Represents an interval based on periods rather than specific times.
 * A PeriodInterval could specify from 1 day to 2 days after some time.
 * Or it could specify from 10 seconds to 20 secones after some time.
 */
public class PeriodInterval {
    private Period start;
    private Period end;

    public PeriodInterval(Period start, Period end) {
        this.start = start;
        this.end = end;
    }

    public Interval toInterval(DateTime startTimeInstant) {
        return new Interval(startTimeInstant.plus(this.start),
                startTimeInstant.plus(this.end));
    }

    public Period getStart() {
        return this.start;
    }

    public Period getEnd() {
        return this.end;
    }

    @Override
    public boolean equals(final Object obj) {
        if ( obj == null || obj == this || !(obj instanceof PeriodInterval) )
            return false;

        PeriodInterval pi = (PeriodInterval) obj;
        if (start.equals(pi.start) && end.equals(pi.end))
            return true;
        else
            return false;
    }

    List<UByte> compile() {
        List<UByte> compiledPeriodList = new ArrayList<>();

        compiledPeriodList.addAll(PeriodCompilier.compilePeriod(start));
        compiledPeriodList.addAll(PeriodCompilier.compilePeriod(end));

        return compiledPeriodList;
    }

//    private void fitsInPeriod(Period period) throws Rule.InvalidIntervalException {
//        if (start.)
//
//        if (period.getPeriodType() == PeriodType.years()) {
//            if (getStart().getYears()-1970 > period.getYears()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYears()-1970 > period.getYears()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.months()) {
//            if (getStart().getYears()-1970 > 0 ||
//                    getStart().getMonths() > period.getMonths()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 0 ||
//                    getEnd().getMonthOfYear() > period.getMonths()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.weeks()) {
//            if (getStart().getYear()-1970 > 0 ||
//                    getStart().getWeekOfWeekyear() > period.getWeeks()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 0 ||
//                    getEnd().getWeekOfWeekyear() > period.getWeeks()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.days()) {
//            if (getStart().getYear()-1970 > 0 ||
//                    getStart().getMonthOfYear() > 1 ||
//                    getStart().getDayOfMonth() > period.getDays()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 0 ||
//                    getEnd().getMonthOfYear() > 1 ||
//                    getEnd().getDayOfMonth() > period.getDays()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.hours()) {
//            if (getStart().getYear()-1970 > 1 ||
//                    getStart().getMonthOfYear() > 1 ||
//                    getStart().getDayOfMonth() > 1 ||
//                    getStart().getHourOfDay() > period.getHours()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 1 ||
//                    getEnd().getMonthOfYear() > 1 ||
//                    getEnd().getDayOfMonth() > 1 ||
//                    getEnd().getHourOfDay() > period.getHours()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.minutes()) {
//            if (getStart().getYear()-1970 > 1 ||
//                    getStart().getMonthOfYear() > 1 ||
//                    getStart().getDayOfMonth() > 1 ||
//                    getStart().getHourOfDay() > 1 ||
//                    getStart().getMinuteOfHour() > period.getMinutes()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 1 ||
//                    getEnd().getMonthOfYear() > 1 ||
//                    getEnd().getDayOfMonth() > 1 ||
//                    getEnd().getHourOfDay() > 1 ||
//                    getEnd().getMinuteOfHour() > period.getMinutes()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else if (period.getPeriodType() == PeriodType.seconds()) {
//            if (getStart().getYear()-1970 > 1 ||
//                    getStart().getMonthOfYear() > 1 ||
//                    getStart().getDayOfMonth() > 1 ||
//                    getStart().getHourOfDay() > 1 ||
//                    getStart().getMinuteOfHour() > 1 ||
//                    getStart().getSecondOfDay() > period.getSeconds()) {
//                throw new Rule.InvalidIntervalException();
//            }
//            if (getEnd().getYear()-1970 > 1 ||
//                    getEnd().getMonthOfYear() > 1 ||
//                    getEnd().getDayOfMonth() > 1 ||
//                    getEnd().getHourOfDay() > 1 ||
//                    getEnd().getMinuteOfHour() > 1 ||
//                    getEnd().getSecondOfDay() > period.getSeconds()) {
//                throw new Rule.InvalidIntervalException();
//            }
//        } else {
//            throw new Rule.InvalidIntervalException();
//        }
//    }

}
