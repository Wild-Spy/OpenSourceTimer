import TimerDescriptionLanguage.*;
import customwidgets.GraphPanel;
import customwidgets.NeedsUpdatedDataListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import min.*;
import min.Frame;
import org.joda.time.DateTime;
import org.joda.time.Interval;

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
    private DateTime deploymentTime = new DateTime();
    SerialHandler serialHandler;

    //private zoomLevel

    public frmMain() throws SerialPortException {
        btnUploadToDev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
//                btnUploadToDev.setText("Changed!!");
//                JOptionPane.showMessageDialog(null, "Hello World");
                //zoomIn();
            }
        });

        btnConnectToDev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //zoomOut();
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
//                    Rule r = RuleParser.parse(txtRules.getText());
//                    r.enable();
                    //enable the last rule
                    //r.get(r.size()-1).enable();
                } catch (Exception ex) {
                    System.out.println("error!");
                    ex.printStackTrace();
                }
                //graphPanel1.setFullWindow();
                regenGraphPoints(graphPanel1.getWindowStart(),
                        graphPanel1.getWindowStop());
            }
        });

        //btnReparse.doClick();

        String[] portNames = SerialPortList.getPortNames();
        System.out.println("Serial Port Names:");
        for (String name : portNames) {
            System.out.println(name);
        }
        if (portNames.length > 0) {
            serialHandler = new SerialHandler(portNames[0], 115200, new SerialReceivedFrameHandler());
        }
    }

    class SerialReceivedFrameHandler implements ReceivedFrameHandler {
        @Override
        public void handleReceivedFrame(Frame frame) {
            System.out.println(frame.toString());
        }
    }

    private void updateWindowLabel() {
        lblInfo.setText("<html>" + graphPanel1.getWindowStart().toString("dd/MM/yyyy HH:mm:ss") + "<br>" +
                        graphPanel1.getWindowStop().toString("dd/MM/yyyy HH:mm:ss") + "</html>");
    }

//    private void regenGraphPointsOld(DateTime windowStart, DateTime windowStop) {
//        List<Double> scores = new ArrayList<>();
//        List<Long> times = new ArrayList<>();
//        int maxDataPoints = 400;
//        Interval window = new Interval(windowStart, windowStop);
//        Period pointPeriod = new Interval(windowStart.getMillis(), windowStart.getMillis() + window.toDurationMillis()/maxDataPoints).toPeriod();
//        //update window to give a touch more scope
//        window = new Interval(windowStart.minus(pointPeriod), windowStop.plus(pointPeriod));
//
//        RuleRunner.resetAll();
//        DateTime simulatedNow = window.getStart();
//        for (int i = 0; i < maxDataPoints+4; i++) {
//            RuleRunner.run(simulatedNow);
//            times.add(simulatedNow.getMillis());
//            if (Channels.getInstance().get("1").getState() == ChannelState.DISABLED) {
//                scores.add(0.0);
//            } else {
//                scores.add(1.0);
//            }
//            simulatedNow = simulatedNow.plus(pointPeriod);
//        }
//        graphPanel1.updateData(scores, times);
//        graphPanel1.repaint();
//    }

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
        List<Double> scores1 = new ArrayList<>();
        List<Long> times1 = new ArrayList<>();
        List<Double> scores2 = new ArrayList<>();
        List<Long> times2 = new ArrayList<>();
        List<Double> scores3 = new ArrayList<>();
        List<Long> times3 = new ArrayList<>();
        List<Double> scores4 = new ArrayList<>();
        List<Long> times4 = new ArrayList<>();
        Interval window = new Interval(windowStart, windowStop);
        //Period pointPeriod = new Interval(windowStart.getMillis(), windowStart.getMillis() + window.toDurationMillis()/maxDataPoints).toPeriod();
        //update window to give a touch more scope
        //Interval pointWindow = new Interval(windowStart.minus(pointPeriod), windowStop.plus(pointPeriod));

        RuleRunner.resetAll(deploymentTime);
        //RuleRunner.disableAll();
        //Rules.getInstance().get("zzz").enable();
        RuleRunner.start(deploymentTime);
//        RuleRunner.printStatusToConsole(deploymentTime);
        if (window.getStart().isAfter(deploymentTime)) {
            //RuleRunner.stepTo(window.getStart());
            RuleRunner.startGraph(window.getStart());
//            RuleRunner.printStatusToConsole(window.getStart());
        }
        addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow());
        addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow());
        addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow());
        addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow());
        do {
            RuleRunner.step();
//            RuleRunner.printStatusToConsole(nextStateChange);
            addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow());
            addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow());
            addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow());
            addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow());
        } while (RuleRunner.getNow().isBefore(window.getEnd()));

        //Add one more so we fill the full graph.
        RuleRunner.step();
//        RuleRunner.printStatusToConsole(nextStateChange);
        addPointToChannelPoints(scores1, times1, "1", RuleRunner.getNow(), true);
        addPointToChannelPoints(scores2, times2, "2", RuleRunner.getNow(), true);
        addPointToChannelPoints(scores3, times3, "3", RuleRunner.getNow(), true);
        addPointToChannelPoints(scores4, times4, "4", RuleRunner.getNow(), true);


//        Rule r = Rules.getInstance().get("Rule_1");
//        r.reset();
//        r.update(deploymentTime);
//        if (window.getStart().isAfter(deploymentTime)) {
//            r.update(window.getStart());
//        }
//        scores.add(channelStateToDouble("1"));
//        times.add(window.getStart().getMillis());
//        DateTime nextStateChange = r.getNextStateChangeTime();
//
//        do {
//            r.update(nextStateChange);
//            scores.add(channelStateToDouble("1"));
//            times.add(nextStateChange.getMillis());
//            nextStateChange = r.getNextStateChangeTime();
//        } while (nextStateChange.isBefore(window.getEnd()));

//        //Add one more so we fill the full graph.
//        r.update(nextStateChange);
//        scores.add(channelStateToDouble("1"));
//        times.add(nextStateChange.getMillis());

        graphPanel1.getChannel(0).updateData(scores1, times1);
        graphPanel1.getChannel(1).updateData(scores2, times2);
        graphPanel1.getChannel(2).updateData(scores3, times3);
        graphPanel1.getChannel(3).updateData(scores4, times4);
    }

    private double channelStateToDouble(String chanName) {
        if (Channels.getInstance().get(chanName).getState() == ChannelState.DISABLED) {
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
//        int maxDataPoints = 400;
//        int maxScore = 10;
//        DateTime pointTime = new DateTime();
//        for (int i = 0; i < maxDataPoints; i++) {
//            scores.add(((double) ((int) (random.nextDouble()+0.5)) * maxScore));
//            pointTime = pointTime.plusSeconds(random.nextInt(100-1)+1);
//            times.add(pointTime.getMillis());
//        }
//        graphPanel1 = new GraphPanel(scores, times);
//        graphPanel1.setPreferredSize(new Dimension(80, 60));

//        try {
//            //Rule r = RuleParser.parse(this.txtRules.getText());
//            Rule r = RuleParser.parse("enable channel 1 for one hour every 2 days");
//            r.enable();
//        } catch (Exception ex) {
//            System.out.println("error!");
//            ex.printStackTrace();
//        }
//
//        DateTime simulatedNow = new DateTime();
//        for (int i = 0; i < maxDataPoints; i++) {
//            RuleRunner.run(simulatedNow);
//            times.add(simulatedNow.getMillis());
//            if (Channels.getInstance().get("1").getState() == ChannelState.DISABLED) {
//                scores.add(0.0);
//            } else {
//                scores.add(1.0);
//            }
//            simulatedNow = simulatedNow.plusMinutes(15);
//        }
        scores.add(0.0);
        times.add(new DateTime().getMillis());
        scores.add(1.0);
        times.add(new DateTime().plusSeconds(1).getMillis());
        graphPanel1 = new GraphPanel();
        graphPanel1.setPreferredSize(new Dimension(80, 60));
        graphPanel1.addChannel("Channel 1", scores, times);
        graphPanel1.addChannel("Channel 2", scores, times);
        graphPanel1.addChannel("Channel 3", scores, times);
        graphPanel1.addChannel("Channel 4", scores, times);
        graphPanel1.setFullWindow();
    }
}
