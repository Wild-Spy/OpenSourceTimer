package min;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joou.UByte;
import org.joou.UInteger;

/**
 * Created by mcochrane on 2/04/17.
 */
public class FrameReceiver implements ReceivedFrameHandler {
    public static String print_rx_str;
    public static int print_rx_str_len;

    public static final int MIN_ID_RESPONSE_GET_RULE_COUNT     = 0x26;
    public static final int MIN_ID_RESPONSE_GET_RULE_WITH_ID   = 0x27;
    public static final int MIN_ID_GET_RESPONSE_ALL_RULES      = 0x28;
    public static final int MIN_ID_RESPONSE_GENERAL            = 0xF0;
    public static final int MIN_ID_RESPONSE_GET_RTC_TIME       = 0x29;

    public static final int MIN_ID_PRINT_START                 = 0x30;
    public static final int MIN_ID_PRINT_DATA                  = 0x31;
    public static final int MIN_ID_PRINT_END                   = 0x32;

    public static final int MIN_ID_RESPONSE_GENERAL_ACK        = 0x00;
    public static final int MIN_ID_RESPONSE_GENERAL_NAK        = 0x01;

    private ReceivedFrameHandler response_callback_ = null;

    private ResponseType response;

    public enum ResponseType {
        Ack,
        Nak,
        Timeout
    }

    public ResponseType waitForResponse() throws InterruptedException {
        response = ResponseType.Timeout;

        setResponseCallback(new ReceivedFrameHandler() {
            @Override
            public void handleReceivedFrame(Frame frame) {
                int code = SerialHandler.min_decode(frame.get_payload());
                if (code == FrameReceiver.MIN_ID_RESPONSE_GENERAL_ACK) {
                    response = ResponseType.Ack;
                } else if (code == FrameReceiver.MIN_ID_RESPONSE_GENERAL_NAK) {
                    response = ResponseType.Nak;
                }
            }
        });

        long start_time = System.currentTimeMillis();
        while (System.currentTimeMillis() - start_time < 3000) { //1 second timeout
            if (response != ResponseType.Timeout) break;
            Thread.sleep(10); //sleep for 10ms
        }

        return response;
    }

    public void setResponseCallback(ReceivedFrameHandler callback) {
        response_callback_ = callback;
    }

    public void deleteResponseCallback() {
        response_callback_ = null;
    }

    private DateTime y2kEpochIntToDateTime(long y2k_time_secs) {
        long unix_y2k_offset_secs = 946684800; //in seconds
        long unix_time_secs = y2k_time_secs + unix_y2k_offset_secs;
        return new DateTime(unix_time_secs*1000);
    }

    @Override
    public void handleReceivedFrame(Frame frame) {
        switch (frame.get_id().intValue()) {
            case MIN_ID_RESPONSE_GET_RULE_COUNT:
                int rule_count = SerialHandler.min_decode(frame.get_payload());
                System.out.printf("Rule count = %d\r\n", rule_count);
                break;
            case MIN_ID_RESPONSE_GET_RULE_WITH_ID:
                break;
            case MIN_ID_RESPONSE_GENERAL:
                long code = SerialHandler.min_decode(frame.get_payload());
                if (code == MIN_ID_RESPONSE_GENERAL_ACK) {
                    System.out.printf("Received ACK\r\n", code);
                } else if (code == MIN_ID_RESPONSE_GENERAL_NAK) {
                    System.out.printf("Received NAK\r\n", code);
                } else {
                    System.out.printf("Received response = %d\r\n", code);
                }
                if (response_callback_ != null) {
                    response_callback_.handleReceivedFrame(frame);
                }
                break;
            case MIN_ID_RESPONSE_GET_RTC_TIME:
                long t = SerialHandler.min_decode_unsigned(frame.get_payload());
                System.out.println(frame.toString());
                DateTime dt = y2kEpochIntToDateTime(t);
//                DateTime dt = new DateTime(t*1000);
                System.out.println("RTC Time: " + dt.toString());
//                System.out.println("Offset Seconds: " + ((new DateTime(2010, 1, 1, 0, 0, 0)).getMillis() - dt.getMillis())/1000);
                break;
            case MIN_ID_PRINT_START:
                print_rx_str = "";
                print_rx_str_len = (int)SerialHandler.min_decode_unsigned(frame.get_payload());
                break;
            case MIN_ID_PRINT_DATA:
                if (print_rx_str == null) return;
                print_rx_str += frame.payloadToCharString();
                break;
            case MIN_ID_PRINT_END:
                if (print_rx_str == null) return;
                if (print_rx_str.length() != print_rx_str_len)
                    System.out.println("Length of received print statement was incorrect.");
                System.out.println("DEVICE: " + print_rx_str);
                print_rx_str = null;
                break;
            default:
                System.out.println(frame.toString());
        }
    }
}
