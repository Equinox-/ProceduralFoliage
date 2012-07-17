package com.pi.gl.graphics.objects;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface Mesh {
	public int getIndexCount();

	public FloatBuffer getVertexBuffer();

	public FloatBuffer getColorBuffer();

	public FloatBuffer getNormalBuffer();

	public FloatBuffer getTextureBuffer();

	public IntBuffer getIndexBuffer();

	public int getColorBufferSize();

	public int getRenderMethod();
}
