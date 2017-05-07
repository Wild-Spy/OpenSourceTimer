package customwidgets;

import customwidgets.listeners.GraphMarkerChangedListener;
import customwidgets.listeners.GraphMarkerPopupMenuListener;
import customwidgets.menus.GraphMarkerPopupMenu;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Created by mcochrane on 24/11/16.
 */
public class GraphMarker extends GraphPanelWidget {
    private GraphPanel parent;
    private DateTime time;
    private String label;
    private boolean selected;
    private GraphMarkerChangedListener onChangeCallback = null;

    private static int height = GraphPanel.timeBarMinorAxisHeight*3/4;
    private Integer width = null;
    private Integer labelWidth = null;
    private JPopupMenu popupMenu = null;
    private boolean dragable = false;

    GraphMarker(GraphPanel parent, DateTime time, String label) {
        this.time = time;
        setLabel(label);
        this.parent = parent;
    }

    public void setOnChangeCallback(GraphMarkerChangedListener callback) {
        onChangeCallback = callback;
    }

    public void setSelected(boolean selected) {
        if (selected == this.selected) return;
        this.selected = selected;
        parent.repaint();
    }

    public void setIsDragable(boolean dragable) {
        this.dragable = dragable;
    }

    public void setPopupMenu(JPopupMenu menu) {
        this.popupMenu = menu;
    }

    public DateTime getTime() {
        return this.time;
    }

    public void setTime(DateTime time) {
        this.time = time;
        if (onChangeCallback != null)
            onChangeCallback.onChange(this.time);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        this.width = null;
        this.labelWidth = null;
    }

    private Integer getX0() {
        if (time.isBefore(parent.getWindowStart()) || time.isAfter(parent.getWindowStop())) return null;
        return parent.getXPositionPixel(time.getMillis());
    }

    public void drawIfVisible(Graphics2D g2) {
        Integer x0 = getX0();
        if (x0 == null) return;

        int y0 = GraphPanel.timeBarHeight;
        int y1 = GraphPanel.timeBarHeight + 200;
        g2.setColor(GraphPanel.lineColor);
        g2.drawLine(x0, y0, x0, y1);
        drawTop(g2, x0);
    }

    /**
     * Update the width of the marker.
     * g2 passed should have g2.setFont(theFontThatWillBeUsedWhenDrawingTheMarker);
     * @param g2
     */
    private void updateWidth(Graphics2D g2) {
        FontMetrics metrics = g2.getFontMetrics();
        labelWidth = metrics.stringWidth(label);

        width = (int)(labelWidth * 1.3);//20;
        if (width < 20) width = 20;
    }

    private void drawTop(Graphics2D g2, int x0) {
        //Setup font
        Font oldFont = g2.getFont();
        Font newFont = new Font("Arial", Font.PLAIN, 10);
        g2.setFont(newFont);

        if (this.width == null) updateWidth(g2);

        int xPoly[] = {-width/2, width/2, width/2, 0, -width/2, -width/2};
        int yPoly[] = {-height, -height, -height/2, 0, -height/2, -height};

        //Draw polygon
        if (!selected)
            g2.setColor(GraphPanel.lineColor);
        else
            g2.setColor(GraphPanel.backgroundColor);

        Polygon marker = new Polygon(xPoly, yPoly, xPoly.length);
        marker.translate(x0, GraphPanel.timeBarHeight);
        g2.fill(marker);
        g2.setColor(new Color(40, 40, 40));
        g2.drawPolygon(marker);

        if (label == null || label.trim().equals("")) return;

        if (!selected)
            g2.setColor(new Color(40, 40, 40));
        else
            g2.setColor(new Color(255-40, 255-40, 255-40));
        g2.drawString(label, x0 - labelWidth/2 ,
                GraphPanel.timeBarHeight - height/2);//- height + (height/2-metrics.getHeight())/2);

//        drawBoundingBox(g2);

        g2.setFont(oldFont);
    }

    @Override
    protected Shape getRegion() {
        Integer x0 = getX0();
        if (x0 == null) return null;

        Rectangle rect = new Rectangle(-width/2, -height, width, height);
        rect.translate(x0, GraphPanel.timeBarHeight);

        return rect;
    }

    public boolean isVisible() {
        return !(getX0() == null);
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        super.mouseMoved(mouseEvent);

        if (mouseDown && dragable) {
            setTime(parent.getXPosition(mouseEvent.getX()));

            parent.updateData();
            parent.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);

        parent.updateData();
        parent.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        super.mouseClicked(mouseEvent);

        if (mouseEvent.getButton() == 3 && this.popupMenu != null) {
            this.popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            parent.repaint();
        }
    }

}
