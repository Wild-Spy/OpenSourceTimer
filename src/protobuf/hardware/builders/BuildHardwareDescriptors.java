package protobuf.hardware.builders;

import javax.swing.*;

/**
 * Created by mcochrane on 7/05/17.
 */
public class BuildHardwareDescriptors implements Runnable {
    @Override
    public void run() {
        OSTRev0DescriptorBuilder.compile();
        OSTRev1DescriptorBuilder.compile();
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new BuildHardwareDescriptors());
    }
}
