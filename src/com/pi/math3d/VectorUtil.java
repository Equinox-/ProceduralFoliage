package com.pi.math3d;
public class VectorUtil {
    public static Point3D normalize(Point3D p) {
	double dist = p.dist(new Point3D(0, 0, 0));
	if (dist != 0) {
	    p.x /= dist;
	    p.y /= dist;
	    p.z /= dist;
	}
	return p;
    }

    public static float dotProduct(Point3D u, Point3D v) {
	return (u.x * v.x) + (u.y * v.y) + (u.z * v.z);
    }

    public static Point3D crossProduct(Point3D u, Point3D v) {
	return new Point3D((u.y * v.z) - (u.z * v.y),
		(u.z * v.x) - (u.x * v.z), (u.x * v.y) - (u.y * v.x));
    }

    public static Point3D negative(Point3D p) {
	return new Point3D(-p.x, -p.y, -p.z);
    }
}
