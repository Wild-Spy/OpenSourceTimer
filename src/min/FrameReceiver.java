package min;

import TimerDescriptionLanguage.TimeHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joou.UByte;
import org.joou.UInteger;

import javax.swing.*;
import java.util.Objects;

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

    public static final int MIN_ID_DEV_INFO                    = 0x36;

    public static final int MIN_ID_RESPONSE_GENERAL_ACK        = 0x00;
    public static final int MIN_ID_RESPONSE_GENERAL_NAK        = 0x01;
    public static final int MIN_ID_RESPONSE_PING               = 0x02;

    public static final int MIN_ID_RESPONSE_GET_DEVICE_TYPE    = 0x33;
    public static final int MIN_ID_RESPONSE_GET_FIRMWARE_VERSION = 0x34;


    private ReceivedFrameHandler response_callback_ = null;

    private ResponseType response;

    public enum ResponseType {
        Ack,
        Nak,
        Timeout
    }

    public ResponseType waitForResponse(Integer timeout_ms) {
        response = ResponseType.Timeout;
        if (timeout_ms == null) timeout_ms = 3000; //3 seconds

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
        while (System.currentTimeMillis() - start_time < timeout_ms) {
            if (response != ResponseType.Timeout) break;
            try {
                Thread.sleep(10); //sleep for 10ms
            } catch (InterruptedException e) {
                // Do nothing, just continue
            }
        }

        deleteResponseCallback();

        return response;
    }

    /**
     * Returns either the returned Frame, or null if it timed out
     * @return Frame
     */
    public Frame waitForResponseFrame(int timeout_ms, UByte expected_id) {
        final Frame[] result = new Frame[1];
        result[0] = null;
        setResponseCallback(new ReceivedFrameHandler() {
            @Override
            public void handleReceivedFrame(Frame frame) {
                result[0] = frame;
            }
        });

        long start_time = System.currentTimeMillis();
        while (System.currentTimeMillis() - start_time < timeout_ms) {
            if (result[0] != null) {
                if (expected_id != null && Objects.equals(result[0].get_id(), expected_id)) break;
            }
            try {
                Thread.sleep(10); //sleep for 10ms
            } catch (InterruptedException e) {
                // Do nothing, just continue.
            }
        }

        deleteResponseCallback();

        return result[0];
    }

    /**
     * Waits until the response callback is null.
     * @param timeout_ms    the number of milliseconds after which the function will time out
     * @return boolean  true if the response callback became null before the timeout
     *                  false if the function timed out before the callback became null
     */
    public boolean waitForFreeResponseCallback(int timeout_ms) {
        boolean result = false;

        long start_time = System.currentTimeMillis();
        while (timeout_ms == -1 || System.currentTimeMillis() - start_time < timeout_ms) {
            if (!hasResponseCallback()) {
                result = true;
                break;
            }
            try {
                Thread.sleep(10); //sleep for 10ms
            } catch (InterruptedException e) {
                // Do nothing, just continue.
            }
        }

        return result;
    }

    public synchronized boolean hasResponseCallback() {
        return response_callback_ != null;
    }

    public synchronized void setResponseCallback(ReceivedFrameHandler callback) {
        if (hasResponseCallback()) return;
        response_callback_ = callback; //TODO: allow multiple callbacks and call them all.  Then remove when done!!!
    }

    public synchronized void deleteResponseCallback() {
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
            case MIN_ID_RESPONSE_PING:
                if (response_callback_ != null) {
                    response_callback_.handleReceivedFrame(frame);
                }
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
                DateTime dt = TimeHelper.y2kEpochIntToDateTime(t);
                if (response_callback_ != null) {
                    response_callback_.handleReceivedFrame(frame);
                }
//                JOptionPane.showMessageDialog(null, "RTC Time: " + dt.toString() , "Error", JOptionPane.ERROR_MESSAGE);
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
            case MIN_ID_DEV_INFO:
                if (response_callback_ != null) {
                    response_callback_.handleReceivedFrame(frame);
                }
                break;
            case MIN_ID_RESPONSE_GET_DEVICE_TYPE:
                if (response_callback_ != null) {
                    response_callback_.handleReceivedFrame(frame);
                }
                break;
            default:
                System.out.println(frame.toString());
        }
    }
}
