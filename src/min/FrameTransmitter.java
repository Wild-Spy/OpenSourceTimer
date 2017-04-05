package min;

import TimerDescriptionLanguage.Rule;
import TimerDescriptionLanguage.Rules;
import org.joou.UByte;

import java.util.ArrayList;
import java.util.List;

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
        sendRuleStart(compiledRule);
        sendRuleData(compiledRule);
        sendRuleEnd();
        //TODO: wait for response
    }

    private void sendRuleStart(List<UByte> compiledRule) {
        //Tx Start Frame
        List<UByte> data = new ArrayList<>();
        data.addAll(SerialHandler.min_encode_16((short)compiledRule.size()));
        sendFrame(MIN_ID_RULE_START_RECEIVE, data);
    }

    private void sendRuleData(List<UByte> compiledRule) {
        List<UByte> data;
        int startIndex = 0;
        while (startIndex < compiledRule.size()) {
            int endIndex = startIndex + 32;
            if (endIndex > compiledRule.size()) endIndex = compiledRule.size();
            data = compiledRule.subList(startIndex, endIndex);
            sendFrame(MIN_ID_RULE_DATA_RECEIVE, data);
            startIndex += 32;
        }
    }

    private void sendRuleEnd() {
        List<UByte> data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        sendFrame(MIN_ID_RULE_END_RECEIVE, data);
    }

}