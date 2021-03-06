package min;

import TimerDescriptionLanguage.Rule;
import TimerDescriptionLanguage.Rules;
import TimerDescriptionLanguage.TimeHelper;
import org.joda.time.DateTime;
import org.joou.UByte;
import org.joou.UInteger;

import java.util.ArrayList;
import java.util.List;

import static min.FrameReceiver.MIN_ID_RESPONSE_GET_DEVICE_TYPE;
import static min.FrameReceiver.MIN_ID_RESPONSE_GET_FIRMWARE_VERSION;

/**
 * Created by mcochrane on 2/04/17.
 */
public class FrameTransmitter {


    private static final int MIN_ID_PING                    = 0x02;
    private static final int MIN_ID_RULE_START_RECEIVE      = 0x03;
    private static final int MIN_ID_RULE_DATA_RECEIVE       = 0x04;
    private static final int MIN_ID_RULE_END_RECEIVE        = 0x05;
    private static final int MIN_ID_GET_RULE_COUNT          = 0x06;
    private static final int MIN_ID_GET_RULE_WITH_ID        = 0x07;
    private static final int MIN_ID_GET_ALL_RULES           = 0x08;
    private static final int MIN_ID_ERASE_ALL_RULES         = 0x09;
    private static final int MIN_ID_GET_RTC_TIME            = 0x0a;
    private static final int MIN_ID_SET_RTC_TIME            = 0x0b;
    private static final int MIN_ID_SAVE_RULES              = 0x0c;
    private static final int MIN_ID_PRINT_EEPROM            = 0x0d;
    private static final int MIN_ID_GET_DEVICE_TYPE         = 0x0e;
    private static final int MIN_ID_GET_DEVICE_FW_VER       = 0x0f; //firmware version
//    private static final int MIN_ID_GET_DEV_INFO            = 0x10;

    private SerialHandler serialHandler;

    public FrameTransmitter(SerialHandler _serialHandler) {
        serialHandler = _serialHandler;
    }

    private void sendFrame(int message_id, List<UByte> data) {
        Frame frame = new Frame(this.serialHandler, UByte.valueOf(message_id), data); //GetRuleCount
        frame.transmit();
    }

    public void sendPing() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0x01));
        data.add(UByte.valueOf(0x02));
        data.add(UByte.valueOf(0x03));

        sendFrame(MIN_ID_PING, data);
    }

    public void sendEraseAllRules() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0x5D));
        data.add(UByte.valueOf(0x92));

        sendFrame(MIN_ID_ERASE_ALL_RULES, data);
    }

    public void sendGetRuleCount() {
        List<UByte> data = new ArrayList<>();
        sendFrame(MIN_ID_GET_RULE_COUNT, data);
    }

    public void sendAllRules() {
        List<Rule> allRules = Rules.getInstance().getAll();
        for (Rule r : allRules) {
            sendRule(r);
            //TODO: wait...
        }
    }

    public void sendRule(Rule rule) {
        sendCompiledRule(rule.compile());
    }

    public void sendCompiledRule(List<UByte> compiledRule) {
        try {
            Thread.sleep(100);
            sendRuleStart(compiledRule);
            Thread.sleep(100);
            sendRuleData(compiledRule);
            sendRuleEnd();
        } catch (Exception e) {}
    }

    private void sendRuleStart(List<UByte> compiledRule) {
        //Tx Start Frame
        List<UByte> data = new ArrayList<>();
        data.addAll(SerialHandler.min_encode_16((short)compiledRule.size()));
        sendFrame(MIN_ID_RULE_START_RECEIVE, data);
    }

    private void sendRuleData(List<UByte> compiledRule) throws Exception {
        List<UByte> data;
        int startIndex = 0;
        while (startIndex < compiledRule.size()) {
            int endIndex = startIndex + 15;
            if (endIndex > compiledRule.size()) endIndex = compiledRule.size();
            data = compiledRule.subList(startIndex, endIndex);
            sendFrame(MIN_ID_RULE_DATA_RECEIVE, data);
            Thread.sleep(100);
            startIndex = endIndex;
        }
    }

    private void sendRuleEnd() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_RULE_END_RECEIVE, data);
    }

    public void sendGetRtcTime() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_GET_RTC_TIME, data);
    }

    public DateTime getRtcTimeBlocking(int timeout_ms) {
        if (!serialHandler.received_frame_handler.waitForFreeResponseCallback(2000)) return null;
        sendGetRtcTime();
        Frame f = serialHandler.received_frame_handler.waitForResponseFrame(timeout_ms, UByte.valueOf(FrameReceiver.MIN_ID_RESPONSE_GET_RTC_TIME));
        if (f == null) {
            System.out.printf("Failed to get time.\r\n");
            return null;
        }
        long t = SerialHandler.min_decode_unsigned(f.get_payload());
        return TimeHelper.y2kEpochIntToDateTime(t);
    }

    public void sendSetRtcTime(DateTime dateTime) {
        List<UByte> data = new ArrayList<>();

        long unix_y2k_offset_secs = 946684800; //in seconds

        data.addAll(SerialHandler.min_encode_u32((int)((dateTime.getMillis() / 1000) - unix_y2k_offset_secs)));

        sendFrame(MIN_ID_SET_RTC_TIME, data);
    }

    public void sendSaveRules() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_SAVE_RULES, data);
    }

    public void sendPrintEeprom(short start_index, int length) {
        List<UByte> data = new ArrayList<>();
        data.addAll(SerialHandler.min_encode_u16(start_index));
        data.add(UByte.valueOf(length));
        sendFrame(MIN_ID_PRINT_EEPROM, data);
    }

    private String getResponseAsString(int frame_id) {
        Frame response = this.serialHandler.received_frame_handler.waitForResponseFrame(1000, null);
        if (response == null) return "";
        if (response.get_id().intValue() != frame_id) return "";
        return response.payloadToCharString();
    }

    public void sendGetDeviceType() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_GET_DEVICE_TYPE, data);
    }

    public String sendGetDeviceTypeBlocking() {
        serialHandler.received_frame_handler.waitForFreeResponseCallback(100);
        sendGetDeviceType();
        return getResponseAsString(MIN_ID_RESPONSE_GET_DEVICE_TYPE);
    }


    public void sendGetDeviceFirmwareVersion() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_GET_DEVICE_FW_VER, data);
    }

    public String sendGetDeviceFirmwareVersionBlocking() {
        serialHandler.received_frame_handler.waitForFreeResponseCallback(1000);
        sendGetDeviceFirmwareVersion();
        return getResponseAsString(MIN_ID_RESPONSE_GET_FIRMWARE_VERSION);
    }

}