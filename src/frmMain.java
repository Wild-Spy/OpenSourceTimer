import TimerDescriptionLanguage.*;
import customwidgets.GraphPanel;
import customwidgets.GraphPoint;
import customwidgets.NeedsUpdatedDataListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import min.*;
import min.Frame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joou.UByte;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcochrane on 30/10/16.
 */
public class frmMain {
    private JButton btnUploadToDev;
    private JPanel panel1;
    private JTextArea txtRules;
    private GraphPanel graphPanel1;
    private JLabel lblInfo;
    private JButton btnConnectToDev;
    private JButton btnReparse;
    private DateTime deploymentTime;
    SerialHandler serialHandler;

    //private zoomLevel

    private void txRule(List<UByte> compiledRule) {
        List<UByte> data;
        //Tx Start Frame
        data = new ArrayList<>();
        data.addAll(SerialHandler.min_encode_16((short)compiledRule.size()));
        Frame startFrame = new Frame(frmMain.this.serialHandler, UByte.valueOf(0x03), data);
        startFrame.transmit();

        int startIndex = 0;
        while (startIndex < compiledRule.size()) {
            int endIndex = startIndex + 32;
            if (endIndex > compiledRule.size()) endIndex = compiledRule.size();
            data = compiledRule.subList(startIndex, endIndex);
            Frame part = new Frame(frmMain.this.serialHandler, UByte.valueOf(0x04), data);
            part.transmit();
            startIndex += 32;
        }

        data = new ArrayList<>();
        data.add(UByte.valueOf(0));
        Frame endFrame = new Frame(frmMain.this.serialHandler, UByte.valueOf(0x05), data);
        endFrame.transmit();
    }

    public frmMain() throws SerialPortException {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        this.deploymentTime = new DateTime(2016, 1, 1, 0, 0, 0);
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 1, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 1, 10, 0, 0, 0)));
        SimulatedEvents.getInstance().addEvent(new SimulatedEvent("event1",
                new DateTime(2017, 3, 1, 0, 0, 0)));

        btnUploadToDev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

//                List<UByte> data = new ArrayList<>();
//                data.add(UByte.valueOf(1));
//                data.add(UByte.valueOf(5));
//                data.add(UByte.valueOf(2));
//                Frame pingFrame = new Frame(frmMain.this.serialHandler, UByte.valueOf(0x02), data); //ping
//                pingFrame.transmit();
//                return;

                List<List<UByte>> allCompiled = Rules.getInstance().getAllCompiled();
                for (List<UByte> r : allCompiled) {
                    txRule(r);
                }
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
                        serialHandler = new SerialHandler(portNames[0], 115200, new SerialReceivedFrameHandler());
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

            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {

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
                Rules.resetInstance();
                try {
                    List<Rule> r = RuleParser.parseMultiple(txtRules.getText());
                } catch (Exception ex) {
                    System.out.println("error!");
                    ex.printStackTrace();
                }

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
        });

        //btnReparse.doClick();
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
//        List<Double> scores1 = new ArrayList<>();
//        List<Long> times1 = new ArrayList<>();
//        List<Double> scores2 = new ArrayList<>();
//        List<Long> times2 = new ArrayList<>();
//        List<Double> scores3 = new ArrayList<>();
//        List<Long> times3 = new ArrayList<>();
//        List<Double> scores4 = new ArrayList<>();
//        List<Long> times4 = new ArrayList<>();
        Interval window = new Interval(windowStart, windowStop);
        //Period pointPeriod = new Interval(windowStart.getMillis(), windowStart.getMillis() + window.toDurationMillis()/maxDataPoints).toPeriod();
        //update window to give a touch more scope
        //Interval pointWindow = new Interval(windowStart.minus(pointPeriod), windowStop.plus(pointPeriod));
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

        //graphPanel1.clearAllGraphs();

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

//        RuleRunner.startGraph(deploymentTime, window.getStart());
////        RuleRunner.resetAll(deploymentTime);
////        //RuleRunner.disableAll();
////        //Rules.getInstance().get("zzz").enable();
////        RuleRunner.start(deploymentTime);
//////        RuleRunner.printStatusToConsole(deploymentTime);
////        if (window.getStart().isAfter(deploymentTime)) {
////            //RuleRunner.stepTo(window.getStart());
////            RuleRunner.startGraph(window.getStart());
//////            RuleRunner.printStatusToConsole(window.getStart());
////        }
//        addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow());
//        addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow());
//        addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow());
//        addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow());
//        do {
//            RuleRunner.step();
////            RuleRunner.printStatusToConsole(nextStateChange);
//            addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow());
//            addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow());
//            addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow());
//            addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow());
//        } while (RuleRunner.getNow().isBefore(window.getEnd()));
//
//        //Add one more so we fill the full graph.
//        RuleRunner.step();
////        RuleRunner.printStatusToConsole(nextStateChange);
//        addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow(), true);
//        addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow(), true);
//        addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow(), true);
//        addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow(), true);
//
//        graphPanel1.getGraph(0).updateData(scores1, times1);
//        graphPanel1.getGraph(1).updateData(scores2, times2);
//        graphPanel1.getGraph(2).updateData(scores3, times3);
//        graphPanel1.getGraph(3).updateData(scores4, times4);
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
        JFrame frame = new JFrame("App");
        frame.setContentPane(new frmMain().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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
        graphPanel1.setFullWindow();
    }
}
