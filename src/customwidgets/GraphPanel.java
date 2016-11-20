package customwidgets;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Created by mcochrane on 30/10/16.
 */

public class GraphPanel extends JPanel {

    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(145, 145, 145);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private Color backgroundColor = new Color(35, 35, 35);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 6;
    private int numberYDivisions = 10;
    private List<Double> scores;
    private List<Long> times;
    private Long[] tWindow = new Long[2];
    private Long[] fullTRange = new Long[2];

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

    public GraphPanel(List<Double> scores, List<Long> times) {
        this.scores = scores;
        this.times = times;
        setFullWindow();
        fullTRange = tWindow.clone();
    }

    public void updateData(List<Double> scores, List<Long> times) {
//        this.scores.clear();
//        this.times.clear();
//
//        for (Double d : scores) this.scores.add(d);
//        for (Long l : times) this.times.add(l);

        this.scores = scores;
        this.times = times;
        invalidate();
        this.repaint();
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
        tWindow[0] = times.get(0);
        tWindow[1] = times.get(times.size()-1);
    }

    public DateTime getWindowStart() {
        return new DateTime(tWindow[0]);
    }

    public DateTime getWindowStop() {
        return new DateTime(tWindow[1]);
    }

    public void setWindowCenter(DateTime center) {
        setWindowCenter(center.getMillis());
    }

    public long getWindowCenterMs() {
        return (tWindow[0] + tWindow[1])/2;
    }

    public DateTime getWindowCenter() {
        return new DateTime(getWindowCenterMs());
    }

    public void setWindowCenter(long center) {
        long newStart = center - getWindowDurationMs()/2;
        long newStop = center + getWindowDurationMs()/2;
        if (newStart < fullTRange[0]) newStart = fullTRange[0];
        if (newStop > fullTRange[0]) newStop = fullTRange[0];

        setWindow(newStart, newStop);
    }

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

    public Duration getWindowDuration() {
        return new Duration(getWindowStart().getMillis(), getWindowStop().getMillis());
    }

    public long getWindowDurationMs() {
        //return getWindowDuration().getMillis();
        return tWindow[1] - tWindow[0];
    }

    public long getXPositionMs(int xpixel) {
        long xMin = tWindow[0];
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (getWindowDurationMs());
//        return (int) ((xTimeMs-xMin) * xScale + padding + labelPadding);
//        x1 = (int) ((xTimeMs-xMin) * xScale + padding + labelPadding);
//        x1 = ((xTimeMs-xMin) * xScale + padding + labelPadding);
//        (x1 - padding - labelPadding)/xScale = xTimeMs-xMin;
//        ((x1 - padding - labelPadding) / xScale) + xMin = xTimeMs;
        return (long) ((xpixel - padding - labelPadding) / xScale) + xMin;
    }

    public DateTime getXPosition(int xpixel) {
        return new DateTime(getXPositionMs(xpixel));
    }

    public int getXPositionPixel(long xTimeMs) {
        long xMin = tWindow[0];
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (getWindowDurationMs());
        return (int) ((xTimeMs-xMin) * xScale + padding + labelPadding);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long tRange = getWindowDurationMs();
        double xMin = tWindow[0];
        double xMax = tWindow[1];
        //double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (tRange);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            if (times.get(i) >= xMin && times.get(i) <= xMax) {
                //int x1 = (int) (i * xScale + padding + labelPadding);
                int x1 = (int) ((times.get(i) - xMin) * xScale + padding + labelPadding);
                int y1 = (int) ((getMaxScore() - scores.get(i)) * yScale + padding);
                graphPoints.add(new Point(x1, y1));
            } else if (times.get(i) > xMax) {
                int x1 = (int) ((xMax - xMin) * xScale + padding + labelPadding);
                int y1 = (int) ((getMaxScore() - scores.get(i-1)) * yScale + padding);
                graphPoints.add(new Point(x1, y1));
                break;
            }
        }

        // draw white background
        g2.setColor(backgroundColor);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
//        for (int i = 0; i < numberYDivisions + 1; i++) {
//            int x0 = padding + labelPadding;
//            int x1 = pointWidth + padding + labelPadding;
//            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
//            int y1 = y0;
//            if (scores.size() > 0) {
////                g2.setColor(gridColor);
////                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
//                g2.setColor(Color.BLACK);
//                String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
//                FontMetrics metrics = g2.getFontMetrics();
//                int labelWidth = metrics.stringWidth(yLabel);
//                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
//            }
//            g2.drawLine(x0, y0, x1, y1);
//        }

        //new x axis drawing...
        //Find best spacing - secs/hours/mins/days/weeks/months/years
        //want at most one tick per ??? pixels.
        int minPixelsPerTick = 50;
//        final Period[] possibleXAxisTickPeriods = {Period.seconds(1), Period.minutes(1),
//            Period.hours(1), Period.days(1), Period.months(1), Period.years(1)};

        Period xAxisTickPeriod = null;
        for (Period p : possibleXAxisTickPeriods) {
            if (periodToPixels(p) > minPixelsPerTick) {
                xAxisTickPeriod = p;
                break;
            }
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

        //draw


//        // draw x axis
//        //want N ticks, equally spaced.
//        int numTicks = 10;
//        long xSpacing = tRange/(numTicks-1); //get's floored because it's int division
//        for (int i = 0; i < numTicks; i++) {
//            //variables determining the drawing location on the screen
//            int x0 = i * (getWidth() - padding * 2 - labelPadding) / (numTicks - 1) + padding + labelPadding;
//            int x1 = x0;
//            int y0 = getHeight() - padding - labelPadding;
//            int y1 = y0 - pointWidth;
//
//            long x0Time = tWindow[0] + xSpacing * i;
//
//            //g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
//            g2.setColor(Color.BLACK);
//            String xLabel = new DateTime(x0Time).toString("HH:mm:ss");
//            FontMetrics metrics = g2.getFontMetrics();
//            int labelWidth = metrics.stringWidth(xLabel);
//            g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
//
//            g2.drawLine(x0, y0, x1, y1);
//        }


        // and for x axis
//        for (int i = 0; i < scores.size(); i++) {
//            if (scores.size() > 1) {
//                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
//                int x1 = x0;
//                int y0 = getHeight() - padding - labelPadding;
//                int y1 = y0 - pointWidth;
//                if ((i % ((int) ((scores.size() / 20.0)) + 1)) == 0) {
//                    g2.setColor(gridColor);
//                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
//                    g2.setColor(Color.BLACK);
//                    //String xLabel = i + "";
//                    String xLabel = new DateTime(times.get(i)).toString("HH:mm:ss");
//                    FontMetrics metrics = g2.getFontMetrics();
//                    int labelWidth = metrics.stringWidth(xLabel);
//                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
//                }
//                g2.drawLine(x0, y0, x1, y1);
//            }
//        }

        // create x and y axes
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            Point p1 = graphPoints.get(i);
            Point p2 = graphPoints.get(i + 1);
            //drawSegment(g2, p1, p2);
            drawSegmentPreStep(g2, p1, p2);
        }

//        g2.setStroke(oldStroke);
//        g2.setColor(pointColor);
//        //no dot for first or last points
//        for (int i = 1; i < graphPoints.size()-1; i++) {
//            int x = graphPoints.get(i).x - pointWidth / 2;
//            int y = graphPoints.get(i).y - pointWidth / 2;
//            int ovalW = pointWidth;
//            int ovalH = pointWidth;
//            g2.fillOval(x, y, ovalW, ovalH);
//        }
    }

    private String makeXAxisLabelText(DateTime tickTime, Period xAxisTickPeriod) throws Exception {
        if (xAxisTickPeriod.getYears() > 0) {
            return tickTime.toString("YYYY");
        } else if (xAxisTickPeriod.getMonths() > 0) {
            return tickTime.toString("MMM");
        } else if (xAxisTickPeriod.getWeeks() > 0) {
            return tickTime.toString("d");
        } else if (xAxisTickPeriod.getDays() > 0) {
//            return tickTime.toString("E");
            return tickTime.toString("d");
        } else if (xAxisTickPeriod.getHours() > 0) {
            return tickTime.toString("ha");
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
                    makeXAxisLabelText(currentXAxisTick, xAxisTickPeriod),
                    makeXAxisLabelText(currentXAxisTick, getParentTickPeriod(xAxisTickPeriod)));
        } else {
            drawSingleTierXAxisLabel(g2, currentXAxisTick.getMillis(),
                    makeXAxisLabelText(currentXAxisTick, xAxisTickPeriod));
        }
    }

    private void drawSingleTierXAxisLabel(Graphics2D g2, long tMs, String xLabel) {
        drawTwoTierXAxisLabel(g2, tMs, xLabel, "");
    }

//    private void drawTwoTierXAxisLabel(Graphics2D g2, long tMs, String xLabel, String tierTwoLabel) {
//        //variables determining the drawing location on the screen
//        int x0 = getXPositionPixel(tMs);
//        int x1 = x0;
//        int y0 = getHeight() - padding - labelPadding;
//        int y1 = y0 - pointWidth;
//
//        g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
//        g2.setColor(Color.BLACK);
//        //String xLabel = new DateTime(tMs).toString("HH:mm:ss");
//        FontMetrics metrics = g2.getFontMetrics();
//        int labelWidth = metrics.stringWidth(xLabel);
//        g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
//
//        g2.drawLine(x0, y0, x1, y1);
//
//        if (tierTwoLabel != null && !tierTwoLabel.isEmpty()) {
//            g2.drawString(tierTwoLabel, x0 - labelWidth / 2, y0 + (metrics.getHeight() + 3)*2);
//        }
//    }

    private void drawTwoTierXAxisLabel(Graphics2D g2, long tMs, String xLabel, String tierTwoLabel) {
        //variables determining the drawing location on the screen
        int x0 = getXPositionPixel(tMs);
        int x1 = x0;
        int y0 = getHeight() - padding - labelPadding;
        int y1 = y0 - pointWidth;

//        g2.setColor(gridColor);
//        g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
        g2.setColor(Color.BLACK);
        //String xLabel = new DateTime(tMs).toString("HH:mm:ss");
        FontMetrics metrics = g2.getFontMetrics();
        int labelWidth = metrics.stringWidth(xLabel);
        g2.drawString(xLabel, x0 - labelWidth / 2, y0 + (metrics.getHeight() + 3)*2);



        if (tierTwoLabel != null && !tierTwoLabel.isEmpty()) {
            int label2Width = metrics.stringWidth(tierTwoLabel);
            g2.drawString(tierTwoLabel, x0 - label2Width / 2, y0 + metrics.getHeight() + 3);
            g2.drawLine(x0, y0, x1, y1);
        } else {
            g2.drawLine(x0, y0 + metrics.getHeight() + 3, x1, y1);
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
        int graphWidthPixels = getWidth() - (2 * padding) - labelPadding;
        return (int) (graphWidthPixels/(getWindowDurationMs()/d.getMillis()));
    }

    private void drawSegment(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
    }


    private void drawSegmentPreStep(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(p1.x, p1.y, p2.x, p1.y);
        g2.drawLine(p2.x, p1.y, p2.x, p2.y);
    }

    private void drawSegmentPostStep(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(p1.x, p1.y, p1.x, p2.y);
        g2.drawLine(p1.x, p2.y, p2.x, p2.y);
    }

    //    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, heigth);
//    }
    private double getMinScore() {
        return 0.0 - 0.1;
//        double minScore = Double.MAX_VALUE;
//        for (Double score : scores) {
//            minScore = Math.min(minScore, score);
//        }
//        return minScore;
    }

    private double getMaxScore() {
        return 1.0 + 0.1;
//        double maxScore = Double.MIN_VALUE;
//        for (Double score : scores) {
//            maxScore = Math.max(maxScore, score);
//        }
//        return maxScore;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
        invalidate();
        this.repaint();
    }

    public List<Double> getScores() {
        return scores;
    }
}