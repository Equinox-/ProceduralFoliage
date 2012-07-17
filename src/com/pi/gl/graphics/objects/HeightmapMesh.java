package com.pi.gl.graphics.objects;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.jogamp.common.nio.Buffers;

public class HeightmapMesh implements Mesh {
	private float[][] heightMap;
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer textureBuffer;

	private IntBuffer indexBuffer;
	private Vector3D offset;
	private float spacing;
	private float minHeight;
	private float maxHeight;
	private Color[] colorMapping = { Color.GREEN, Color.GREEN,
			new Color(139, 169, 19) };

	public HeightmapMesh(float[][] heightMap, float spacing,
			Vector3D off) {
		this.heightMap = heightMap;
		this.spacing = spacing;
		this.offset = off;

		minHeight = Float.MAX_VALUE;
		maxHeight = Float.MIN_VALUE;
		for (float[] fA : heightMap) {
			for (float f : fA) {
				minHeight = Math.min(minHeight, f);
				maxHeight = Math.max(maxHeight, f + 1f);
			}
		}
		createBuffers();
	}

	public Color getColorAtHeight(float height) {
		return getColorAt((height - minHeight)
				/ (maxHeight - minHeight));
	}

	public void createBuffers() {
		vertexBuffer =
				Buffers.newDirectFloatBuffer(heightMap.length
						* heightMap[0].length * 3);
		colorBuffer =
				Buffers.newDirectFloatBuffer(heightMap.length
						* heightMap[0].length * 3);
		textureBuffer =
				Buffers.newDirectFloatBuffer(heightMap.length
						* heightMap[0].length * 2);

		float[] normalCount =
				new float[heightMap.length * heightMap[0].length];
		int[] ix = new int[3];
		normalBuffer =
				Buffers.newDirectFloatBuffer(heightMap.length
						* heightMap[0].length * 3);

		indexBuffer =
				Buffers.newDirectIntBuffer((heightMap.length - 1)
						* (heightMap[0].length - 1) * 6);

		for (int x = 0; x < heightMap.length; x++) {
			for (int z = 0; z < heightMap[0].length; z++) {
				vertexBuffer.put(((float) x) * spacing
						+ offset.x);
				vertexBuffer.put(heightMap[x][z] + offset.y);
				vertexBuffer.put(((float) z) * spacing
						+ offset.z);

				Color c =
						getColorAt((heightMap[x][z] - minHeight)
								/ (maxHeight - minHeight));
				colorBuffer.put(c.getRed() / 255f);
				colorBuffer.put(c.getGreen() / 255f);
				colorBuffer.put(c.getBlue() / 255f);

				textureBuffer.put(x & 1);
				textureBuffer.put(z & 1);

				if (x > 0 && z > 0) {
					// Shared between triangles
					ix[1] = (z - 1 + (x * heightMap[0].length));
					ix[2] =
							(z + ((x - 1) * heightMap[0].length));

					indexBuffer.put(ix[0] = (ix[1] + 1));
					indexBuffer.put(ix[1]);
					indexBuffer.put(ix[2]);
					Vector3D v =
							getFaceNormal(ix[0], ix[1], ix[2]);
					for (int i : ix) {
						int i3 = i * 3;
						normalBuffer.put(i3,
								normalBuffer.get(i3) + v.x);
						normalBuffer.put(i3 + 1,
								normalBuffer.get(i3 + 1) + v.y);
						normalBuffer.put(i3 + 2,
								normalBuffer.get(i3 + 2) + v.z);
						normalCount[i]++;
					}

					indexBuffer.put(ix[0] = (ix[2] - 1));
					indexBuffer.put(ix[2]);
					indexBuffer.put(ix[1]);
					v = getFaceNormal(ix[0], ix[2], ix[1]);
					for (int i : ix) {
						int i3 = i * 3;
						normalBuffer.put(i3,
								normalBuffer.get(i3) + v.x);
						normalBuffer.put(i3 + 1,
								normalBuffer.get(i3 + 1) + v.y);
						normalBuffer.put(i3 + 2,
								normalBuffer.get(i3 + 2) + v.z);
						normalCount[i]++;
					}
				}
			}
		}

		// Rescan the normals and perform the average function on them
		for (int i = 0, i2 = 0; i < normalCount.length
				&& i2 < normalBuffer.limit() - 2; i++, i2 += 3) {
			normalBuffer.put(i2, normalBuffer.get(i2)
					/ normalCount[i]);
			normalBuffer.put(i2 + 1, normalBuffer.get(i2 + 1)
					/ normalCount[i]);
			normalBuffer.put(i2 + 2, normalBuffer.get(i2 + 2)
					/ normalCount[i]);
		}

		colorBuffer = (FloatBuffer) colorBuffer.flip();
		vertexBuffer = (FloatBuffer) vertexBuffer.flip();
		indexBuffer = (IntBuffer) indexBuffer.flip();
		textureBuffer = (FloatBuffer) textureBuffer.flip();
	}

	public Color getColorAt(float heightNormal) {
		float mapRoot =
				heightNormal * (colorMapping.length - 1f);
		int lowerCap = (int) Math.floor(mapRoot);
		int upperCap = lowerCap + 1;
		mapRoot -= lowerCap;
		float rC =
				colorMapping[upperCap].getRed()
						- colorMapping[lowerCap].getRed();
		float gC =
				colorMapping[upperCap].getGreen()
						- colorMapping[lowerCap].getGreen();
		float bC =
				colorMapping[upperCap].getBlue()
						- colorMapping[lowerCap].getBlue();
		return new Color(colorMapping[lowerCap].getRed()
				+ (int) (rC * mapRoot),
				colorMapping[lowerCap].getGreen()
						+ (int) (gC * mapRoot),
				colorMapping[lowerCap].getBlue()
						+ (int) (bC * mapRoot));
	}

	@Override
	public int getIndexCount() {
		return indexBuffer.limit();
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

	public float getHeight(float x, float z) {
		if (x < 0)
			x = 0;
		if (z < 0)
			z = 0;
		if (x >= heightMap.length - 1)
			x = heightMap.length - 2;
		if (z >= heightMap[(int) x].length - 1)
			z = heightMap[(int) x].length - 2;
		float a = heightMap[(int) x][(int) z];
		float s = x - ((int) x);
		float t = z - ((int) z);
		if (s == 0 && t == 0)
			return a;
		float b = heightMap[(int) x + 1][(int) z];
		float c = heightMap[(int) x][(int) z + 1];
		float d = heightMap[(int) x + 1][(int) z + 1];
		if (s + t <= 1f) {
			float uy = b - a;
			float vy = c - a;
			return a + (s * uy) + (t * vy);
		} else {
			float uy = c - d;
			float vy = b - d;
			return d + (1f - s) * uy + (1f - t) * vy;
		}
	}

	public Vector3D getFaceNormal(int iAt, int iBt, int iCt) {
		int iA = iAt * 3;
		int iB = iBt * 3;
		int iC = iCt * 3;
		float xA = vertexBuffer.get(iB) - vertexBuffer.get(iA);
		float yA =
				vertexBuffer.get(iB + 1)
						- vertexBuffer.get(iA + 1);
		float zA =
				vertexBuffer.get(iB + 2)
						- vertexBuffer.get(iA + 2);
		float xB = vertexBuffer.get(iC) - vertexBuffer.get(iA);
		float yB =
				vertexBuffer.get(iC + 1)
						- vertexBuffer.get(iA + 1);
		float zB =
				vertexBuffer.get(iC + 2)
						- vertexBuffer.get(iA + 2);

		double xC = yA * zB - yB * zA;
		double yC = zA * xB - zB * xA;
		double zC = xA * yB - xB * yA;

		double sqrt =
				Math.sqrt((xC * xC) + (yC * yC) + (zC * zC));
		xC /= sqrt;
		yC /= sqrt;
		zC /= sqrt;
		return new Vector3D((float) xC, (float) yC, (float) zC);
	}

	public Vector3D getOffset() {
		return offset;
	}

	@Override
	public int getColorBufferSize() {
		return 3;
	}

	@Override
	public int getRenderMethod() {
		return GL.GL_TRIANGLES;
	}

	@Override
	public FloatBuffer getTextureBuffer() {
		return textureBuffer;
	}
}
