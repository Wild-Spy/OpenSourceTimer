package customwidgets;

import customwidgets.listeners.MouseClickedListener;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by mcochrane on 5/05/17.
 */
public class GraphPanelWidget {
    protected boolean mouseDown = false;
    private MouseClickedListener mouseClickedListener = null;

    public void setMouseClickedListener(MouseClickedListener mouseClickedListener) {
        this.mouseClickedListener = mouseClickedListener;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == 1)
            mouseDown = true;
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        //if (!mouseDown) return;
        mouseDown = false;
    }

    public void draw(Graphics2D g2) {}

    public void mouseMoved(MouseEvent mouseEvent) {

    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (this.mouseClickedListener != null)
            this.mouseClickedListener.mouseClicked(mouseEvent);
    }

    protected Shape getRegion() { return null; }

    protected void drawBoundingBox(Graphics2D g2) {
        Shape z = getRegion();
        if (z == null) return;

        g2.setColor(new Color(0, 0, 255));
        g2.draw(z);
    }

    public boolean mouseInRegion(int x, int y) {
        Shape region = getRegion();
        if (region == null) return false;

        return region.contains(x, y);
    }

}
