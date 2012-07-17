package com.pi.math3d;
public class TransformationMatrix implements Transformation{
    public float xX = 1, xY = 0, xZ = 0, yX = 0, yY = 1, yZ = 0, zX = 0,
	    zY = 0, zZ = 1, wX = 0, wY = 0, wZ;

    public TransformationMatrix setScale(float x, float y, float z) {
	xX = x;
	xY = 0;
	xZ = 0;
	yY = y;
	yX = 0;
	yZ = 0;
	zZ = z;
	zX = 0;
	zY = 0;
	return this;
    }

    public TransformationMatrix setRotation(float x, float y, float z,
	    double radians) {
	double c = Math.cos(radians), s = Math.sin(radians);
	xX = (float) (c + (1 - c) * x * x);
	xY = (float) ((1 - c) * x * y - s * z);
	xZ = (float) ((1 - c) * x * z - s * y);
	yX = (float) ((1 - c) * x * y - s * z);
	yY = (float) (c + (1 - c) * y * y);
	yZ = (float) ((1 - c) * y * z + s * x);
	zX = (float) ((1 - c) * x * z + s * y);
	zY = (float) ((1 - c) * y * z - s * x);
	zZ = (float) (c + (1 - c) * z * z);
	return this;
    }

    public TransformationMatrix setXRotation(double radians) {
	return setRotation(1, 0, 0, radians);
    }

    public TransformationMatrix setYRotation(double yaw) {
	return setRotation(0, 1, 0, yaw);
    }

    public TransformationMatrix setZRotation(double radians) {
	return setRotation(0, 0, 1, radians);
    }

    public TransformationMatrix setSystemTranslation(Point3D loc, Point3D xVec,
	    Point3D yVec, Point3D zVec) {
	Point3D nLoc = VectorUtil.negative(loc);
	xX = xVec.x;
	xY = xVec.y;
	xZ = xVec.z;
	yX = yVec.x;
	yY = yVec.y;
	yZ = yVec.z;
	zX = zVec.x;
	zY = zVec.y;
	zZ = zVec.z;
	wX = VectorUtil.dotProduct(nLoc, xVec);
	wY = VectorUtil.dotProduct(nLoc, yVec);
	wZ = VectorUtil.dotProduct(nLoc, zVec);
	return this;
    }

    public TransformationMatrix setTranslation(float x, float y, float z) {
	wX = x;
	wY = y;
	wZ = z;
	return this;
    }

    public Point3D translate(Point3D p) {
	float x = wX + (xX * p.x) + (yX * p.y) + (zX * p.z);
	float y = wY + (xY * p.x) + (yY * p.y) + (zY * p.z);
	float z = wZ + (xZ * p.x) + (yZ * p.y) + (zZ * p.z);
	p.setLocation(x, y, z);
	return p;
    }

    @Override
    public String toString() {
	return xX + "," + xY + "," + xZ + "\n" + yX + "," + yY + "," + yZ
		+ "\n" + zX + "," + zY + "," + zZ + "\n" + wX + "," + wY + ","
		+ wZ;
    }
}
