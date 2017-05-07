package min;

import org.joou.UByte;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to handle MIN 1.0 frame handling. Constructed after receiving frame data from
 * the serial port and also in prelude to sending on the serial port.
 * <br/>
 * Ported from Python code.
 */
public class Frame {

    public static final UByte HEADER_BYTE = UByte.valueOf(0xAA);
    public static final UByte STUFF_BYTE = UByte.valueOf(0x55);
    public static final UByte EOF_BYTE = UByte.valueOf(0x55);

    private UByte frame_id;
    private List<UByte> payload;
    private List<UByte> stuffed = null;
    private SerialHandler handler;

    /**
     * @param serial_handler is the SerialHandler to handle callbacks
     */
    public Frame(SerialHandler serial_handler) {
        this(serial_handler, UByte.valueOf(0), new ArrayList<UByte>());
    }

    /**
     * @param serial_handler is the SerialHandler to handle callbacks
     * @param frame_id is the MIN frame id
     * @param payload is a list of up to 15 bytes that is the payload of the frame
     */
    public Frame(SerialHandler serial_handler, UByte frame_id, List<UByte> payload) {
        this.handler = serial_handler;
        this.frame_id = frame_id;
        this.payload = payload;
    }

    /**
     * Compute Fletcher's checksum (16-bit version)
     * <p>
     * <code>sum1</code> and <code>sum2</code> are 16 bit integers so must be
     * clipped back to this range
     * @return an array of high byte then low byte (big-endian order on the wire)
     */
    List<UByte> checksum() {
        long sum1 = 0xff;
        long sum2 = 0xff;

        List<UByte> checksummed_data = getRawWithoutPayload();

        for (UByte b : checksummed_data) {
            sum1 += b.intValue();
            sum1 &= 0xffff;  //Results wrapped at 16 bits
            sum2 += sum1;
            sum2 &= 0xffff;

            sum1 = (sum1 & 0x00ff) + (sum1 >> 8);
            sum2 = (sum2 & 0x00ff) + (sum2 >> 8);
        }

        sum1 = (sum1 & 0x00ff) + (sum1 >> 8);
        sum2 = (sum2 & 0x00ff) + (sum2 >> 8);
        long checksum = ((sum2 << 8) & 0xffff) | sum1;

        long high_byte = (checksum & 0xff00) >> 8;
        long low_byte = checksum & 0x00ff;

        List<UByte> ret = new ArrayList<>();
        ret.add(UByte.valueOf(high_byte));
        ret.add(UByte.valueOf(low_byte));
        return ret;
    }

    private List<UByte> getRawWithoutPayload() {
        List<UByte> data = new ArrayList<>();
        data.add(frame_id);
        data.add(get_control());
        data.addAll(payload);
        return data;
    }

    /**
     * Transmit this through the assigned serial handler
     */
    public void transmit() {
        this.handler.send_queue.add(this.get_bytes());
    }

    @Override
    public String toString() {
        String parts = "";
        for (UByte b : payload) {
            parts += ":" + String.format("%02x", b.intValue());
        }

        String s = String.format("ID: 0x%02x\n", frame_id.shortValue());
        s += String.format("Payload: %s\n", parts);

        return s;
    }

    public String payloadToCharString() {
        String str = "";
        for (UByte b : payload) {
            str += String.format("%c", b.intValue());
        }

        return str;
    }

    /**
     * Get the control byte based on the frame properties
     * <br>
     * NB: <br>
     * In MIN 1.0 the top four bits must be set to zero (reserved for future)
     */
    private UByte get_control() {
        UByte tmp = UByte.valueOf(0);
        tmp  = tmp.bitwise_or(UByte.valueOf(payload.size()));
        return tmp;
    }

    public List<UByte> get_payload() {
        return this.payload;
    }

    /**
     * Get the on - wire byte sequence for the frame, including stuff bytes after
     * every 0xaa 0xaa pair
     */
    private List<UByte> get_bytes() {
        List<UByte> raw = getRawWithoutPayload();
        raw.addAll(checksum());

        stuffed = new ArrayList<>();
        stuffed.add(HEADER_BYTE);
        stuffed.add(HEADER_BYTE);
        stuffed.add(HEADER_BYTE);

        int count = 0;

        for (UByte i : raw) {
            stuffed.add(i);
            if (i.equals(HEADER_BYTE)) {
                count += 1;
                if (count == 2) {
                    stuffed.add(STUFF_BYTE);
                    count = 0;
                }
            } else {
                count = 0;
            }
        }
        stuffed.add(EOF_BYTE);

        return stuffed;
    }

    public int get_length(){
        return payload.size();
    }

    public UByte get_id(){
        return frame_id;

    }
}
