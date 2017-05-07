package TimerDescriptionLanguage;

import min.SerialHandler;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 3/12/16.
 */
public class PeriodCompiler {

    public static List<UByte> compilePeriod(Period period) {
        List<UByte> compiledPeriod = new ArrayList<>();
        //TODO: make this work.......
        //byte (non-zero field count)
        //field 1
        //byte (field type)
        //4 bytes (field value)
        //field 2
        //byte (field type)
        //4 bytes (field value)
        //field N
        //byte (field type)
        //4 bytes (field value)

        if (period == null) {
            compiledPeriod.add(UByte.valueOf(0));
        } else {

            DurationFieldType[] field_types = period.getFieldTypes();
            int[] values = period.getValues();

            int field_count = 0;
            for (int i = 0; i < field_types.length; i++) {
                if (values[i] != 0) field_count++;
            }

            compiledPeriod.add(UByte.valueOf(field_count));
            for (int i = 0; i < field_types.length; i++) {
                if (values[i] != 0) {
                    compiledPeriod.add(fieldTypeToUByte(field_types[i]));
                    compiledPeriod.addAll(SerialHandler.min_encode_32(values[i]));
                }
            }
        }

        return compiledPeriod;
    }

    public static UByte fieldTypeToUByte(DurationFieldType dft) {
        if (dft == DurationFieldType.years()) {
            return UByte.valueOf(0);
        } else if (dft == DurationFieldType.months()) {
            return UByte.valueOf(1);
        } else if (dft == DurationFieldType.weeks()) {
            return UByte.valueOf(2);
        } else if (dft == DurationFieldType.days()) {
            return UByte.valueOf(3);
        } else if (dft == DurationFieldType.hours()) {
            return UByte.valueOf(4);
        } else if (dft == DurationFieldType.minutes()) {
            return UByte.valueOf(5);
        } else if (dft == DurationFieldType.seconds()) {
            return UByte.valueOf(6);
        } else if (dft == DurationFieldType.millis()) {
            return UByte.valueOf(7);
        } else {
            return UByte.valueOf(255);
        }
    }

}
