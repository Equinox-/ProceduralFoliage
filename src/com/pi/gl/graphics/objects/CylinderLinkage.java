package com.pi.gl.graphics.objects;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.jogamp.common.nio.Buffers;

public class CylinderLinkage implements Mesh {
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer normalBuffer;

	private IntBuffer indexBuffer;

	private Vector3D[] vectors;
	private float[] radi;
	private float sliceSize;
	private Color[] colors;
	private int slices;

	public CylinderLinkage(Vector3D[] vectors, float[] radi,
			Color[] colors, int slices) {
		this.vectors = vectors;
		this.radi = radi;
		this.colors = colors;
		this.slices = slices;
		this.sliceSize =
				(float) ((Math.PI * 2f) / ((float) slices));

		createBuffers();
	}

	private void allocBuffers() {
		// Count the vertices and indices
		int verts = slices * vectors.length;
		vertexBuffer = Buffers.newDirectFloatBuffer(verts * 3);
		normalBuffer = Buffers.newDirectFloatBuffer(verts * 3);
		colorBuffer = Buffers.newDirectFloatBuffer(verts * 3);
		indexBuffer =
				Buffers.newDirectIntBuffer(slices * 12
						* (vectors.length - 1));
	}

	private void createBuffers() {
		allocBuffers();
		Vector3D p =
				new Vector3D((float) Math.random(),
						(float) Math.random(),
						(float) Math.random());
		p.normalize();
		Vector3D[] r = new Vector3D[vectors.length], s =
				new Vector3D[vectors.length];

		// Gen r + s
		for (int i = 0; i < vectors.length; i++) {
			Vector3D change;
			if (i < vectors.length - 1) {
				change =
						vectors[i].clone().subtract(
								vectors[i + 1]);
			} else {
				change =
						vectors[i].clone().subtract(
								vectors[i - 1]);
			}
			r[i] = Vector3D.crossProduct(change, p).normalize();
			s[i] =
					Vector3D.crossProduct(change, r[i])
							.normalize();

			if (i > 0 && i < vectors.length - 1) {
				r[i - 1].add(r[i]).normalize();
				s[i - 1].add(s[i]).normalize();
			}
		}

		double angle;
		Color lColor = Color.WHITE;
		float lRadi = 10;
		for (int i = 0; i < vectors.length; i++) {
			if (i < radi.length)
				lRadi = radi[i];
			if (i < colors.length)
				lColor = colors[i];
			angle = 0;
			for (int sI = 0; sI < slices + 1; sI++, angle +=
					sliceSize) {
				if (sI < slices) {
					Vector3D normal =
							new Vector3D(
									(float) (r[i].x
											* Math.cos(angle) + s[i].x
											* Math.sin(angle)),
									(float) (r[i].y
											* Math.cos(angle) + s[i].y
											* Math.sin(angle)),
									(float) (r[i].z
											* Math.cos(angle) + s[i].z
											* Math.sin(angle)))
									.normalize();
					vertexBuffer.put(vectors[i].x
							+ (normal.x * lRadi));
					vertexBuffer.put(vectors[i].y
							+ (normal.y * lRadi));
					vertexBuffer.put(vectors[i].z
							+ (normal.z * lRadi));

					normalBuffer.put(normal.x);
					normalBuffer.put(normal.y);
					normalBuffer.put(normal.z);

					colorBuffer.put(lColor.getRed() / 255f);
					colorBuffer.put(lColor.getGreen() / 255f);
					colorBuffer.put(lColor.getBlue() / 255f);
				}

				if (i > 0 && sI > 0) {
					int lOff = sI < slices ? sI : 0;
					int below = ((i - 1) * slices);
					int curr = below + slices;
					indexBuffer.put(curr + lOff);
					indexBuffer.put(below + sI - 1);
					indexBuffer.put(curr + sI - 1);

					indexBuffer.put(curr + lOff);
					indexBuffer.put(below + lOff);
					indexBuffer.put(below + sI - 1);
				}
			}
		}

		colorBuffer = (FloatBuffer) colorBuffer.flip();
		vertexBuffer = (FloatBuffer) vertexBuffer.flip();
		indexBuffer = (IntBuffer) indexBuffer.flip();
		normalBuffer = (FloatBuffer) normalBuffer.flip();
	}

	@Override
	public FloatBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	@Override
	public FloatBuffer getColorBuffer() {
		return colorBuffer;
	}

	@Override
	public FloatBuffer getNormalBuffer() {
		return normalBuffer;
	}

	@Override
	public IntBuffer getIndexBuffer() {
		return indexBuffer;
	}

	@Override
	public int getIndexCount() {
		return indexBuffer.limit();
	}

	@Override
	public int getColorBufferSize() {
		return 4;
	}

	@Override
	public int getRenderMethod() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public FloatBuffer getTextureBuffer() {
		return null;
	}
}
