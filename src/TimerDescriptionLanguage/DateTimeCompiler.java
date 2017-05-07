package TimerDescriptionLanguage;

import min.SerialHandler;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 3/12/16.
 */
public class DateTimeCompiler {

//        private DateTime y2kEpochIntToDateTime(long y2k_time_secs) {
//            long unix_y2k_offset_secs = 946684800; //in seconds
//            long unix_time_secs = y2k_time_secs + unix_y2k_offset_secs;
//            return new DateTime(unix_time_secs*1000);
//        }

    public static List<UByte> compileDateTime(DateTime dateTime) {
        List<UByte> compiledDateTime = new ArrayList<>();

        if (dateTime == null) {
            compiledDateTime.add(UByte.valueOf(0xFF));
            compiledDateTime.add(UByte.valueOf(0xFF));
            compiledDateTime.add(UByte.valueOf(0xFF));
            compiledDateTime.add(UByte.valueOf(0xFF));
        } else {
            long unix_y2k_offset_secs = 946684800; //in seconds
            compiledDateTime.addAll(SerialHandler.min_encode_u32((int) ((dateTime.getMillis() / 1000) - unix_y2k_offset_secs)));
        }
        return compiledDateTime;
    }

}
