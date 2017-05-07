package customwidgets;

import customwidgets.listeners.NeedsUpdatedDataListener;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

/**
 * Created by mcochrane on 30/10/16.
 */

public class GraphPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {

    private int width = 800;
    private int heigth = 400;

    static final int timeBarHeight = 50;
    static final int leftPanelWidth = 100;
    static final int channelHeight = 75;
    static final int channelMeasurementBarHeight = 10;


    static final int timeBarMinorAxisHeight = 30;
    static final int timeBarMajorAxisHeight = 20;


//    private int padding = 25;
//    private int labelPadding = 25;
    public static final Color lineColor = new Color(145, 145, 145);
    public static final Color pointColor = new Color(100, 100, 100, 180);
    public static final Color gridColor = new Color(200, 200, 200, 200);
    public static final Color backgroundColor = new Color(40, 40, 40);
    public static final Color graphBackgroundColor = new Color(35, 35, 35);
    static final Stroke GRAPH_STROKE = new BasicStroke(1f);
    private int pointWidth = 6;
    private int numberYDivisions = 10;

    private GraphPanelXAxis xAxis = new GraphPanelXAxis(this);
    private List<SubGraph> subGraphs = new ArrayList<>();
    private List<GraphMarker> markers = new ArrayList<>();
    Long[] tWindow = new Long[2];
    private NeedsUpdatedDataListener needsUpdatedDataListener = null;
    private boolean mouseDragging = false;
    private Long mouseDragTimeMs;
    private boolean mouseScrollingXAxis = false;
    private Point mouseDragStartPoint;

//    Long[] fullTRange = new Long[2];

    final Period[] possibleXAxisTickPeriods = {Period.seconds(1), Period.seconds(15),
            Period.minutes(1), Period.minutes(15), Period.hours(1), Period.hours(6),
            Period.days(1), /*Period.days(5),*/ Period.months(1), Period.years(1)};
//            Period.days(1), Period.weeks(1), Period.months(1), Period.years(1)};
    final Period[] parentTickPeriods = {Period.minutes(1), Period.minutes(1),
            Period.hours(1), Period.hours(1), Period.days(1), Period.days(1),
            Period.months(1), /*Period.months(1),*/ Period.years(1)};
//    private final Period[] xAxisMajorTickPeriods = {Period.seconds(1), Period.minutes(1),
//            Period.hours(1), Period.days(1), Period.weeks(1), Period.months(1),
//            Period.years(1)};

    public GraphPanel() {
        //setFullWindow();
//        fullTRange = tWindow.clone();
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addMouseListener(this);
        markers.add(new GraphMarker(this, new DateTime(), "T0"));
    }

    public void clearAllGraphs() {
        subGraphs.clear();
    }

    public void addChannelGraph(List<Double> scores, List<Long> times) {
        addChannelGraph(String.valueOf(subGraphs.size()), scores, times);
    }

    public void addChannelGraph(String name, List<Double> scores, List<Long> times) {
        subGraphs.add(new SubGraph(this, name, subGraphs.size(), scores, times));
    }

    public void addRuleGraph(String ruleName, List<Double> scores, List<Long> times) {
        subGraphs.add(new SubGraph(this, subGraphs.size(), ruleName, scores, times));
    }

    public SubGraph getGraph(int index) throws IndexOutOfBoundsException {
        return subGraphs.get(index);
    }

    public SubGraph getGraph(String name) throws IndexOutOfBoundsException {
        for (SubGraph c : subGraphs) {
            if (c.getChannelName().equals(name)) return c;
        }
        throw new IndexOutOfBoundsException("Name " + name + " not found in channel list.");
    }

    public GraphMarker addMarker(DateTime time, String label) {
        GraphMarker marker = new GraphMarker(this, time, label);
        markers.add(marker);
        return marker;
    }

    public GraphMarker getMarker(int index) throws IndexOutOfBoundsException {
        return markers.get(index);
    }

    //delete specific marker.  Returns true if the list was modified.
    public boolean deleteMarker(GraphMarker marker) {
        return markers.remove(marker);
    }

    //delete at index
    public void deleteMarker(int index) {
        markers.remove(index);
    }

    public synchronized void addNeedsUpdatedDataListener(NeedsUpdatedDataListener listener) {
        if (listener == null) return;
        this.needsUpdatedDataListener = listener;
    }

    public void setWindow(DateTime start, DateTime stop) {
        tWindow[0] = start.getMillis();
        tWindow[1] = stop.getMillis();
    }

    public void setWindow(long start, long stop) {
        tWindow[0] = start;
        tWindow[1] = stop;
    }

    //View full range of data
    public void setFullWindow() {
        tWindow[0] = Long.MAX_VALUE;
        tWindow[1] = Long.MIN_VALUE;
        for (SubGraph c : subGraphs) {
            Long chanFullWindow[] = c.getFullWindow();
            if (chanFullWindow[0] < tWindow[0]) tWindow[0] = chanFullWindow[0];
            if (chanFullWindow[1] > tWindow[1]) tWindow[1] = chanFullWindow[1];
        }
    }

    public void setFullWindow(int chan) {
        try {
            tWindow = subGraphs.get(chan).getFullWindow();
        } catch (Exception ex) {
            //invalid channel?
            //failed to update
        }
    }

    boolean isTimeVisible(DateTime time) {
        return time.isAfter(getWindowStart()) && time.isBefore(getWindowStop());
    }

    boolean isIntervalVisible(Interval interval) {
        return !((interval.getStart().isBefore(getWindowStart()) &&
                  interval.getEnd().isBefore(getWindowStart()))
                ||
                 (interval.getStart().isAfter(getWindowStop()) &&
                  interval.getEnd().isAfter(getWindowStop())));
    }

    boolean isIntervalFullyVisible(Interval interval) {
        return (isTimeVisible(interval.getStart()) &&
                isTimeVisible(interval.getEnd()));
    }

    boolean isObjectVisible(DateTime startTime, int widthInPixels) {
        return isIntervalVisible(new Interval(startTime.getMillis(),
                startTime.getMillis() + (int)(widthInPixels/getXPixelsPerMs())));
    }

    //pixels per ms
    double getXPixelsPerMs() {
        return ((double) getWidth() - GraphPanel.leftPanelWidth) / (getWindowDurationMs());
    }

    public DateTime getWindowStart() {
        return new DateTime(tWindow[0]);
    }

    public DateTime getWindowStop() {
        return new DateTime(tWindow[1]);
    }

//    public void setWindowCenter(DateTime center) {
//        setWindowCenter(center.getMillis());
//    }

    public long getWindowCenterMs() {
        return (tWindow[0] + tWindow[1])/2;
    }

    public DateTime getWindowCenter() {
        return new DateTime(getWindowCenterMs());
    }

//    public void setWindowCenter(long center) {
//        long newStart = center - getWindowDurationMs()/2;
//        long newStop = center + getWindowDurationMs()/2;
//        if (newStart < fullTRange[0]) newStart = fullTRange[0];
//        if (newStop > fullTRange[0]) newStop = fullTRange[0];
//
//        setWindow(newStart, newStop);
//    }

    public void pinWindowToPoint(long pointMs, int pointX) {
        //get pointX in time space
        long pointXMs = getXPositionMs(pointX);

        long offset = pointMs - pointXMs;

        long newStart = tWindow[0] + offset;
        long newStop = tWindow[1] + offset;
//        if (newStart < fullTRange[0]) newStart = fullTRange[0];
//        if (newStop > fullTRange[0]) newStop = fullTRange[0];

        setWindow(newStart, newStop);
    }

    public Interval getWindowInterval() {
        return new Interval(getWindowStart(), getWindowStop());
    }

    public Duration getWindowDuration() {
        return new Duration(getWindowStart().getMillis(), getWindowStop().getMillis());
    }

    public long getWindowDurationMs() {
        //return getWindowDuration().getMillis();
        return tWindow[1] - tWindow[0];
    }

    public long getXPositionMs(int xpixel) {
        long tMin = tWindow[0];
        return (long) ((xpixel - leftPanelWidth) / getXPixelsPerMs()) + tMin;
    }

    public DateTime getXPosition(int xpixel) {
        return new DateTime(getXPositionMs(xpixel));
    }

    public int getXPositionPixel(long xTimeMs) {
        long tMin = tWindow[0];
        return (int) ((xTimeMs-tMin) * getXPixelsPerMs() + leftPanelWidth);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);

        xAxis.draw(g2);

        //draw subGraphs
        for (SubGraph c : subGraphs) {
            c.drawChannelData(g2);
        }

        //draw markers
        for (GraphMarker m : markers) {
            m.drawIfVisible(g2);
        }

        //draw the left panel
        for (SubGraph c : subGraphs) {
            c.drawLeftPanelChannelBox(g2);
        }
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void zoomIn(int xPosPixels) {
        zoomBy(xPosPixels, 0.6);
    }

    // zoomFactor is a number between -1 and 1.
    // Positive numbers zoom in, negative numbers zoom out.
    private void zoomBy(int xPosPixels, double zoomFactor) {
        long xPosMs = getXPositionMs(xPosPixels);
        long duration = getWindowDurationMs();
        long newDuration = (long)((double)duration/(1.0+zoomFactor));

        if (new Duration(newDuration).isShorterThan(Duration.millis(1)) ||
                new Duration(newDuration).isLongerThan(Duration.standardDays(365*2)))
            return;
        // Set new duration
        setWindow(getWindowStart().getMillis(),
                getWindowStart().getMillis() + newDuration);

        pinWindowToPoint(xPosMs, xPosPixels);

        if (needsUpdatedDataListener != null)
            needsUpdatedDataListener.needsUpdatedData(getWindowInterval());

        //updateWindowLabel();
    }

    private void zoomOut(int xPosPixels) {
        zoomBy(xPosPixels, -0.6);

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (mouseDragging && mouseScrollingXAxis) {
            pinWindowToPoint(mouseDragTimeMs, mouseEvent.getX());
            if (needsUpdatedDataListener != null)
                needsUpdatedDataListener.needsUpdatedData(getWindowInterval());
        } else {
            for (GraphMarker marker : markers) {
                marker.mouseMoved(mouseEvent);
            }
            this.repaint();
        }
        //needsUpdatedDataListener
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

        //Check for enter/exit sub objects..
        for (SubGraph c : subGraphs) {
            c.parentMouseMoved(mouseEvent);
        }

        for (GraphMarker marker : markers) {
            if (marker.mouseInRegion(mouseEvent.getX(), mouseEvent.getY())) {
                marker.mouseMoved(mouseEvent);
                mouseScrollingXAxis = false;
                break;
            }
        }

        this.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        //long xPosMs = graphPanel1.getXPositionMs(mouseWheelEvent.getX());
        if (mouseWheelEvent.getWheelRotation() < 0) {
            zoomIn(mouseWheelEvent.getX());
        } else if (mouseWheelEvent.getWheelRotation() > 0) {
            zoomOut(mouseWheelEvent.getX());
        }

        for (SubGraph c : subGraphs) {
            c.parentMouseMoved(mouseWheelEvent);
        }

        this.repaint();
    }

    public List<GraphMarker> getMarkersReversed() {
        List<GraphMarker> markersReversed = new ArrayList<>(markers);
        Collections.reverse(markersReversed);
        return markersReversed;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        boolean clickedMarker = false;

        for (GraphMarker marker : getMarkersReversed()) {
            if (marker.mouseInRegion(mouseEvent.getX(), mouseEvent.getY())) {
                marker.mouseClicked(mouseEvent);
                mouseScrollingXAxis = false;
                clickedMarker = true;
                break;
            }
        }

        if (!clickedMarker) {
            if (xAxis.mouseInRegion(mouseEvent.getX(), mouseEvent.getY())) {
                xAxis.mouseClicked(mouseEvent);
            }
        }

        this.repaint();

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        mouseDragging = true;
        mouseScrollingXAxis = true;
        mouseDragTimeMs = getXPositionMs(mouseEvent.getX());
        mouseDragStartPoint = mouseEvent.getPoint();

        for (GraphMarker marker : getMarkersReversed()) {
            if (marker.mouseInRegion(mouseEvent.getX(), mouseEvent.getY())) {
                marker.mousePressed(mouseEvent);
                mouseScrollingXAxis = false;
                break;
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mouseDragging = false;
        mouseScrollingXAxis = false;
        mouseDragTimeMs = null;
        mouseDragStartPoint = null;

        for (GraphMarker marker : getMarkersReversed()) {
            marker.mouseReleased(mouseEvent);
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    public void scrollTo(DateTime startTime, DateTime endTime) {
        setWindow(startTime, endTime);
        if (needsUpdatedDataListener != null)
            needsUpdatedDataListener.needsUpdatedData(getWindowInterval());
    }

    public void scrollTo(DateTime center, Period windowSize) {
        int windowHalfSeconds = (int)(windowSize.toDurationFrom(center).getMillis()/1000/2);
        scrollTo(center.minusSeconds(windowHalfSeconds), center.plusSeconds(windowHalfSeconds));
    }

    public void scrollTo(DateTime center, Duration windowSize) {
        int windowHalfSeconds = (int)(windowSize.getMillis()/1000/2);
        if (windowHalfSeconds <= 0) windowHalfSeconds = 1;
        scrollTo(center.minusSeconds(windowHalfSeconds), center.plusSeconds(windowHalfSeconds));
    }

}