package min;

import org.joou.UByte;

/**
 * Created by mcochrane on 2/04/17.
 */
public class FrameReceiver implements ReceivedFrameHandler {

    private static final int MIN_ID_RESPONSE_GET_RULE_COUNT     = 0x26;
    private static final int MIN_ID_RESPONSE_GET_RULE_WITH_ID   = 0x27;
    private static final int MIN_ID_GET_RESPONSE_ALL_RULES      = 0x28;
    private static final int MIN_ID_RESPONSE_GENERAL            = 0xF0;

    private static final int MIN_ID_RESPONSE_GENERAL_ACK        = 0x00;
    private static final int MIN_ID_RESPONSE_GENERAL_NAK        = 0x01;

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
                int code = SerialHandler.min_decode(frame.get_payload());
                if (code == MIN_ID_RESPONSE_GENERAL_ACK) {
                    System.out.printf("Received ACK\r\n", code);
                } else if (code == MIN_ID_RESPONSE_GENERAL_NAK) {
                    System.out.printf("Received NAK\r\n", code);
                } else {
                    System.out.printf("Received response = %d\r\n", code);
                }
                break;
            default:
                System.out.println(frame.toString());
        }
    }
}
