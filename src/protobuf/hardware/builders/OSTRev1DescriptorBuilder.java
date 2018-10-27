package protobuf.hardware.builders;

import protobuf.hardware.Port.HardwarePort;
import protobuf.hardware.TypeDescriptor.HardwareTypeDescriptor;

/**
 * Created by mcochrane on 7/05/17.
 */
public class OSTRev1DescriptorBuilder extends DescriptorBuilder {

    public static void compile() {
        OSTRev1DescriptorBuilder obj = new OSTRev1DescriptorBuilder();
        obj.buildAndWriteDescriptor();
    }

    public void buildAndWriteDescriptor() {
        super.buildAndWriteDescriptor("/OSTRev1/descriptor.pb");
    }

    @Override
    public HardwareTypeDescriptor buildDescriptor() {
        HardwareTypeDescriptor.Builder builder = HardwareTypeDescriptor.newBuilder();
        builder.setHardwareId(0x2000);
        builder.setOptionId(0x0000);
        builder.setName("Wild Spy Open Source Timer Rev 1");
        builder.setActiveCurrentMa(6.0f);
        builder.setSleepCurrentUa(5.7f);
        builder.setNumChannels(4);

        // Describe ports
        HardwarePort.Builder portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(0);
        portBuilder.setDesignator("BAT");
        portBuilder.setDescription("Battery input to power the timer.");
        portBuilder.setLocation("Bottom right on front.");
        portBuilder.setType(HardwarePort.Type.POWER_IN);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(true);
        portBuilder.setMinInputVoltage(3.5f);
        portBuilder.setMaxInputVoltage(15.0f);
        portBuilder.setNumPins(2);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(1);
        portBuilder.setDesignator("SWD");
        portBuilder.setDescription("SoftWare Debug and programming port");
        portBuilder.setLocation("Front of board, bottom left smt pads.");
        portBuilder.setType(HardwarePort.Type.MIXED);
        portBuilder.setDevOnly(true);
        portBuilder.setCanPowerDevice(true);
        portBuilder.setMinInputVoltage(3.0f);
        portBuilder.setMaxInputVoltage(3.6f);
        portBuilder.setProtocol(HardwarePort.Protocol.SWD);
        portBuilder.setNumPins(5);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(2);
        portBuilder.setDesignator("USB");
        portBuilder.setDescription("A micro USB port. Can power the device if power select jumper is set to USB.");
        portBuilder.setLocation("Back bottom. USB.");
        portBuilder.setType(HardwarePort.Type.DIGITAL_SIGNAL);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setProtocol(HardwarePort.Protocol.USB);
        portBuilder.setNumPins(5);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(3);
        portBuilder.setDesignator("Channel 1");
        portBuilder.setDescription("Channel 1 PMOS switch.");
        portBuilder.setLocation("Front, top left.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(4);
        portBuilder.setDesignator("Channel 2");
        portBuilder.setDescription("Channel 2 PMOS switch.");
        portBuilder.setLocation("Front, middle left.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(5);
        portBuilder.setDesignator("Channel 3");
        portBuilder.setDescription("Channel 3 PMOS switch.");
        portBuilder.setLocation("Front, top right.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(6);
        portBuilder.setDesignator("Channel 4");
        portBuilder.setDescription("Channel 4 PMOS switch.");
        portBuilder.setLocation("Front, middle right.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(7);
        portBuilder.setDesignator("External Sensors");
        portBuilder.setDescription("Connector to power and control external sensors");
        portBuilder.setLocation("Front, top.");
        portBuilder.setType(HardwarePort.Type.MIXED);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(4);
        builder.addPorts(portBuilder);

        return builder.build();

//        portBuilder = HardwarePort.newBuilder();
//        portBuilder.setId(5);
//        portBuilder.setDesignator("");
//        portBuilder.setDescription("");
//        portBuilder.setLocation("");
//        portBuilder.setType(HardwarePort.Type.POWER_IN);
//        portBuilder.setDevOnly(false);
//        portBuilder.setCanPowerDevice(true);
//        portBuilder.setMinInputVoltage(3.0f);
//        portBuilder.setMaxInputVoltage(3.6f);
//        portBuilder.setProtocol(HardwarePort.Protocol.USB);
//        portBuilder.setNumPins(4);
//        builder.addPorts(portBuilder);
    }

}
