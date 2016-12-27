package customwidgets;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
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
    static final Color lineColor = new Color(145, 145, 145);
    static final Color pointColor = new Color(100, 100, 100, 180);
    static final Color gridColor = new Color(200, 200, 200, 200);
    static final Color backgroundColor = new Color(40, 40, 40);
    static final Color graphBackgroundColor = new Color(35, 35, 35);
    static final Stroke GRAPH_STROKE = new BasicStroke(1f);
    private int pointWidth = 6;
    private int numberYDivisions = 10;

    private List<SubGraph> subGraphs = new ArrayList<>();
    private List<GraphMarker> markers = new ArrayList<>();
    Long[] tWindow = new Long[2];
    private NeedsUpdatedDataListener needsUpdatedDataListener = null;
    private boolean mouseDragging = false;
    private long mouseDragTimeMs;

//    Long[] fullTRange = new Long[2];

    private final Period[] possibleXAxisTickPeriods = {Period.seconds(1), Period.seconds(15),
            Period.minutes(1), Period.minutes(15), Period.hours(1), Period.hours(6),
            Period.days(1), /*Period.days(5),*/ Period.months(1), Period.years(1)};
//            Period.days(1), Period.weeks(1), Period.months(1), Period.years(1)};
    private final Period[] parentTickPeriods = {Period.minutes(1), Period.minutes(1),
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

    public void addMarker(DateTime time, String label) {
        markers.add(new GraphMarker(this, time, label));
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

        drawXAxis(g2);

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

    private void drawXAxis(Graphics2D g2) {
        drawXaxisMinorBar(g2);
        drawXaxisMajorBar(g2);

        //new x axis drawing...
        //Find best spacing - secs/hours/mins/days/weeks/months/years
        //want at most one tick per ??? pixels.
        int minPixelsPerTick = 50;
//        final Period[] possibleXAxisTickPeriods = {Period.seconds(1), Period.minutes(1),
//            Period.hours(1), Period.days(1), Period.months(1), Period.years(1)};

        Period xAxisTickPeriod = null;
        for (Period p : possibleXAxisTickPeriods) {
            try {
                if (periodToPixels(p) > minPixelsPerTick) {
                    xAxisTickPeriod = p;
                    break;
                }
            } catch (Exception ex) {}
        }
        if (xAxisTickPeriod == null) xAxisTickPeriod = Period.years(1);

        DateTime currentXAxisTick;
        try {
            currentXAxisTick = roundToNearestPeriod(getWindowStart(), xAxisTickPeriod);

            while (currentXAxisTick.isBefore(getWindowStop())) {
                currentXAxisTick = currentXAxisTick.plus(xAxisTickPeriod);
                if (currentXAxisTick.isBefore(getWindowStart())) continue;
                drawXAxisLabel(g2, currentXAxisTick, xAxisTickPeriod);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private void drawXaxisMinorBar(Graphics2D g2) {
        Rectangle r = new Rectangle(leftPanelWidth, timeBarMajorAxisHeight,
                getWidth()-leftPanelWidth, timeBarMinorAxisHeight);
        Color gradientStart = new Color(75, 75, 75);
        Color gradientEnd = new Color(60, 60, 60);

        GradientPaint gp = new GradientPaint(new Point(0, r.y), gradientStart,
                new Point(0, (int)r.getMaxY()), gradientEnd);
        g2.setPaint(gp);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);
    }

    private void drawXaxisMajorBar(Graphics2D g2) {
        Rectangle r = new Rectangle(leftPanelWidth, 0,
                getWidth()-leftPanelWidth, timeBarMajorAxisHeight);
        Color gradientStart = new Color(75, 75, 75);
        Color gradientEnd = new Color(60, 60, 60);

        GradientPaint gp = new GradientPaint(new Point(0, r.y), gradientStart,
                new Point(0, (int)r.getMaxY()), gradientEnd);
        g2.setPaint(gp);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);
    }

    //pixels per ms
    double getXPixelsPerMs() {
        return ((double) getWidth() - leftPanelWidth) / (getWindowDurationMs());
    }

    private String makeMinorXAxisLabelText(DateTime tickTime, Period xAxisTickPeriod) throws Exception {
        if (xAxisTickPeriod.getYears() > 0) {
            return tickTime.toString("YYYY");
        } else if (xAxisTickPeriod.getMonths() > 0) {
            return tickTime.toString("MMM");
        } else if (xAxisTickPeriod.getWeeks() > 0) {
            return tickTime.toString("d");
        } else if (xAxisTickPeriod.getDays() > 0) {
//            return tickTime.toString("E");
            return tickTime.toString("E d");
        } else if (xAxisTickPeriod.getHours() > 0) {
            return tickTime.toString("h a");
        } else if (xAxisTickPeriod.getMinutes() > 0) {
            return tickTime.toString("mm");
        } else if (xAxisTickPeriod.getSeconds() > 0) {
            return tickTime.toString("ss");
        } else if (xAxisTickPeriod.getMillis() > 0) {
            return tickTime.toString("SSS");
        } else {
            throw new Exception("invalid period type");
        }
    }

    private String makeMajorXAxisLabelText(DateTime tickTime, Period xAxisTickPeriod) throws Exception {
        if (xAxisTickPeriod.getYears() > 0) {
            return tickTime.toString("YYYY");
        } else if (xAxisTickPeriod.getMonths() > 0) {
            return tickTime.toString("MMM YYYY");
        } else if (xAxisTickPeriod.getWeeks() > 0) {
            return tickTime.toString("d MMM");
        } else if (xAxisTickPeriod.getDays() > 0) {
//            return tickTime.toString("E");
            return tickTime.toString("d MMM");
        } else if (xAxisTickPeriod.getHours() > 0) {
            return tickTime.toString("h a 'on' MMM d");
        } else if (xAxisTickPeriod.getMinutes() > 0) {
            return tickTime.toString("h:mm a");
        } else if (xAxisTickPeriod.getSeconds() > 0) {
            return tickTime.toString("h:mm:ss a");
        } else if (xAxisTickPeriod.getMillis() > 0) {
            return tickTime.toString("SSS");
        } else {
            throw new Exception("invalid period type");
        }
    }

    private boolean isMajorXTick(DateTime tickTime, Period xAxisTickPeriod) throws Exception {
        Period majorTickPeriod = getParentTickPeriod(xAxisTickPeriod);
        if (majorTickPeriod == null) return false;
        if (roundToNearestPeriod(tickTime, majorTickPeriod).equals(tickTime)) {
            return true;
        }
        return false;
    }

    private Period getParentTickPeriod(Period p) {
        //ie. If we pass in hours, then we return days.
        //    If we pass in months, we get years. etc.

        //The last value of xAxisMajorTickPeriods is 'year' which doesn't have a parent.. Could do decade?
        for (int i = 0; i < possibleXAxisTickPeriods.length - 1; i++) {
            if (p.equals(possibleXAxisTickPeriods[i])) {
                return parentTickPeriods[i];
            }
        }
        //Otherwise there is no parent
        return null;
    }

    private void drawXAxisLabel(Graphics2D g2, DateTime currentXAxisTick, Period xAxisTickPeriod) throws Exception {
        if (isMajorXTick(currentXAxisTick, xAxisTickPeriod)) {
            drawTwoTierXAxisLabel(g2, currentXAxisTick.getMillis(),
                    makeMinorXAxisLabelText(currentXAxisTick, xAxisTickPeriod),
                    makeMajorXAxisLabelText(currentXAxisTick, getParentTickPeriod(xAxisTickPeriod)));
        } else {
            drawSingleTierXAxisLabel(g2, currentXAxisTick.getMillis(),
                    makeMinorXAxisLabelText(currentXAxisTick, xAxisTickPeriod));
        }
    }

    private void drawSingleTierXAxisLabel(Graphics2D g2, long tMs, String xLabel) {
        drawTwoTierXAxisLabel(g2, tMs, xLabel, "");
    }

    private void drawTwoTierXAxisLabel(Graphics2D g2, long tMs, String xLabel, String tierTwoLabel) {
        //variables determining the drawing location on the screen
        int x0 = getXPositionPixel(tMs);
        int x1 = x0;
        int y0 = timeBarHeight;
        int y1 = timeBarHeight - 10;

//        g2.setColor(gridColor);
//        g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);

        FontMetrics metrics = g2.getFontMetrics();

        if (tierTwoLabel != null && !tierTwoLabel.isEmpty()) {
//            int label2Width = metrics.stringWidth(tierTwoLabel);
            g2.setColor(new Color(163, 163, 163));
            g2.drawString(tierTwoLabel, x0 + 5 , timeBarMajorAxisHeight - 3);
            g2.drawLine(x0, y0, x1, timeBarMajorAxisHeight+1);
            g2.drawLine(x0, timeBarMajorAxisHeight, x1, timeBarMajorAxisHeight/2);
        } else {
//            int labelWidth = metrics.stringWidth(xLabel);
            g2.setColor(new Color(145, 145, 145));
            g2.drawString(xLabel, x0 - 3, y1 - 3);
            g2.drawLine(x0, y0, x1, y1);
        }
    }

    private DateTime roundToNearestPeriod(DateTime t, Period p) throws Exception {
        if (p.equals(Period.seconds(1))) {
            return t.minusMillis(t.getMillisOfSecond());

        } else if (p.equals(Period.seconds(15))) {
                return t.minusMillis(t.getMillisOfSecond())
                        .minusSeconds(t.getSecondOfMinute() % 15);

        } else if (p.equals(Period.minutes(1))) {
            return t.minusMillis(t.getMillisOfSecond())
                    .minusSeconds(t.getSecondOfMinute());

        } else if (p.equals(Period.minutes(15))) {
            return t.minusMillis(t.getMillisOfSecond())
                    .minusSeconds(t.getSecondOfMinute())
                    .minusMinutes(t.getMinuteOfHour() % 15);

        } else if (p.equals(Period.hours(1))) {
            return t.minusMillis(t.getMillisOfSecond())
                    .minusSeconds(t.getSecondOfMinute())
                    .minusMinutes(t.getMinuteOfHour());

        } else if (p.equals(Period.hours(6))) {
            return t.minusMillis(t.getMillisOfSecond())
                    .minusSeconds(t.getSecondOfMinute())
                    .minusMinutes(t.getMinuteOfHour())
                    .minusHours(t.getHourOfDay() % 6);

        } else if (p.equals(Period.days(1))) {
            return t.minusMillis(t.getMillisOfDay());

//        } else if (p.equals(Period.weeks(1))) {
//            return t.minusMillis(t.getMillisOfDay())
//                    .minusDays(t.getDayOfWeek());

        } else if (p.equals(Period.months(1))) {
            return t.minusMillis(t.getMillisOfDay())
                    .minusDays(t.getDayOfMonth()-1);

        } else if (p.equals(Period.years(1))) {
            return t.minusMillis(t.getMillisOfDay())
                    .minusDays(t.getDayOfYear()-1);

        } else {
            throw new Exception("invalid period type");
        }
    }

    private int periodToPixels(Period p) {
        Duration d = p.toDurationFrom(getWindowStart());
        int graphWidthPixels = getWidth() - leftPanelWidth;
        return (int) (graphWidthPixels/(getWindowDurationMs()/d.getMillis()));
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
        if (mouseDragging) {
            pinWindowToPoint(mouseDragTimeMs, mouseEvent.getX());
            if (needsUpdatedDataListener != null)
                needsUpdatedDataListener.needsUpdatedData(getWindowInterval());
        }
        //needsUpdatedDataListener
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        for (SubGraph c : subGraphs) {
            c.mouseMoved(mouseEvent);
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
            c.mouseMoved(mouseWheelEvent);
        }

        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        boolean addEventMode = true;
        String eventName = "event1";

        if (addEventMode) {

        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        mouseDragging = true;
        mouseDragTimeMs = getXPositionMs(mouseEvent.getX());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mouseDragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}