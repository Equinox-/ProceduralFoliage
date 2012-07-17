package com.pi.math3d;
public class Point3D {
    private static final float variance = 0.0025f;
    public float x, y, z;

    public Point3D(float x, float y, float z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public void setLocation(float x, float y, float z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public Point3D translate(float x, float y, float z) {
	this.x += x;
	this.y += y;
	this.z += z;
	return this;
    }

    public Point3D subtract(Point3D p) {
	this.x -= p.x;
	this.y -= p.y;
	this.z -= p.z;
	return this;
    }

    public Point3D add(Point3D p) {
	this.x += p.x;
	this.y += p.y;
	this.z += p.z;
	return this;
    }

    public Point3D multiply(float scalar) {
	this.x *= scalar;
	this.y *= scalar;
	this.z *= scalar;
	return this;
    }

    public double dist(Point3D p) {
	float dX = p.x - x, dY = p.y - y, dZ = p.z - z;
	return Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }

    @Override
    public Point3D clone() {
	return new Point3D(x, y, z);
    }

    @Override
    public int hashCode() {
	return (int) x << 24 ^ (int) y << 12 ^ (int) z << 6;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Point3D) {
	    Point3D p = (Point3D) o;
	    float xD = p.x - x, yD = p.y - y, zD = p.z - z;
	    return xD < variance && xD > -variance && yD < variance
		    && yD > -variance && zD < variance && zD > -variance;
	}
	return false;
    }

    @Override
    public String toString() {
	return "(" + x + "," + y + "," + z + ")";
    }

    public Point3D translate(Point3D trans) {
	return translate(trans.x, trans.y, trans.z);
    }
}
