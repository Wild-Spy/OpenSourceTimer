import TimerDescriptionLanguage.*;
import customwidgets.GraphPanel;
import customwidgets.GraphPoint;
import customwidgets.listeners.NeedsUpdatedDataListener;
import customwidgets.SimulatedEventMutableTreeNode;
import jsmaz.Smaz;
import jssc.SerialPortException;
import jssc.SerialPortList;
import min.*;
import min.Frame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joou.UByte;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Created by mcochrane on 30/10/16.
 */
public class FrmMain implements Runnable {
    private JButton btnUploadToDev;
    private JPanel panelRoot;
    private JTextArea txtRules;
    private GraphPanel graphPanel1;
    private JLabel lblInfo;
    private JButton btnConnectToDev;
    private JButton btnReparse;
    private JButton btn2;
    private JPanel statusBar;
    private JLabel statusBarLabel;
    private JTabbedPane tabbedPane1;
    private JTree treeEvents;
    private JSplitPane splitPaneMain;
    private DateTime deploymentTime;
    SerialHandler serialHandler = null;
    private JFrame frameRoot;
    FrmDevice frmDevice;

    //private zoomLevel

    private void txRule(List<UByte> compiledRule) {
        List<UByte> data;
        //Tx Start Frame
        data = new ArrayList<>();
        data.addAll(SerialHandler.min_encode_16((short)compiledRule.size()));
        Frame startFrame = new Frame(FrmMain.this.serialHandler, UByte.valueOf(0x03), data);
        startFrame.transmit();

        int startIndex = 0;
        while (startIndex < compiledRule.size()) {
            int endIndex = startIndex + 32;
            if (endIndex > compiledRule.size()) endIndex = compiledRule.size();
            data = compiledRule.subList(startIndex, endIndex);
            Frame part = new Frame(FrmMain.this.serialHandler, UByte.valueOf(0x04), data);
            part.transmit();
            startIndex += 32;
        }

        data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        Frame endFrame = new Frame(FrmMain.this.serialHandler, UByte.valueOf(0x05), data);
        endFrame.transmit();
    }

    private void setUiColors() {
        try {
            UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(frameRoot);
    }

    public FrmMain() throws SerialPortException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    private class SerialReceivedFrameHandler implements ReceivedFrameHandler {
        @Override
        public void handleReceivedFrame(Frame frame) {
            System.out.println(frame.toString());
        }
    }

    private void updateWindowLabel() {
        lblInfo.setText("<html>" + graphPanel1.getWindowStart().toString("dd/MM/yyyy HH:mm:ss") + "<br>" +
                        graphPanel1.getWindowStop().toString("dd/MM/yyyy HH:mm:ss") + "</html>");
    }

    private boolean addPointToChannelPoints(List<Double> scores, List<Long> times, String chanName, DateTime time) {
        return addPointToChannelPoints(scores, times, chanName, time, false);
    }

    private boolean addPointToChannelPoints(List<Double> scores, List<Long> times, String chanName,
                                            DateTime time, boolean forceAdd) {
        Double newScore = channelStateToDouble(chanName);

        if (scores.size() > 0 && !forceAdd) {
            Double oldScore = scores.get(scores.size() - 1);
            if (oldScore.equals(newScore)) return false;
        }
        scores.add(newScore);
        times.add(time.getMillis());
        return true;
    }

    private void regenGraphPoints(DateTime windowStart, DateTime windowStop) {
        List<List<Double>> scoresLists = new ArrayList<>();
        List<List<Long>> timesLists = new ArrayList<>();
        Interval window = new Interval(windowStart, windowStop);
        int channelCount = Channels.getInstance().getCount();

        for (int i = 0; i < channelCount; i++) {
            scoresLists.add(new ArrayList<>());
            timesLists.add(new ArrayList<>());
        }
        for (int i = 0; i < Rules.getInstance().count(); i++) {
            scoresLists.add(new ArrayList<>());
            timesLists.add(new ArrayList<>());
        }

        List<List<GraphPoint>> pointsLists = RuleRunner.generateGraphPoints(deploymentTime, window);

        for (int i = 0; i < pointsLists.size(); i++) {
            List<GraphPoint> points = pointsLists.get(i);
            for (GraphPoint p : points) {
                scoresLists.get(i).add(channelStateToDouble(p.getState()));
                timesLists.get(i).add(p.getDateTime().getMillis());
            }
        }

        for (int i = 0; i < channelCount; i++) {
            //Channel chan = Channels.getInstance().get(i);
            graphPanel1.getGraph(i).updateData(scoresLists.get(i),
                    timesLists.get(i));
        }
        for (int i = 0; i < Rules.getInstance().count(); i++) {
            //Rule rule = Rules.getInstance().get(i);
            graphPanel1.getGraph(i+channelCount).updateData(scoresLists.get(i+channelCount),
                    timesLists.get(i+channelCount));
        }
    }

    private double channelStateToDouble(String chanName) {
        if (Channels.getInstance().get(chanName).getState() == ChannelState.DISABLED) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    private double channelStateToDouble(ChannelState chanState) {
        if (chanState == ChannelState.DISABLED) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new FrmMain());
    }

    @Override
    public void run() {
        frameRoot = new JFrame("WS Open Source Timer");
        ImageIcon icon = new ImageIcon("src/resources/timer-icon.png");
        frameRoot.setIconImage(icon.getImage());
        frameRoot.setContentPane(panelRoot);
        frameRoot.setJMenuBar(createMenuBar());
        frameRoot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameRoot.pack();
        frameRoot.setLocationRelativeTo(null);
        frameRoot.setVisible(true);
        setUiColors();
//        frameRoot.setBackground(GraphPanel.backgroundColor);
//        frameRoot.getContentPane().setBackground(GraphPanel.backgroundColor);
//        setUiColors();

        DateTimeZone.setDefault(DateTimeZone.UTC);
        this.deploymentTime = new DateTime(2000, 1, 1, 0, 0, 0);
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("1",
                new DateTime(2017, 1, 3, 0, 0, 0), graphPanel1));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("1",
                new DateTime(2017, 1, 10, 0, 0, 0), graphPanel1));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("1",
                new DateTime(2017, 3, 1, 0, 0, 0), graphPanel1));

        SimulatedEvents.getInstance().updateUiTreeView(treeEvents);
        System.out.print(panelRoot.getBackground());

        treeEvents.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                if (mouseEvent.getButton() != 1) return;
                TreePath tp = treeEvents.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (tp != null) {
//                    if (((SimulatedEventMutableTreeNode) tp.getLastPathComponent()).getUserObject().getClass() == SimulatedEvent.class) {
                    if (tp.getLastPathComponent().getClass() == SimulatedEventMutableTreeNode.class) {
                        SimulatedEvents.getInstance().setAllMarkersNotSelected();
                        SimulatedEvent event = ((SimulatedEventMutableTreeNode) tp.getLastPathComponent()).getEvent();
                        event.getMarker().setSelected(true);
//                        graphPanel1.scrollTo(event.getTime(), Period.hours(10));
                        if (!event.getMarker().isVisible()) {
                            graphPanel1.scrollTo(event.getTime(), graphPanel1.getWindowDuration());
                        }
                    }
                }
            }
        });

        btn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serialHandler.frame_transmitter.sendGetRuleCount();
            }
        });

        btnUploadToDev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serialHandler.frame_transmitter.sendPrintEeprom((short) 0,128);
            }
        });

        btnConnectToDev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String[] portNames = SerialPortList.getPortNames();
                System.out.println("Serial Port Names:");
                for (String name : portNames) {
                    System.out.println(name);
                }
                if (portNames.length > 0) {
                    try {
                        //serialHandler = new SerialHandler(portNames[0], 115200, new SerialReceivedFrameHandler());
                        serialHandler = new SerialHandler(portNames[0], 115200, new FrameReceiver());

                    } catch (SerialPortException e) {
                        System.out.println("Couldn't connect to device");
                    }
                }
            }
        });

        updateWindowLabel();

        graphPanel1.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                updateWindowLabel();
            }
        });

        graphPanel1.addMouseMotionListener(new MouseInputAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseDragged(mouseEvent);
                updateWindowLabel();
            }
        });

        txtRules.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                reparse();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                reparse();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {

            }
        });

        graphPanel1.addNeedsUpdatedDataListener(new NeedsUpdatedDataListener() {
            @Override
            public void needsUpdatedData(Interval window) {
                regenGraphPoints(window.getStart(), window.getEnd());
            }
        });

        btnReparse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                reparse();
            }
        });

        //btnReparse.doClick();
    }

    private void reparse() {
        Rules.resetInstance();
        try {
            List<Rule> r = RuleParser.parseMultiple(txtRules.getText());
        } catch (Exception ex) {
            System.out.println("error!");
            ex.printStackTrace();
            txtRules.setBackground(Color.red);
            txtRules.setToolTipText(ex.toString());
            return;
        }
        txtRules.setBackground(Color.gray);
        txtRules.setToolTipText("");

        System.out.print(Rules.getInstance().get(0).compile().toString());
        Smaz smaz = new Smaz();
        byte[] compressed = smaz.compress(txtRules.getText());
        //smaz.decompress(compressed)
        System.out.print(compressed.toString());
        System.out.printf("Smaz compression.  Orig: %d, compressed: %d", txtRules.getText().length(), compressed.length);
//        System.out.printf("Smaz compression.  Orig: %d, compressed: %d, %f", txtRules.getText().length(), compressed.length, compressed.length/txtRules.getText().length());

        int channelCount = Channels.getInstance().getCount();

        List<Double> scores = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        scores.add(0.0);
        times.add(new DateTime().getMillis());
        scores.add(1.0);
        times.add(new DateTime().plusSeconds(1).getMillis());

        graphPanel1.clearAllGraphs();
        for (int i = 0; i < channelCount; i++) {
            //Channel chan = Channels.getInstance().get(i);
            graphPanel1.addChannelGraph("Channel " + (i+1), scores, times);
        }
        for (int i = 0; i < Rules.getInstance().count(); i++) {
            Rule rule = Rules.getInstance().get(i);
            graphPanel1.addRuleGraph(rule.getName(), scores, times);
        }

        regenGraphPoints(graphPanel1.getWindowStart(),
                graphPanel1.getWindowStop());
    }

    private void createUIComponents() {
//        Random random = new Random();
        List<Double> scores = new ArrayList<>();
        List<Long> times = new ArrayList<>();

        scores.add(0.0);
        times.add(new DateTime().getMillis());
        scores.add(1.0);
        times.add(new DateTime().plusSeconds(1).getMillis());
        graphPanel1 = new GraphPanel();
        graphPanel1.setPreferredSize(new Dimension(80, 60));
        graphPanel1.addChannelGraph("Channel 1", scores, times);
        graphPanel1.addChannelGraph("Channel 2", scores, times);
        graphPanel1.addChannelGraph("Channel 3", scores, times);
        graphPanel1.addChannelGraph("Channel 4", scores, times);
        graphPanel1.addRuleGraph("a", scores, times);
        graphPanel1.addRuleGraph("B", scores, times);
//        graphPanel1.setFullWindow();
//        graphPanel1.scrollTo(DateTime.now(), Duration.standardDays(10));
        graphPanel1.scrollTo(new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 5, 1, 1, 0, 0));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menu_bar = new JMenuBar();
//        menu_bar.setBackground(GraphPanel.backgroundColor);
//        menu_bar.setForeground(GraphPanel.gridColor);
        JMenu menu_file = new JMenu("File");

        JMenuItem menu_item_new = new JMenuItem("New");
        menu_item_new.addActionListener(new MenuNewActionListener());
        menu_file.add(menu_item_new);

        JMenuItem menu_item_open = new JMenuItem("Open");
        menu_item_open.addActionListener(new MenuOpenActionListener());
        menu_file.add(menu_item_open);

        JMenuItem menu_item_save = new JMenuItem("Save");
        menu_item_save.addActionListener(new MenuSaveActionListener());
        menu_file.add(menu_item_save);

        menu_bar.add(menu_file);

        //
        // Device Menu
        //
        JMenu menu_device = new JMenu("Device");

        // Show Device Window
        JMenuItem menu_item_show_dev_window = new JMenuItem("Show Device Window");
        menu_item_show_dev_window.addActionListener(new MenuDevWindowShowActionListener());
        menu_device.add(menu_item_show_dev_window);

        // Connect
        JMenuItem menu_item_connect = new JMenuItem("Connect");
        menu_item_connect.addActionListener(new MenuDevConnectActionListener());
        menu_device.add(menu_item_connect);

        // Upload Rules To Device
        JMenuItem menu_item_upload = new JMenuItem("Upload Rules To Device");
        menu_item_upload.addActionListener(new MenuDevUploadRulesActionListener());
        menu_device.add(menu_item_upload);

        JMenuItem menu_item_erase = new JMenuItem("Erase All Rules From Device");
        menu_item_erase.addActionListener(new MenuDevEraseRulesActionListener());
        menu_device.add(menu_item_erase);

        JMenuItem menu_item_set_time = new JMenuItem("Set Device Time");
        menu_item_set_time.addActionListener(new MenuDevSetTimeActionListener());
        menu_device.add(menu_item_set_time);

        JMenuItem menu_item_get_time = new JMenuItem("Get Device Time");
        menu_item_get_time.addActionListener(new MenuDevGetTimeActionListener());
        menu_device.add(menu_item_get_time);

        JMenuItem menu_item_update_firmware = new JMenuItem("Update Firmware");
        menu_item_update_firmware.addActionListener(new MenuDevUpdateFirmwareActionListener());
        menu_device.add(menu_item_update_firmware);

        menu_bar.add(menu_device);

        return menu_bar;
    }

    class MenuNewActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

        }
    }

    class MenuOpenActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
//            ((JMenuItem)actionEvent.getSource()).setText(".........");

            if (JOptionPane.showConfirmDialog(null, "All unsaved changes will be lost.  Are you sure you want to load a file?", "Warning!", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;

            JFileChooser loadChooser = new JFileChooser();
            loadChooser.setDialogTitle("Choose which file to load.");
            loadChooser.setFileFilter(new FileNameExtensionFilter("Timer Description Language Files", "tdl"));
            if (loadChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
            File loadFile = loadChooser.getSelectedFile();

            try {
                String contents = new String(Files.readAllBytes(loadFile.toPath()));
                txtRules.setText(contents);
            } catch (Exception ex) {
            }
        }
    }

    class MenuSaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Choose where to save your file.");
            saveChooser.setFileFilter(new FileNameExtensionFilter("Timer Description Language Files", "tdl"));
            if (saveChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
            File saveFile = saveChooser.getSelectedFile();

            // Ensure correct extension
            if (!saveFile.getName().endsWith(".tdl")) {
                saveFile = new File(saveFile.toString() + ".tdl");
            }

            try {
                PrintWriter out = new PrintWriter(saveFile);
                out.print(txtRules.getText());
                out.close();
            } catch (Exception ex) {
            }
        }
    }

    private void DisconnectDevice() {
        if (serialHandler == null) return;
        serialHandler.Disconnect();
        serialHandler = null;
        statusBarLabel.setText("Not Connected");
    }

    class MenuDevWindowShowActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler == null) return;

            if (frmDevice == null)
                frmDevice = new FrmDevice(FrmMain.this);

            frmDevice.setVisible(true);
            final Long[] devType = {null};

            serialHandler.received_frame_handler.setResponseCallback(new ReceivedFrameHandler() {
                @Override
                public void handleReceivedFrame(Frame frame) {
                    if (frame.get_id().intValue() != FrameReceiver.MIN_ID_RESPONSE_GET_DEVICE_TYPE) return;
                    devType[0] = SerialHandler.min_decode_unsigned(frame.get_payload());
                    serialHandler.received_frame_handler.deleteResponseCallback();
                }
            });

            String dev_type_name = serialHandler.frame_transmitter.sendGetDeviceTypeBlocking();

            if (dev_type_name.isEmpty()) return;

            HardwareType hw_type = new HardwareType(dev_type_name);
            if (hw_type.typeDescriptor == null) return;

            frmDevice.setHardwareType(hw_type);
        }
    }

    class SerialHandlerDisconnect implements SerialHandlerDisconnectCallback {
        @Override
        public void handleReceivedFrame(SerialHandler sh) {
            DisconnectDevice();
//            JOptionPane.showMessageDialog(null, "Device disconnected.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    class MenuDevConnectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler != null) {
                //already connected!
                int result = JOptionPane.showConfirmDialog(null, "Already connected to a device.  Do you want to disconnect from it?");
                if (result != 0) return;
                DisconnectDevice();
            }

            OSTDeviceSelector dev_sel = new OSTDeviceSelector();
            dev_sel.setVisible(true);
            System.out.printf("Selected port '%s'\r\n", dev_sel.result);

            if (dev_sel.result.isEmpty()) return;

            try {
                serialHandler = new SerialHandler(dev_sel.result, 115200, new FrameReceiver());
                serialHandler.setDisconnectCallback(new SerialHandlerDisconnect());
                statusBarLabel.setText("Connected to '" + dev_sel.result + "'");
                serialHandler.frame_transmitter.sendGetDeviceTypeBlocking(); // Seems to fail on first request so make a request so the next one works.
            } catch (SerialPortException e) {
                System.out.println("Couldn't connect to device");
            }

//            DeviceSelector dev_sel = new DeviceSelector();
//            dev_sel.setVisible(true);
//            System.out.printf("Selected port '%s'\r\n", dev_sel.result);
//
//            if (dev_sel.result.isEmpty()) return;
//
//            try {
//                serialHandler = new SerialHandler(dev_sel.result, 115200, new FrameReceiver());
//                statusBarLabel.setText("Connected to '" + dev_sel.result + "'");
//            } catch (SerialPortException e) {
//                System.out.println("Couldn't connect to device");
//            }

        }
    }

    class MenuDevSetTimeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler == null) {
                JOptionPane.showMessageDialog(null, "Not connected to a device.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DateTime d = new DateTime(TimeZone.getDefault().getOffset(DateTime.now().getMillis()) + DateTime.now().getMillis());
//            DateTime d = new DateTime(new DateTime(2017, 8, 31, 23, 59, 0).getMillis());
            serialHandler.frame_transmitter.sendSetRtcTime(d);
        }
    }

    class MenuDevGetTimeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler == null) {
                JOptionPane.showMessageDialog(null, "Not connected to a device.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                DateTime dt = serialHandler.frame_transmitter.getRtcTimeBlocking(6000);
                if (dt == null) {
                    JOptionPane.showMessageDialog(null, "Failed to get time from device.  Please try again." , "Time", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                graphPanel1.getMarker(0).setTime(dt);
                JOptionPane.showMessageDialog(null, "RTC Time: " + dt.toString() , "Time", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {

            }

        }
    }

    private String execSystemCommandLinux(String cmd) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            proc.waitFor();
            StringBuffer output = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            return output.toString();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public interface StringCallback {
        void stringCallback(String line);
    }


    private void execSystemCmdLinuxWithLiveResponseCallback(String command, StringCallback stdInputLineHandler,
                                                            StringCallback stdErrorLineHandler) {
        try {
            Runtime rt = Runtime.getRuntime();
//            String[] commands = {"system.exe","-get t"};
            Process proc = rt.exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // read the output from the command
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                stdInputLineHandler.stringCallback(s);
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                stdErrorLineHandler.stringCallback(s);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean checkBOSSACmdResponse(String response) {
        String expected = "Basic Open Source SAM-BA Application (BOSSA) Version 1.8";
        String[] parts = response.split("\n");
        if (parts.length < 2) return false;
        if (!Objects.equals(parts[1], expected)) return false;
        return true;
    }

    private String findBOSSAExecutable() {
        if (OSValidator.isUnix()) {
            String cmdName = "bossac";
            String response = "";
            while ( ((response = execSystemCommandLinux(cmdName + " -h")) == null) || !checkBOSSACmdResponse(response) ) {
                JFileChooser BOSSAExecChooser = new JFileChooser();
                BOSSAExecChooser.setDialogTitle("Could not find bossac executable, please specify its path.");
                //TODO: Do you want to download it?
                FileFilter filterWithoutExtension = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        // This will display only the files without "."
                        return Objects.equals(f.getName(), "bossac") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "bossac executable";
                    }
                };
                BOSSAExecChooser.setFileFilter(filterWithoutExtension);
                if (BOSSAExecChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return null;
                cmdName = BOSSAExecChooser.getSelectedFile().getPath();
            }
            return cmdName;
        } else if (OSValidator.isWindows()) {

        } else if (OSValidator.isMac()) {

        }
        return null;
    }


    private void programFirmware(String bossaExec, String firmwareFile) {
        DisconnectDevice();
        JOptionPane.showMessageDialog(null, "Please restart-device in firmware upload mode by powering it on while holding the Firmware Update button.", "Restart Device", JOptionPane.INFORMATION_MESSAGE);

        DeviceSelector dev_sel = new DeviceSelector();
        dev_sel.setVisible(true);
        System.out.printf("Selected port for firmware upload '%s'\r\n", dev_sel.result);

        if (dev_sel.result.isEmpty()) return;

        if (OSValidator.isUnix()) {
            String args = "-e -w -v -R --port=" + dev_sel.result + " " + firmwareFile + "";
            String command = bossaExec + " " + args;
            System.out.printf("Running Command '%s'\r\n", command);

            WaitDialog wait_dialog = new WaitDialog("Erasing flash", 3);

            final boolean[] wasError = {false};
            String[] errorStr = {""};

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

                @Override
                protected Boolean doInBackground() throws InterruptedException {
                    execSystemCmdLinuxWithLiveResponseCallback(command, new StringCallback() {
                        @Override
                        public void stringCallback(String line) {
                            String currentTaskStr = "";

                            if (wait_dialog.get_progress() == 0) {
                                currentTaskStr = "Erasing flash";
                            } else if (wait_dialog.get_progress() == 1) {
                                currentTaskStr = "Writing flash";
                            } else if (wait_dialog.get_progress() == 2) {
                                currentTaskStr = "Verifying flash";
                            }

                            wait_dialog.update_text(currentTaskStr + ": " + line);
                            if (line.startsWith("Done in ")) {
                                wait_dialog.increment_update();
                            }
//                            System.out.println(line);
                        }
                    }, new StringCallback() {
                        @Override
                        public void stringCallback(String line) {
                            System.out.printf("err: %s\n", line);
                            wasError[0] = true;
                            errorStr[0] +=  line + "\n";
                        }
                    });
                    return true;
                }
                @Override
                protected void done() {
                    wait_dialog.dispose();
                }
            };

            worker.execute();
            wait_dialog.setVisible(true);

            if (wasError[0]) {
                JOptionPane.showMessageDialog(null, "Error Programming Device: \r\n" + errorStr[0] , "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Successfully wrote firmware to device.  You can now re-connect.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (OSValidator.isWindows()) {

        } else if (OSValidator.isMac()) {

        }
    }

    class MenuDevUpdateFirmwareActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
//            if (serialHandler == null) {
//                JOptionPane.showMessageDialog(null, "Not connected to a device.", "Warning", JOptionPane.WARNING_MESSAGE);
//                return;
//            }

            JFileChooser firmwareChooser = new JFileChooser();
            firmwareChooser.setDialogTitle("Choose firmware file to upload");
            //TODO: do you want to download the latest version?
            firmwareChooser.setFileFilter(new FileNameExtensionFilter("Firmware Binary", "bin"));
            if (firmwareChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
            File firmwareFile = firmwareChooser.getSelectedFile();

            System.out.printf("File Selected: %s\r\n", firmwareFile.getPath());

            // Try and find bossa console executable
            String bossaExec = findBOSSAExecutable();
            programFirmware(bossaExec, firmwareFile.getPath());
        }
    }

    class MenuDevEraseRulesActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler == null) {
                JOptionPane.showMessageDialog(null, "Not connected to a device.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //Ask are you sure?
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to erase all rules from the device?");
            if (result != 0) return;

            serialHandler.frame_transmitter.sendEraseAllRules();
            if (serialHandler.received_frame_handler.waitForResponse(3000) != FrameReceiver.ResponseType.Ack) {
                System.out.printf("Failed to erase all rules.\r\n");
                return;
            }

        }
    }

    class MenuDevUploadRulesActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (serialHandler == null) {
                JOptionPane.showMessageDialog(null, "Not connected to a device.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            WaitDialog wait_dialog = new WaitDialog("Uploading rules to device...", Rules.getInstance().count());

            //serialHandler.show_raw = true;

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

                @Override
                protected Boolean doInBackground() throws InterruptedException {
                    FrameReceiver.ResponseType response;

                    serialHandler.frame_transmitter.sendEraseAllRules();
                    if (serialHandler.received_frame_handler.waitForResponse(3000) != FrameReceiver.ResponseType.Ack) {
                        System.out.printf("Failed to erase all rules.\r\n");
                        return false;
                    }

                    List<Rule> allRules = Rules.getInstance().getAll();

                    // Must sort the list so we upload the rules in order
                    allRules.sort(new Comparator<Rule>() {
                        public int compare(Rule r1, Rule r2) {
                            int id1 = r1.getId();
                            int id2 = r2.getId();
                            return id1 < id2 ? -1
                                    : id2 > id1 ? 1
                                    : 0;
                        }
                    });

                    for (Rule r : allRules) {
                        System.out.printf("Sending rule %d\r\n", r.getId());
                        boolean sent_rule_successfully = false;
                        int send_rule_attempts_remaining = 5;
                        while (send_rule_attempts_remaining-- > 0 && !sent_rule_successfully) {
                            serialHandler.frame_transmitter.sendRule(r);

                            response = serialHandler.received_frame_handler.waitForResponse(3000);

                            if (response == FrameReceiver.ResponseType.Ack) {
                                sent_rule_successfully = true;
                                wait_dialog.update_text("Sent rule " + r.getId() + " successfully.");
                            } else {
                                //retry sending?
                                sent_rule_successfully = false;
                                wait_dialog.update_text("Faild to send rule " + r.getId() + ".  Attempts remaining: " + send_rule_attempts_remaining);
                            }
                        }
                        if (!sent_rule_successfully) return false;
                        wait_dialog.increment_update();
                    }

                    System.out.printf("Save rules...\r\n");
                    serialHandler.frame_transmitter.sendSaveRules();
                    if (serialHandler.received_frame_handler.waitForResponse(5000) != FrameReceiver.ResponseType.Ack) {
                        System.out.printf("Failed to save rules.\r\n");
                        return false;
                    }

                    return true;
                }
                @Override
                protected void done() {
                    wait_dialog.dispose();
                }
            };

            System.out.printf("worker.execute();\r\n");
            worker.execute();
            System.out.printf("wait_dialog.setVisible(true);\r\n");
            wait_dialog.setVisible(true);

//            while (!worker.isDone()) { //1 second timeout
//                try {
//                    Thread.sleep(10); //sleep for 10ms
//                } catch (Exception e) {}
//            }

            try {
                System.out.printf("Boolean result = worker.get();\r\n");
                Boolean result = worker.get();
                if (result) {
                    JOptionPane.showMessageDialog(null, "Successfully transmitted " + Rules.getInstance().count() + " rules.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error transmitting " + Rules.getInstance().count() + " rules.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error transmitting " + Rules.getInstance().count() + " rules. Exception thrown.", "Info", JOptionPane.ERROR_MESSAGE);
            }
            return;

//            DeviceSelector dev_sel = new DeviceSelector();
//            dev_sel.setVisible(true);
//            System.out.printf("Selected port '%s'\r\n", dev_sel.result);
//
//            if (dev_sel.result.isEmpty()) return;
//
//            try {
//                serialHandler = new SerialHandler(dev_sel.result, 115200, new FrameReceiver());
//                statusBarLabel.setText("Connected to '" + dev_sel.result + "'");
//            } catch (SerialPortException e) {
//                System.out.println("Couldn't connect to device");
//            }

        }
    }
}
