package min;

import jssc.SerialPort;
import jssc.SerialPortException;

import org.joou.UByte;
import java.util.*;

/**
 * This class handles the serial port and sends frames to the port and receives them from the port.
 * <br>
 * It creates two threads, one for writing and one for reading
 */
public class SerialHandler {
    // States for receiving a frame
    private enum State {
        SOF, ID, CONTROL, PAYLOAD, CHECKSUM_HIGH, CHECKSUM_LOW, EOF;
    }

    private State state = State.SOF;

    private int header_bytes_seen = 0;
    private UByte frame_id = UByte.valueOf(0);
    private int frame_length = 0;
    private List<UByte> frame_payload = new ArrayList<>();
    private List<UByte> frame_checksum_bytes = new ArrayList<>();
    private int payload_bytes_to_go = 0;
    private Frame frame = null;
    private SerialPort serial;
    private ReceivedFrameHandler received_frame_handler;
    public FrameTransmitter frame_transmitter;
    Queue<List<UByte>> send_queue;// = new PriorityQueue<>();
    private Thread receive_thread;
    private Thread send_thread;

    private boolean show_raw = false;
    private boolean quiet = false;

    private Receiver receiver;
    private Sender sender;

    public SerialHandler(String port, int baudrate, ReceivedFrameHandler received_frame_handler)
            throws jssc.SerialPortException {
        this.state = State.SOF;
        this.serial = new SerialPort(port);
        serial.openPort();
        this.serial.setParams(baudrate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        this.received_frame_handler = received_frame_handler;

        // Initialize receiver and sender threads
        this.send_queue = new PriorityQueue<>();

        this.receiver = new Receiver();
        this.sender = new Sender();

        receive_thread = new Thread(this.receiver);
        send_thread = new Thread(this.sender);

        receive_thread.setDaemon(true);
        send_thread.setDaemon(true);

        receive_thread.start();
        send_thread.start();

        frame_transmitter = new FrameTransmitter(this);

    }

    public void Disconnect() {
        receiver.shutdown();
        sender.shutdown();
        try {
            this.serial.closePort();
        } catch (SerialPortException ex) {
            //must be closed...
        }
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void printfln(String s, Object... objects) {
        System.out.printf(s, objects);
        System.out.println();
    }

    /**
     * Receive loop that takes a byte at a time from the serial port and creates a frame
     */
    class Receiver implements Runnable {
        public boolean halt = false;

        @Override
        public void run() {
            while (!halt) {
                try {
                    // Read a byte from the serial line(blocking call)
                    byte[] data = SerialHandler.this.serial.readBytes(1);
                    if (show_raw) printfln("Data RX on wire: 0x%02x", data[0]);
                    //TODO: this might be a problem.  What is for example byte 0xFF -> -1? if so this won't work!
                    SerialHandler.this.build_received_frame(UByte.valueOf(data[0]));
                } catch (jssc.SerialPortException ex) {
                }
            }
        }

        public void shutdown() {
            halt = true;
        }

    }

    /**
     * Feed the queue into the serial port (blocking on reading the queue and the sending)
     */
    class Sender implements Runnable {
        public boolean halt = false;

        @Override
        public void run() {
            while (!halt) {
                try {
                    //println("sender_thread");
                    List<UByte> frame_data = SerialHandler.this.send_queue.remove();
                    if (show_raw) {
                        //printfln("Data TX on wire: %s" % ':'.join('0x{:02x}'.format(i) for i in frame_data))
                    }
                    SerialHandler.this.serial.writeBytes(ubyteListToByteArray(frame_data));
                } catch (NoSuchElementException | SerialPortException ex) {

                }
            }
        }

        public void shutdown() {
            halt = true;
        }
    }

    private byte[] ubyteListToByteArray(List<UByte> lst) {
        byte[] retAry = new byte[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            retAry[i] = lst.get(i).byteValue();
        }
        return retAry;
    }

    /**
     * Read bytes in sequence until a frame has been pulled in
     */
    private void  build_received_frame(UByte b) {
        if (this.header_bytes_seen == 2) {
            this.header_bytes_seen = 0;
            if (b.equals(Frame.HEADER_BYTE)) {
                //If three header bytes in a row, reset state machine and start reading a new frame
                if (show_raw) {
                    println("Header seen");
                }
                    this.state = State.ID;
                    return;
            }
            //Two in a row:we should see a stuff byte
            if (!b.equals(Frame.STUFF_BYTE)){
                //Something has gone wrong with the frame, discard and reset
                println("Framing error: Missing stuff byte");
                this.state = State.SOF;
                return;
            } else {
                //A stuff byte,discard and carry on receiving on the next byte where we were
                if (show_raw){
                    println("Stuff byte discarded");
                    return;
                }
            }
        }

        if (b.equals(Frame.HEADER_BYTE)) {
            this.header_bytes_seen += 1;
        } else {
            this.header_bytes_seen = 0;
        }

        if (this.state == State.ID) {
            if (show_raw) println("ID byte");
            this.frame_id = b;
            this.state = State.CONTROL;
        } else if (this.state == State.CONTROL) {
            if (show_raw) println("control byte");
            this.frame_length = b.intValue() & 0x000f;
            if (show_raw) {
                printfln("control byte 0b%s [length=%d]",
                        String.format("%16s", Integer.toBinaryString(b.intValue())).replace(' ', '0') ,
                        frame_length);
            }
            this.payload_bytes_to_go = this.frame_length;
            this.frame_payload = new ArrayList<>();
            if (this.payload_bytes_to_go > 0) {
                this.state = State.PAYLOAD;
            } else {
                this.state = State.CHECKSUM_HIGH;
            }
        } else if (this.state == State.PAYLOAD) {
            if (show_raw) println("payload byte");
            this.frame_payload.add(b);
            this.payload_bytes_to_go -= 1;
            if (this.payload_bytes_to_go == 0) {
                this.state = State.CHECKSUM_HIGH;
            }
        } else if (this.state == State.CHECKSUM_HIGH) {
            if (show_raw) println("checksum high");
            this.frame_checksum_bytes = new ArrayList<>();
            this.frame_checksum_bytes.add(b);
            this.state = State.CHECKSUM_LOW;
        } else if (this.state == State.CHECKSUM_LOW) {
            if (show_raw) println("checksum low");
            this.frame_checksum_bytes.add(b);
            //Construct the frame object
            this.frame = new Frame(this,
                    this.frame_id,
                    this.frame_payload);
            List<UByte> checksum_bytes = this.frame.checksum();
            if (!ubyteListsEqual(checksum_bytes, frame_checksum_bytes)) {
                //Checksum failure, drop it and look for a new one
                println("FAILED CHECKSUM");
                this.state = State.SOF;
            }else {
                this.state = State.EOF;
            }
        } else if (this.state == State.EOF) {
            if (b.equals(Frame.EOF_BYTE)) {
                if (show_raw) {
                    println("EOF, frame passed up");
                    println(this.frame.toString());
                }
                //Frame is well - formed, pass it up for handling
                received_frame_handler.handleReceivedFrame(frame);
            }
            this.state = State.SOF;
        }
    }

    private boolean ubyteListsEqual(List<UByte> expected, List<UByte> actual) {
        if (expected.size() != actual.size()) return false;
        for (int i = 0; i < expected.size(); i++) {
            if (!expected.get(i).equals(actual.get(i))) return false;
        }
        return true;
    }

    // Decoder MIN network order 16-bit and 32-bit words
    public static int min_decode(List<UByte> data) {
        if (data.size() == 1) {
            // 8-bit integer (unsigned..)
            return data.get(0).intValue();
        }else if (data.size() == 2) {
            // 16-bit big-endian integer
            return (data.get(0).intValue() << 8) | (data.get(1).intValue());
        } else if (data.size() == 4) {
            // 32-bit big-endian integer
            return (data.get(0).intValue() << 24) |
                    (data.get(1).intValue() << 16) |
                    (data.get(2).intValue() << 8) |
                    (data.get(3).intValue());
        }
        return -1;
    }

    // Encode a 32-bit integer into MIN network order bytes
    public static List<UByte> min_encode_32(int x) {
        List<UByte> ret = new ArrayList<>();
        ret.add(UByte.valueOf((x & 0xff000000) >> 24));
        ret.add(UByte.valueOf((x & 0x00ff0000) >> 16));
        ret.add(UByte.valueOf((x & 0x0000ff00) >> 8));
        ret.add(UByte.valueOf((x & 0x000000ff)));

        return ret;
    }

    // Encode a 16-bit integer into MIN network order bytes
    public static List<UByte> min_encode_16(short x) {
        List<UByte> ret = new ArrayList<>();
        ret.add(UByte.valueOf((x & 0x0000ff00) >> 8));
        ret.add(UByte.valueOf((x & 0x000000ff)));

        return ret;
    }

    /**
     * Called when a MIN frame has been received successfully from the serial line
     * @param frame the Frame that was received
     * @see Frame
     */
    // Called when a MIN frame has been received successfully from the serial line
    private void received_frame(Frame frame) {
        UByte message_id = frame.get_id();
        List<UByte> data = frame.get_payload();

        if (!quiet) {
            if (message_id.equals(UByte.valueOf(0x0e))) {      // Deadbeef message
                //print("RX deadbeef: " + ':'.join('{:02x}'.format(i) for i in data))
                println("RX deadbeef: " );
            } else if (message_id.equals(UByte.valueOf(0x23))) {            // Environment message
                double temperature = -20.0 + (min_decode(data.subList(0, 2)) *0.0625);
                double humidity = min_decode(data.subList(2, 4)) * 0.64;
                printfln("Environment: temperature=%fC, humidity=%f%", temperature, humidity);
            } else if (message_id.equals(UByte.valueOf(0x24))) {            // Motor status message
                UByte status = data.get(0);
                Integer position = min_decode(data.subList(1, 5));
                printfln("Motor: status=%d, position=%d", status.intValue(), position);
            } else if (message_id.equals(UByte.valueOf(0x02))) {
                println("Ping received");
                //print("Ping received: " + ':'.join('{:02x}'.format(i) for i in data));
            }
        }
    }
}
