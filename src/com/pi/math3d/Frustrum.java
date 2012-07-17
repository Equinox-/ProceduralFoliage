package com.pi.math3d;
import java.awt.geom.Point2D;

public class Frustrum {
    private int near, far, width, height;
    private final float verticalRadians, projDepth;

    public Frustrum(int near, int far, float verticalRadians, int width,
	    int height) {
	if (near == 0)
	    throw new IllegalArgumentException("Invalid Near Bound!");
	this.near = near;
	this.far = far;
	this.verticalRadians = verticalRadians;
	this.width = width;
	this.height = height;
	this.projDepth = (float) (1 / Math.tan(this.verticalRadians / 2));
    }

    public void setSize(int width, int height) {
	this.width = width;
	this.height = height;
    }

    public boolean isOnScreen(Point3D pt) {
	return toScreen(pt) != null;
    }

    public Point2D toScreen(Point3D point) {
	if (point.z >= near && point.z <= far) {
	    float _x = ((point.x / point.z * this.projDepth) + 1) / 2f;
	    float _y = ((point.y / point.z * this.projDepth) + 1) / 2f;
	    return new Point2D.Float(_x * width, height - (_y * height));
	}
	return null;
    }

    public float getScreenDepth() {
	return this.projDepth;
    }

    public int getHeight() {
	return height;
    }

    public int getWidth() {
	return width;
    }
}
