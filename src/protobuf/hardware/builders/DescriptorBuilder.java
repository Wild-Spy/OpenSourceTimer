package protobuf.hardware.builders;

import protobuf.hardware.TypeDescriptor;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mcochrane on 7/05/17.
 */
public abstract class DescriptorBuilder {

    public void buildAndWriteDescriptor(String outputFilename) {
//        String outputFile = DescriptorBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/resources/hardware";
        String outputFile = System.getProperty("user.dir") + "/src/resources/hardware";
        outputFile = outputFile + outputFilename;

        try {
            FileOutputStream output = new FileOutputStream(outputFile);
            buildDescriptor().writeTo(output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract com.google.protobuf.GeneratedMessage buildDescriptor();

}
