package customwidgets;

import TimerDescriptionLanguage.SimulatedEvent;
import TimerDescriptionLanguage.SimulatedEvents;
import customwidgets.dialogs.NewEventDialog;
import customwidgets.listeners.GraphPanelXAxisPopupMenuListener;
import customwidgets.listeners.MouseClickedListener;
import customwidgets.menus.GraphPanelXAxisPopupMenu;
import jssc.SerialPortException;
import min.FrameReceiver;
import min.SerialHandler;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by mcochrane on 6/05/17.
 */
public class GraphPanelXAxis extends GraphPanelWidget {

    public GraphPanel parent;

    GraphPanelXAxis(GraphPanel parent) {
        this.parent = parent;
    }

    @Override
    public void draw(Graphics2D g2) {
        drawXaxisMinorBar(g2);
        drawXaxisMajorBar(g2);

        //new x axis drawing...
        //Find best spacing - secs/hours/mins/days/weeks/months/years
        //want at most one tick per ??? pixels.
        int minPixelsPerTick = 50;
//        final Period[] possibleXAxisTickPeriods = {Period.seconds(1), Period.minutes(1),
//            Period.hours(1), Period.days(1), Period.months(1), Period.years(1)};

        Period xAxisTickPeriod = null;
        for (Period p : parent.possibleXAxisTickPeriods) {
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
            currentXAxisTick = roundToNearestPeriod(parent.getWindowStart(), xAxisTickPeriod);

            while (currentXAxisTick.isBefore(parent.getWindowStop())) {
                currentXAxisTick = currentXAxisTick.plus(xAxisTickPeriod);
                if (currentXAxisTick.isBefore(parent.getWindowStart())) continue;
                drawXAxisLabel(g2, currentXAxisTick, xAxisTickPeriod);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
//        drawBoundingBox(g2);
    }

    private void drawXaxisMinorBar(Graphics2D g2) {
        Rectangle r = new Rectangle(GraphPanel.leftPanelWidth, GraphPanel.timeBarMajorAxisHeight,
                parent.getWidth()-GraphPanel.leftPanelWidth, GraphPanel.timeBarMinorAxisHeight);
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
        Rectangle r = new Rectangle(GraphPanel.leftPanelWidth, 0,
                parent.getWidth()-GraphPanel.leftPanelWidth, GraphPanel.timeBarMajorAxisHeight);
        Color gradientStart = new Color(75, 75, 75);
        Color gradientEnd = new Color(60, 60, 60);

        GradientPaint gp = new GradientPaint(new Point(0, r.y), gradientStart,
                new Point(0, (int)r.getMaxY()), gradientEnd);
        g2.setPaint(gp);
        g2.fill(r);
        g2.setColor(Color.BLACK);
        g2.draw(r);
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
            return tickTime.toString("m") + " min";
        } else if (xAxisTickPeriod.getSeconds() > 0) {
            return tickTime.toString("s") + " s";
        } else if (xAxisTickPeriod.getMillis() > 0) {
            return tickTime.toString("SSS" + " ms");
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
        for (int i = 0; i < parent.possibleXAxisTickPeriods.length - 1; i++) {
            if (p.equals(parent.possibleXAxisTickPeriods[i])) {
                return parent.parentTickPeriods[i];
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
        int x0 = parent.getXPositionPixel(tMs);
        int x1 = x0;
        int y0 = GraphPanel.timeBarHeight;
        int y1 = GraphPanel.timeBarHeight - 10;

//        g2.setColor(gridColor);
//        g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);

        FontMetrics metrics = g2.getFontMetrics();

        if (tierTwoLabel != null && !tierTwoLabel.isEmpty()) {
//            int label2Width = metrics.stringWidth(tierTwoLabel);
            g2.setColor(new Color(163, 163, 163));
            g2.drawString(tierTwoLabel, x0 + 5 , GraphPanel.timeBarMajorAxisHeight - 3);
            g2.drawLine(x0, y0, x1, GraphPanel.timeBarMajorAxisHeight+1);
            g2.drawLine(x0, GraphPanel.timeBarMajorAxisHeight, x1, GraphPanel.timeBarMajorAxisHeight/2);
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
        Duration d = p.toDurationFrom(parent.getWindowStart());
        int graphWidthPixels = parent.getWidth() - GraphPanel.leftPanelWidth;
        return (int) (graphWidthPixels/(parent.getWindowDurationMs()/d.getMillis()));
    }

    @Override
    protected Shape getRegion() {
        return new Rectangle(GraphPanel.leftPanelWidth, 0, parent.getWidth() - GraphPanel.leftPanelWidth, GraphPanel.timeBarHeight);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            GraphPanelXAxisPopupMenu popup = new GraphPanelXAxisPopupMenu(this, new GraphPanelXAxisPopupMenuListener() {
                @Override
                public void createEventAction(DateTime eventTime) {
                    NewEventDialog dev_sel = new NewEventDialog();
                    dev_sel.setVisible(true);
                    if (dev_sel.result.isEmpty()) return;

                    SimulatedEvents.getInstance().addEvent(new SimulatedEvent(dev_sel.result, eventTime, parent));
                }
            });
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            parent.repaint();
        }
    }

}
