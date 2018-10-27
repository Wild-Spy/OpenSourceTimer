import protobuf.hardware.TypeDescriptor;
import sun.misc.Launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import protobuf.hardware.TypeDescriptor.HardwareTypeDescriptor;

/**
 * Created by mcochrane on 7/05/17.
 */
public class HardwareType {
    private String name;
    HardwareTypeDescriptor typeDescriptor = null;


    public static String getNameFromId(Long id) {
        if (id.equals(0L)) {
            return "OSTRev0";
        } else if (id.equals(1L)) {
            return "OSTRev1";
        } else return "Unknown";
    }

    public static HardwareType getFromId(Long id) {
        return new HardwareType(getNameFromId(id));
    }

    HardwareType(String name) {
        this.name = name;
        try {
            typeDescriptor = HardwareTypeDescriptor.parseFrom(this.getClass().getResourceAsStream(getDescriptorPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getHardwarePath() {
        return "resources/hardware/" + this.name;
    }
    private String getDescriptorPath() {
         return getHardwarePath() + "/descriptor.pb";
    }

    public String getName() {
        return this.name;
    }
    public HardwareTypeDescriptor getDesc() {
        return this.typeDescriptor;
    }

    public static List<HardwareType> getAll() {
        List<HardwareType> hardwareTypes = new ArrayList<>();

        final String path = "resources/hardware";
        final File jarFile = new File(HardwareType.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        try {
            if (jarFile.isFile()) {  // Run with JAR file
                final JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while (entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.startsWith(path + "/")) { //filter according to the path
                        System.out.println(name);
                        System.out.println("NOT IMPLEMENTED, FIX ME!! getAll() in HardwareType");
                        //TODO get name...
//                        hardwareTypes.add(new HardwareType(app.getName()));
                    }
                }
                jar.close();
            } else { // Run with IDE
                final URL url = Launcher.class.getResource("/" + path);
                if (url != null) {
                    try {
                        final File apps = new File(url.toURI());
                        for (File app : apps.listFiles()) {
//                            System.out.println(app);
                            hardwareTypes.add(new HardwareType(app.getName()));
                        }
                    } catch (URISyntaxException ex) {
                        // never happens
                    }
                }
            }
        } catch (IOException ex) {

        }

        return hardwareTypes;
    }
}
