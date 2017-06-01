package protobuf.hardware.builders;

import protobuf.hardware.Port.HardwarePort;
import protobuf.hardware.TypeDescriptor.HardwareTypeDescriptor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mcochrane on 7/05/17.
 */
public class OSTRev0DescriptorBuilder extends DescriptorBuilder {

    public static void compile() {
        OSTRev0DescriptorBuilder obj = new OSTRev0DescriptorBuilder();
        obj.buildAndWriteDescriptor();
    }

    public void buildAndWriteDescriptor() {
        super.buildAndWriteDescriptor("/OSTRev0/descriptor.pb");
    }

    @Override
    public HardwareTypeDescriptor buildDescriptor() {
        HardwareTypeDescriptor.Builder builder = HardwareTypeDescriptor.newBuilder();
        builder.setHardwareId(0x2000);
        builder.setOptionId(0x0000);
        builder.setName("Wild Spy Open Source Timer Rev 0");
        builder.setActiveCurrentMa(6.0f);
        builder.setSleepCurrentUa(5.7f);
        builder.setNumChannels(4);

        // Describe ports
        HardwarePort.Builder portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(0);
        portBuilder.setDesignator("Button Cell Holder");
        portBuilder.setDescription("Takes any 16mm lithium button cell.");
        portBuilder.setLocation("Back of board");
        portBuilder.setType(HardwarePort.Type.POWER_IN);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(true);
        portBuilder.setMinInputVoltage(3.0f);
        portBuilder.setMaxInputVoltage(3.6f);
        portBuilder.setNumPins(2);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(1);
        portBuilder.setDesignator("BAT");
        portBuilder.setDescription("Battery input to power the timer.");
        portBuilder.setLocation("Bottom left on front.");
        portBuilder.setType(HardwarePort.Type.POWER_IN);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(true);
        portBuilder.setMinInputVoltage(3.0f);
        portBuilder.setMaxInputVoltage(12.0f);
        portBuilder.setNumPins(2);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(2);
        portBuilder.setDesignator("PDI");
        portBuilder.setDescription("PDI programming port");
        portBuilder.setLocation("Back of board, square smt pads.");
        portBuilder.setType(HardwarePort.Type.MIXED);
        portBuilder.setDevOnly(true);
        portBuilder.setCanPowerDevice(true);
        portBuilder.setMinInputVoltage(3.0f);
        portBuilder.setMaxInputVoltage(3.6f);
        portBuilder.setProtocol(HardwarePort.Protocol.PDI);
        portBuilder.setNumPins(4);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(3);
        portBuilder.setDesignator("PortC");
        portBuilder.setDescription("Breakout pins for PortC of XMEGA32C4 MCU.");
        portBuilder.setLocation("Front middle left");
        portBuilder.setType(HardwarePort.Type.MIXED);
        portBuilder.setDevOnly(true);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(10);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(4);
        portBuilder.setDesignator("USB");
        portBuilder.setDescription("A micro USB port.  Doesn't power the device.");
        portBuilder.setLocation("Front bottom. USB.");
        portBuilder.setType(HardwarePort.Type.DIGITAL_SIGNAL);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setProtocol(HardwarePort.Protocol.USB);
        portBuilder.setNumPins(5);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(5);
        portBuilder.setDesignator("CH1");
        portBuilder.setDescription("Channel 1 PMOS switch.");
        portBuilder.setLocation("Front, top left.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(6);
        portBuilder.setDesignator("CH2");
        portBuilder.setDescription("Channel 2 PMOS switch.");
        portBuilder.setLocation("Front, middle left.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(7);
        portBuilder.setDesignator("CH3");
        portBuilder.setDescription("Channel 3 PMOS switch.");
        portBuilder.setLocation("Front, top right.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
        builder.addPorts(portBuilder);

        portBuilder = HardwarePort.newBuilder();
        portBuilder.setId(8);
        portBuilder.setDesignator("CH4");
        portBuilder.setDescription("Channel 4 PMOS switch.");
        portBuilder.setLocation("Front, middle right.");
        portBuilder.setType(HardwarePort.Type.SWITCH);
        portBuilder.setDevOnly(false);
        portBuilder.setCanPowerDevice(false);
        portBuilder.setNumPins(3);
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
