package customwidgets;

import org.joda.time.DateTime;

import java.awt.*;

/**
 * Created by mcochrane on 24/11/16.
 */
public class GraphMarker {
    private GraphPanel parent;
    private DateTime time;
    private String label;

    GraphMarker(GraphPanel parent, DateTime time, String label) {
        this.time = time;
        this.label = label;
        this.parent = parent;
    }

    public DateTime getTime() {
        return this.time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void drawIfVisible(Graphics2D g2) {
        if (time.isBefore(parent.getWindowStart()) || time.isAfter(parent.getWindowStop())) return;
        int x0 = parent.getXPositionPixel(time.getMillis());
        int x1 = x0;
        int y0 = GraphPanel.timeBarHeight;
        int y1 = GraphPanel.timeBarHeight + 200;
        g2.setColor(GraphPanel.lineColor);
        g2.drawLine(x0, y0, x1, y1);
        drawTop(g2, x0);
    }

    private void drawTop(Graphics2D g2, int x0) {
        //Setup font
        Font oldFont = g2.getFont();
        Font newFont = new Font("Arial", Font.PLAIN, 10);
        g2.setFont(newFont);
        FontMetrics metrics = g2.getFontMetrics();
        int labelWidth = metrics.stringWidth(label);

        int width = (int)(labelWidth * 1.3);//20;
        if (width < 20) width = 20;
        int height = GraphPanel.timeBarMinorAxisHeight*3/4;
        int xPoly[] = {-width/2, width/2, width/2, 0, -width/2, -width/2};
        int yPoly[] = {-height, -height, -height/2, 0, -height/2, -height};

        //Draw polygon
        g2.setColor(GraphPanel.lineColor);
        Polygon marker = new Polygon(xPoly, yPoly, xPoly.length);
        marker.translate(x0, GraphPanel.timeBarHeight);
        g2.fill(marker);

        if (label == null || label.trim().equals("")) return;

        g2.setColor(new Color(40, 40, 40));
        g2.drawString(label, x0 - labelWidth/2 - 1 ,
                GraphPanel.timeBarHeight - height/2);//- height + (height/2-metrics.getHeight())/2);

        g2.setFont(oldFont);
    }

}
