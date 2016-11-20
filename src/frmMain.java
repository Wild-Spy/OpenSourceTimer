import TimerDescriptionLanguage.*;
import customwidgets.GraphPanel;
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
import java.util.Map;

/**
 * Created by mcochrane on 30/10/16.
 */
public class frmMain {
    private JButton btnZoomIn;
    private JPanel panel1;
    private JTextArea txtRules;
    private GraphPanel graphPanel1;
    private JLabel lblInfo;
    private JButton btnZoomOut;
    private JButton btnReparse;
    private DateTime deploymentTime = new DateTime();

    private boolean mouseDragging = false;
    private long mouseDragTimeMs;

    //private zoomLevel

    public frmMain() {
        btnZoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
//                btnZoomIn.setText("Changed!!");
//                JOptionPane.showMessageDialog(null, "Hello World");
                //zoomIn();
            }
        });

        btnZoomOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //zoomOut();
            }
        });

        updateWindowLabel();

        graphPanel1.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                //long xPosMs = graphPanel1.getXPositionMs(mouseWheelEvent.getX());
                if (mouseWheelEvent.getWheelRotation() < 0) {
                    zoomIn(mouseWheelEvent.getX());
                } else if (mouseWheelEvent.getWheelRotation() > 0) {
                    zoomOut(mouseWheelEvent.getX());
                }
            }
        });
        graphPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                mouseDragging = true;
                mouseDragTimeMs = graphPanel1.getXPositionMs(mouseEvent.getX());
            }
        });
        graphPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                super.mouseReleased(mouseEvent);
                mouseDragging = false;
            }
        });

        graphPanel1.addMouseMotionListener(new MouseInputAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);
                if (mouseDragging) {
                    graphPanel1.pinWindowToPoint(mouseDragTimeMs, mouseEvent.getX());
                    regenGraphPoints(graphPanel1.getWindowStart(),
                            graphPanel1.getWindowStop());
                    updateWindowLabel();
                }
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

        btnReparse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Rules.resetInstance();
                try {
                    List<Rule> r = RuleParser.parseMultiple(txtRules.getText());
//                    Rule r = RuleParser.parse(txtRules.getText());
//                    r.enable();
                    //enable the last rule
                    r.get(r.size()-1).enable();
                } catch (Exception ex) {
                    System.out.println("error!");
                    ex.printStackTrace();
                }
                regenGraphPoints(graphPanel1.getWindowStart(),
                        graphPanel1.getWindowStop());
            }
        });

        //btnReparse.doClick();
    }

    private void updateWindowLabel() {
        lblInfo.setText("<html>" + graphPanel1.getWindowStart().toString("dd/MM/yyyy HH:mm:ss") + "<br>" +
                        graphPanel1.getWindowStop().toString("dd/MM/yyyy HH:mm:ss") + "</html>");
    }

    private void zoomIn(int xPosPixels) {
        zoomBy(xPosPixels, 0.2);

//        long start = graphPanel1.getWindowStart().getMillis();
//        long stop = graphPanel1.getWindowStop().getMillis();
//
//        long newstart = graphPanel1.getWindowStart().getMillis() + graphPanel1.getWindowDurationMs()/5
//
//        long newstart = start + (stop-start)/5;
//        long newstop = stop - (stop-start)/5;
//
//        graphPanel1.setWindow(new DateTime(newstart), new DateTime(newstop));
//        graphPanel1.setWindowCenter(xPosMs);
//        graphPanel1.repaint();
    }


    // zoomFactor is a number between -1 and 1.
    // Positive numbers zoom in, negative numbers zoom out.
    private void zoomBy(int xPosPixels, double zoomFactor) {
        long xPosMs = graphPanel1.getXPositionMs(xPosPixels);
        long duration = graphPanel1.getWindowDurationMs();
        long newDuration = (long)((double)duration/(1.0+zoomFactor));

        // Set new duration
        graphPanel1.setWindow(graphPanel1.getWindowStart().getMillis(),
                graphPanel1.getWindowStart().getMillis() + newDuration);

        graphPanel1.pinWindowToPoint(xPosMs, xPosPixels);

        regenGraphPoints(graphPanel1.getWindowStart(),
                graphPanel1.getWindowStart().plus(newDuration));

        updateWindowLabel();
    }

    private void zoomOut(int xPosPixels) {
        zoomBy(xPosPixels, -0.2);

//        long start = graphPanel1.getWindowStart().getMillis();
//        long stop = graphPanel1.getWindowStop().getMillis();
//
//        long newstart = start - (stop-start)/5;
//        long newstop = stop + (stop-start)/5;
//
//        graphPanel1.setWindow(new DateTime(newstart), new DateTime(newstop));
//        graphPanel1.setWindowCenter(xPosMs);
//        graphPanel1.repaint();
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

    private void regenGraphPoints(DateTime windowStart, DateTime windowStop) {
        List<Double> scores = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        Interval window = new Interval(windowStart, windowStop);
        //Period pointPeriod = new Interval(windowStart.getMillis(), windowStart.getMillis() + window.toDurationMillis()/maxDataPoints).toPeriod();
        //update window to give a touch more scope
        //Interval pointWindow = new Interval(windowStart.minus(pointPeriod), windowStop.plus(pointPeriod));



        RuleRunner.resetAll();
        RuleRunner.disableAll();
        Rules.getInstance().get("zzz").enable();
        RuleRunner.start(deploymentTime);
        RuleRunner.printStatusToConsole(deploymentTime);
        if (window.getStart().isAfter(deploymentTime)) {
            //RuleRunner.stepTo(window.getStart());
            RuleRunner.startGraph(window.getStart());
            RuleRunner.printStatusToConsole(window.getStart());
        }
        scores.add(channelStateToDouble("1"));
        times.add(window.getStartMillis());
        DateTime nextStateChange;
        do {
            nextStateChange = RuleRunner.step();
            RuleRunner.printStatusToConsole(nextStateChange);
            scores.add(channelStateToDouble("1"));
            times.add(nextStateChange.getMillis());
        } while (nextStateChange.isBefore(window.getEnd()));

        //Add one more so we fill the full graph.
        nextStateChange = RuleRunner.step();
        RuleRunner.printStatusToConsole(nextStateChange);
        scores.add(channelStateToDouble("1"));
        times.add(nextStateChange.getMillis());


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

        graphPanel1.updateData(scores, times);
        graphPanel1.repaint();
    }

    private double channelStateToDouble(String chanName) {
        if (Channels.getInstance().get(chanName).getState() == ChannelState.DISABLED) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    public static void main(String[] args) {
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
        graphPanel1 = new GraphPanel(scores, times);
        graphPanel1.setPreferredSize(new Dimension(80, 60));

    }
}
