package customwidgets;

/**
 * Created by mcochrane on 28/11/16.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.Transient;
import java.io.Serializable;

/**
 * A 2d point where each coordinate is a double.
 */
public class DoublePoint extends Point2D implements Serializable {
    public double x;
    public double y;
    private static final long serialVersionUID = -5276940640259749850L;

    public DoublePoint() {
        this(0, 0);
    }

    public DoublePoint(java.awt.Point var1) {
        this(var1.x, var1.y);
    }

    public DoublePoint(DoublePoint var1) {
        this(var1.x, var1.y);
    }

    public DoublePoint(double var1, double var2) {
        this.x = var1;
        this.y = var2;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    @Transient
    public DoublePoint getLocation() {
        return new DoublePoint(this.x, this.y);
    }

    public void setLocation(java.awt.Point var1) {
        this.setLocation(var1.x, var1.y);
    }

    public void setLocation(DoublePoint var1) {
        this.setLocation(var1.x, var1.y);
    }

    public void setLocation(double var1, double var2) {
        this.move(var1, var2);
    }

    public void setLocation(int var1, int var3) {
        this.x = (int)Math.floor(var1 + 0.5D);
        this.y = (int)Math.floor(var3 + 0.5D);
    }

    public void move(double var1, double var2) {
        this.x = var1;
        this.y = var2;
    }

    public void translate(double var1, double var2) {
        this.x += var1;
        this.y += var2;
    }

    public boolean equals(Object var1) {
        if(!(var1 instanceof DoublePoint)) {
            return super.equals(var1);
        } else {
            DoublePoint var2 = (DoublePoint)var1;
            return this.x == var2.x && this.y == var2.y;
        }
    }

    public String toString() {
        return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]";
    }

    public Point toPoint() {
        return new Point((int)Math.floor(this.x + 0.5D), (int)Math.floor(this.y + 0.5D));
    }
}
