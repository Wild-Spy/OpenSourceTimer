package customwidgets;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Second;

import java.awt.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mcochrane on 24/11/16.
 */
public class GraphIntervalMarker {
    private SubGraph parentChannel;
    private Interval interval;
    private boolean visible = true;

    private enum LineEndType {
        LEFT, RIGHT;
    }

    GraphIntervalMarker(SubGraph parentChannel, Interval interval) {
        this.parentChannel = parentChannel;
        this.interval = interval;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public DateTime getStart() {
        return this.interval.getStart();
    }

    public DateTime getEnd() {
        return this.interval.getEnd();
    }

    public long getStartMillis() {
        return this.interval.getStartMillis();
    }

    public long getEndMillis() {
        return this.interval.getEndMillis();
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public void setStart(DateTime startTime) {
        this.interval = new Interval(startTime, this.interval.getEnd());
    }

    public void setEnd(DateTime endTime) {
        this.interval = new Interval(this.interval.getStart(), endTime);
    }

    public void drawIfVisible(Graphics2D g2) {
        if (!this.visible) return;
        if (this.interval == null) return;
        if (!parentChannel.getParentGraph().isIntervalVisible(this.interval)) return;
        draw(g2);
    }

    public void drawIfFullyVisible(Graphics2D g2) {
        if (!this.visible) return;
        if (this.interval == null) return;
        if (!parentChannel.getParentGraph().isIntervalFullyVisible(this.interval)) return;
        draw(g2);
    }

    public void draw(Graphics2D g2) {
        if (this.interval == null) return;
        int xLeft = parentChannel.getParentGraph().getXPositionPixel(getStartMillis());
        int xRight = parentChannel.getParentGraph().getXPositionPixel(getEndMillis());

        if (xRight-xLeft < 10) return;

        drawLeftEnd(g2, xLeft);
        drawRightEnd(g2, xRight);
        drawLine(g2, xLeft, xRight);
        drawMeasurement(g2, xRight + 3);
    }

    private void drawLeftEnd(Graphics2D g2, int x) {
        drawLineEnd(g2, x, LineEndType.LEFT);
    }

    private void drawRightEnd(Graphics2D g2, int x) {
        drawLineEnd(g2, x, LineEndType.RIGHT);
    }

    private void drawLineEnd(Graphics2D g2, int x, LineEndType type) {
        int y0 = getYOffset();
        int y1 = getYOffset() + getHeight();

        g2.drawLine(x, y0, x, y1);

        //Draw arrow
        int yMiddle = getYOffset() + getHeight()/2;
        int xArrowBack = x + 5 * (type==LineEndType.LEFT?1:-1);
        g2.drawLine(x, yMiddle, xArrowBack, yMiddle+3);
        g2.drawLine(x, yMiddle, xArrowBack, yMiddle-3);
    }

    private void drawLine(Graphics2D g2, int xLeft, int xRight) {
        int y = getYOffset() + getHeight()/2;

        g2.drawLine(xLeft, y, xRight, y);
    }

    public static String humanReadableFormat(Interval interval) {
        PrettyTime p = new PrettyTime(new Date(interval.getEndMillis()));
        p.removeUnit(JustNow.class); // This stops it from printing 'moments ago for durations < 5 minutes'
        String retStr = p.format(new Date(interval.getStartMillis()));
        return retStr.substring(0, retStr.length()-4);
    }

    private void drawMeasurement(Graphics2D g2, int x) {
        String label = humanReadableFormat(this.interval);

        //Setup font
        Font oldFont = g2.getFont();
        Font newFont = new Font("Arial", Font.PLAIN, 10);
        g2.setFont(newFont);
        FontMetrics metrics = g2.getFontMetrics();
        int labelWidth = metrics.stringWidth(label);

//        int width = (int)(labelWidth * 1.3);//20;
//        if (width < 20) width = 20;
        int height = getHeight()*3/4;
//        int xPoly[] = {-width/2, width/2, width/2, 0, -width/2, -width/2};
//        int yPoly[] = {-height, -height, -height/2, 0, -height/2, -height};
//
//        //Draw polygon
//        g2.setColor(GraphPanel.lineColor);
//        Polygon marker = new Polygon(xPoly, yPoly, xPoly.length);
//        marker.translate(x0, GraphPanel.timeBarHeight);
//        g2.fill(marker);

        if (label == null || label.trim().equals("")) return;

        g2.setColor(new Color(145, 145, 145));
        g2.drawString(label, x,
                getYOffset() + height + 2);

        g2.setFont(oldFont);
    }

    private int getYOffset() {
         return GraphPanel.timeBarHeight
                 + parentChannel.getPlotIndex()*GraphPanel.channelHeight;
    }

    private int getHeight() {
        return GraphPanel.channelMeasurementBarHeight;
    }

}
