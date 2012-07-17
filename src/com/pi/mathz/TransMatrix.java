package com.pi.mathz;

import com.pi.gl.graphics.objects.Vector3D;

public class TransMatrix extends Matrix {

	public TransMatrix() {
		super(4, 4);
		identity();
	}

	public TransMatrix(Matrix m) {
		super(4, 4);
		for (int i = 0; i < m.getRows(); i++)
			for (int q = 0; q < m.getCols(); q++)
				set(i, q, m.get(i, q));
	}

	public TransMatrix setRotation(float x, float y, float z,
			double radians) {
		double c = Math.cos(radians), s = Math.sin(radians);
		set(0, 0, c + (x * x * (1 - c)));
		set(0, 1, x * y * (1 - c) - z * s);
		set(0, 2, x * z * (1 - c) + y * s);

		set(1, 0, y * x * (1 - c) + z * s);
		set(1, 1, c + (y * y * (1 - c)));
		set(1, 2, y * z * (1 - c) - x * s);

		set(2, 0, z * x * (1 - c) - y * s);
		set(2, 1, z * y * (1 - c) + x * s);
		set(2, 2, c + (z * z * (1 - c)));
		return this;
	}

	public TransMatrix identity() {
		set(0, 0, 1);
		set(1, 0, 0);
		set(2, 0, 0);
		set(0, 1, 0);
		set(1, 1, 1);
		set(2, 1, 0);
		set(0, 2, 0);
		set(1, 2, 0);
		set(2, 2, 1);
		return this;
	}

	public TransMatrix setXRotation(double radians) {
		return setRotation(1, 0, 0, radians);
	}

	public TransMatrix setYRotation(double yaw) {
		return setRotation(0, 1, 0, yaw);
	}

	public TransMatrix setZRotation(double radians) {
		return setRotation(0, 0, 1, radians);
	}

	public Vector3D multiply(Vector3D v) {
		double resX =
				get(3, 0) + (v.x * get(0, 0))
						+ (v.y * get(0, 1)) + (v.z * get(0, 2));
		double resY =
				get(3, 1) + (v.x * get(1, 0))
						+ (v.y * get(1, 1)) + (v.z * get(1, 2));
		double resZ =
				get(3, 2) + (v.x * get(2, 0))
						+ (v.y * get(2, 1)) + (v.z * get(2, 2));
		return new Vector3D((float) resX, (float) resY,
				(float) resZ);
	}

	public TransMatrix setSystemTranslation(Vector3D origin,
			Vector3D xVec, Vector3D yVec, Vector3D zVec) {
		Vector3D nLoc = origin.reverse();
		set(0, 0, xVec.x);
		set(1, 0, xVec.y);
		set(2, 0, xVec.z);
		set(0, 1, yVec.x);
		set(1, 1, yVec.y);
		set(2, 1, yVec.z);
		set(0, 2, zVec.x);
		set(1, 2, zVec.y);
		set(2, 2, zVec.z);
		setTranslation(Vector3D.dotProduct(nLoc, xVec),
				Vector3D.dotProduct(nLoc, yVec),
				Vector3D.dotProduct(nLoc, zVec));
		return this;
	}

	public TransMatrix setTranslation(double x, double y,
			double z) {
		set(0, 3, x);
		set(1, 3, y);
		set(2, 3, y);
		return this;
	}

	public TransMatrix multiply(TransMatrix b) {
		Matrix m = Matrix.multiply(this, b);
		for (int i = 0; i < m.getCols(); i++) {
			System.arraycopy(m.data[i], 0, data[i], 0, 3);
		}
		return this;
	}
}
