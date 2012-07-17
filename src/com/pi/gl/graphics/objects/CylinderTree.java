package com.pi.gl.graphics.objects;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.media.opengl.GL;

import com.jogamp.common.nio.Buffers;

public class CylinderTree implements Mesh {
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer textureBuffer;

	private IntBuffer indexBuffer;

	private Vector3DNode rootNode;

	protected int slices;
	protected float sliceSize;
	protected int nodeCount = 0;
	protected int primitiveCount = 0;
	public Vector3D[] r, s, info;
	private int[] normals;
	private int depth = 0;

	Vector3D cProduct = new Vector3D((float) Math.random(),
			(float) Math.random(), (float) Math.random())
			.normalize();

	public CylinderTree(Vector3DNode rootNode, int slices) {
		this.rootNode = rootNode;
		this.slices = slices;
		this.sliceSize =
				(float) ((Math.PI * 2f) / ((float) slices));

		indexNode(rootNode);
		if (nodeCount < 2)
			throw new RuntimeException("Not enough nodes!");

		createBuffers();
	}

	protected void indexNode(Vector3DNode node) {
		if (node.getID() != -1)
			return;
		node.setID(nodeCount);
		nodeCount++;
		node.setDepth(depth);
		depth++;
		int cDepth = depth;
		for (Vector3DNode n : node.getChildren()) {
			indexNode(n);
			depth = cDepth;
			primitiveCount += slices + slices;
		}
	}

	private void allocBuffers() {
		// Count the vertices and indices
		int verts = slices * nodeCount;
		vertexBuffer = Buffers.newDirectFloatBuffer(verts * 3);
		normalBuffer = Buffers.newDirectFloatBuffer(verts * 3);
		colorBuffer =
				Buffers.newDirectFloatBuffer(verts
						* getColorBufferSize());
		indexBuffer =
				Buffers.newDirectIntBuffer(primitiveCount * 3);
		textureBuffer = Buffers.newDirectFloatBuffer(verts * 2);

		r = new Vector3D[nodeCount];
		s = new Vector3D[nodeCount];
		normals = new int[nodeCount];
		info = new Vector3D[nodeCount];
	}

	private void computeIntersection(Vector3DNode n) {
		if (n.getID() == -1)
			return;
		info[n.getID()] = n;
		if (n.getParent() != null && n.getParent().getID() != -1) {
			Vector3D change =
					n.cloneVector().subtract(n.getParent());
			if (change.x < 0)
				change = Vector3D.negative(change);
			r[n.getID()] =
					Vector3D.crossProduct(change, cProduct)
							.normalize();

			s[n.getID()] =
					Vector3D.crossProduct(change, r[n.getID()])
							.normalize();
			normals[n.getID()]++;
			if (r[n.getParent().getID()] == null
					|| s[n.getParent().getID()] == null) {
				r[n.getParent().getID()] = r[n.getID()].clone();
				s[n.getParent().getID()] = s[n.getID()].clone();
			} else {
				r[n.getParent().getID()].add(r[n.getID()])
						.normalize();
				s[n.getParent().getID()].add(s[n.getID()])
						.normalize();
			}
			normals[n.getParent().getID()]++;
		}
		for (Vector3DNode c : n.getChildren())
			computeIntersection(c);
	}

	private void cleanIntersections() {
		for (int i = 0; i < nodeCount; i++) {
			r[i].multiply(1f / normals[i]);
			s[i].multiply(1f / normals[i]);
		}
	}

	private void createBuffers() {
		allocBuffers();

		computeIntersection(rootNode);
		cleanIntersections();

		genNodeBuffer(rootNode);

		indexBuffer = (IntBuffer) indexBuffer.flip();
	}

	private void genNodeBuffer(Vector3DNode n) {
		if (n.getID() == -1)
			return;
		double angle = 0;
		for (int sI = 0; sI < slices + 1; sI++, angle +=
				sliceSize) {
			if (sI < slices) {
				int mIDX = ((n.getID() * slices) + sI) * 3;
				int cIDX =
						((n.getID() * slices) + sI)
								* getColorBufferSize();
				int tIDX = ((n.getID() * slices) + sI) * 2;

				Vector3D normal =
						new Vector3D(
								(float) (r[n.getID()].x
										* Math.cos(angle) + s[n.getID()].x
										* Math.sin(angle)),
								(float) (r[n.getID()].y
										* Math.cos(angle) + s[n
										.getID()].y
										* Math.sin(angle)),
								(float) (r[n.getID()].z
										* Math.cos(angle) + s[n
										.getID()].z
										* Math.sin(angle)))
								.normalize();
				vertexBuffer.put(mIDX,
						n.x + (normal.x * n.getRadius()));
				vertexBuffer.put(mIDX + 1,
						n.y + (normal.y * n.getRadius()));
				vertexBuffer.put(mIDX + 2,
						n.z + (normal.z * n.getRadius()));

				normalBuffer.put(mIDX, normal.x);
				normalBuffer.put(mIDX + 1, normal.y);
				normalBuffer.put(mIDX + 2, normal.z);

				colorBuffer.put(cIDX,
						n.getColor().getRed() / 255f);
				colorBuffer.put(cIDX + 1, n.getColor()
						.getGreen() / 255f);
				colorBuffer.put(cIDX + 2,
						n.getColor().getBlue() / 255f);
				colorBuffer.put(cIDX + 3, n.getColor()
						.getAlpha() / 255f);

				textureBuffer.put(tIDX, sI & 1);
				textureBuffer.put(tIDX + 1, n.getDepth() & 1);
			}

			if (n.getParent() != null && sI > 0
					&& n.getParent().getID() != -1) {
				int lOff = sI < slices ? sI : 0;
				int below = (n.getParent().getID() * slices);
				int curr = (n.getID() * slices);
				indexBuffer.put(curr + lOff);
				indexBuffer.put(below + lOff);
				indexBuffer.put(below + sI - 1);

				indexBuffer.put(curr + lOff);
				indexBuffer.put(below + sI - 1);
				indexBuffer.put(curr + sI - 1);
			}
		}

		for (Vector3DNode c : n.getChildren()) {
			genNodeBuffer(c);
		}
	}

	/*
	 * private int getMatchingIndex(int startIndex, int endIndex, int myIX) {
	 * double bDist = Double.MAX_VALUE; int bIx = -1; int mIX3 = myIX * 3;
	 * Vector3D search = new Vector3D(vertexBuffer.get(mIX3),
	 * vertexBuffer.get(mIX3 + 1), vertexBuffer.get(mIX3 + 2)); for (int i =
	 * startIndex; i < endIndex; i++) { Vector3D v = new
	 * Vector3D(vertexBuffer.get(i * 3), vertexBuffer.get((i * 3) + 1),
	 * vertexBuffer.get((i * 3) + 2)); double mD = v.dist(search);
	 * System.out.println(i + search.toString() + ":" + mD); if (mD < bDist) {
	 * bDist = mD; bIx = i; } } return bIx; }
	 */

	public abstract static class Vector3DNode extends Vector3D {
		private int id = -1;
		private int depth;

		public Vector3DNode(float x, float y, float z) {
			super(x, y, z);
		}

		public abstract List<Vector3DNode> getChildren();

		public abstract Vector3DNode getParent();

		public void setDepth(int i) {
			this.depth = i;
		}

		public int getDepth() {
			return depth;
		}

		public Color getColor() {
			return Color.white;
		}

		public float getRadius() {
			return 10;
		}

		public final void setID(int id) {
			this.id = id;
		}

		public final int getID() {
			return id;
		}

		public Vector3D cloneVector() {
			return super.clone();
		}
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

	public Vector3DNode getRootNode() {
		return rootNode;
	}

	@Override
	public FloatBuffer getTextureBuffer() {
		return textureBuffer;
	}
}
