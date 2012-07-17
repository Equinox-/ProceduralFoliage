package com.pi.gl.graphics;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.pi.gl.graphics.objects.CylinderTree;
import com.pi.gl.graphics.objects.CylinderTree.Vector3DNode;
import com.pi.gl.graphics.objects.HeightmapMesh;
import com.pi.gl.graphics.objects.Mesh;
import com.pi.gl.graphics.objects.MeshLoader;
import com.pi.gl.graphics.objects.Vector3D;
import com.pi.gl.graphics.objects.lsystem.Tuple;
import com.pi.gl.graphics.objects.lsystem.TupleLeaf;

public class RenderLoop implements GLEventListener {
	private DisplayManager displayManager;
	private HeightmapMesh terrain;
	private List<Mesh> meshes = new ArrayList<Mesh>();
	private double horizontalTan = Math.tan(Math.toRadians(25));

	private Texture barkTexture;
	private Texture leafTexture;
	private Texture grassTexture;
	private Texture skybox;
	private Texture[] groundFoliageTexture;
	private boolean[] coloredFoliage;

	private int[][] groundFoliage;
	float terrainScale = 100;
	float groundFoliageSizeMin = 50;
	float groundFoliageSizeMax = 100;

	float areaSize;

	private boolean[][] hasFoliage;

	public RenderLoop(DisplayManager displayManager) {
		this.displayManager = displayManager;
		float[][] hMap;
		try {
			hMap =
					MeshLoader.loadHeightMap(new File(
							"heightmap.png"), 0, 2000);
			hasFoliage =
					new boolean[hMap.length][hMap[0].length];
		} catch (Exception e) {
			hMap = new float[50][50];
			hasFoliage = new boolean[50][50];
		}
		areaSize =
				Math.max(Math.max(terrainScale
						* (hMap.length / 2f), terrainScale
						* (hMap[0].length / 2f)), 250);

		meshes.add(terrain =
				new HeightmapMesh(hMap, terrainScale,
						new Vector3D(-terrainScale
								* (hMap.length / 2f), -1500,
								-terrainScale
										* (hMap[0].length / 2f))));

		for (int i = 0; i < 50; i++) {
			Vector3D loc = new Vector3D(0, 0, 0);
			loc.x =
					(float) (Math.random() * (float) hMap.length);
			loc.z =
					(float) (Math.random() * (float) hMap[0].length);
			loc.y = terrain.getHeight(loc.x, loc.z);
			loc.x *= terrainScale;
			loc.z *= terrainScale;
			loc.add(terrain.getOffset());
			Tuple t =
					Tuple.example1(loc,
							5 + (int) (Math.random() * 4));
			if (t.nodes >= 2) {
				Mesh mesh = new CylinderTree(t.parent, 10);
				meshes.add(mesh);
				System.out.println("Tree: " + i);
			}
		}

		int groundFoliageCount = 2500;
		groundFoliageTexture = new Texture[8];
		coloredFoliage =
				new boolean[] { false, true, true, true, false,
						false, false, false };
		groundFoliage = new int[groundFoliageCount][8];
		for (int i = 0; i < groundFoliageCount; i++) {
			float x =
					(int) (Math.random() * (float) hMap.length);
			float z =
					(int) (Math.random() * (float) hMap[0].length);
			if (hasFoliage[(int) x][(int) z])
				continue;
			hasFoliage[(int) x][(int) z] = true;
			float y = Float.MAX_VALUE;
			for (int xO = 0; xO <= 1; xO++) {
				for (int zO = 0; zO <= 1; zO++) {
					y =
							Math.min(
									y,
									terrain.getHeight(x + xO, z
											+ zO));
				}
			}
			x *= terrainScale;
			z *= terrainScale;
			groundFoliage[i][0] =
					(int) (x + terrain.getOffset().x);
			groundFoliage[i][1] =
					(int) (y + terrain.getOffset().y);
			groundFoliage[i][2] =
					(int) (z + terrain.getOffset().z);
			groundFoliage[i][3] =
					(int) (Math.random() * ((float) groundFoliageTexture.length));
			if (coloredFoliage[groundFoliage[i][3]]) {
				groundFoliage[i][4] = 255;
				groundFoliage[i][5] = 255;
				groundFoliage[i][6] = 255;
			} else {
				Color c = terrain.getColorAtHeight(y);
				groundFoliage[i][4] = c.getRed();
				groundFoliage[i][5] = c.getGreen();
				groundFoliage[i][6] = c.getBlue();
			}
			groundFoliage[i][7] =
					(int) ((Math.random() * (groundFoliageSizeMax - groundFoliageSizeMin)) + groundFoliageSizeMin);
			System.out.println("Ground Foliage: " + i);
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA,
				GL.GL_ONE_MINUS_SRC_ALPHA);

		try {
			barkTexture =
					TextureIO.newTexture(new File("bark.png"),
							true);
			leafTexture =
					TextureIO.newTexture(new File("leaves.png"),
							true);
			grassTexture =
					TextureIO.newTexture(new File("grass.png"),
							true);
			skybox =
					TextureIO.newTexture(new File("skybox.png"),
							true);
			for (int i = 0; i < groundFoliageTexture.length; i++) {
				File f = new File("groundFoliage" + i + ".png");
				if (f.exists()) {
					groundFoliageTexture[i] =
							TextureIO.newTexture(f, true);
				} else {
					groundFoliageTexture[i] =
							groundFoliageTexture[i - 1];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Called when the animator stops
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glScissor(0, 0, drawable.getWidth(),
				drawable.getHeight());
		gl.glLightfv(
				GL2.GL_LIGHT0,
				GL2.GL_DIFFUSE,
				Buffers.newDirectFloatBuffer(new float[] { 1f,
						1f, 1f }));
		gl.glLightfv(
				GL2.GL_LIGHT0,
				GL2.GL_POSITION,
				Buffers.newDirectFloatBuffer(new float[] { .7f,
						.7f, -1f }));
		gl.glLightModelfv(
				GL2.GL_LIGHT_MODEL_AMBIENT,
				Buffers.newDirectFloatBuffer(new float[] { .15f,
						.15f, .15f }));
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE,
				GL.GL_FALSE);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT
				| GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		displayManager.getCamera().translate(gl);

		renderSkybox(gl);

		// This is where the main render logic occurs.
		// gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		for (Mesh mesh : meshes) {
			if (mesh.getTextureBuffer() != null) {
				gl.glEnable(GL.GL_TEXTURE_2D);
				if (mesh instanceof CylinderTree) {
					barkTexture.bind();
				} else if (mesh instanceof HeightmapMesh) {
					grassTexture.bind();
				}
				gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
				gl.glTexCoordPointer(2, GL.GL_FLOAT, 0,
						mesh.getTextureBuffer());
			}
			gl.glVertexPointer(3, GL.GL_FLOAT, 0,
					mesh.getVertexBuffer());
			gl.glColorPointer(mesh.getColorBufferSize(),
					GL.GL_FLOAT, 0, mesh.getColorBuffer());
			gl.glNormalPointer(GL.GL_FLOAT, 0,
					mesh.getNormalBuffer());

			gl.glDrawElements(mesh.getRenderMethod(),
					mesh.getIndexCount(), GL2.GL_UNSIGNED_INT,
					mesh.getIndexBuffer());
			if (mesh.getTextureBuffer() != null) {
				gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
		}
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

		for (Mesh mesh : meshes) {
			if (mesh instanceof CylinderTree) {
				render(gl, ((CylinderTree) mesh).getRootNode());
			}
		}

		for (int[] gF : groundFoliage) {
			renderGroundFoliage(gl, gF);
		}

		TextRenderer r =
				new TextRenderer(new Font("Arial", Font.PLAIN,
						10));
		r.beginRendering(drawable.getWidth(),
				drawable.getHeight());
		r.draw(displayManager.getCamera().toString(), 0,
				drawable.getHeight() - 15);
		r.endRendering();
	}

	public void renderGroundFoliage(GL2 gl, int[] data) {
		if (data.length == 8) {
			float foliageSize = data[7];
			gl.glEnable(GL.GL_TEXTURE_2D);
			groundFoliageTexture[data[3]].bind();
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor4f(data[4] / 255f, data[5] / 255f,
					data[6] / 255f, 1f);

			gl.glTexCoord2f(0f, 1f);
			gl.glVertex3f(data[0], data[1], data[2]);
			gl.glTexCoord2f(0f, 0f);
			gl.glVertex3f(data[0], data[1] + foliageSize,
					data[2]);
			gl.glTexCoord2f(1f, 0f);
			gl.glVertex3f(data[0] + foliageSize, data[1]
					+ foliageSize, data[2] + foliageSize);
			gl.glTexCoord2f(1f, 1f);
			gl.glVertex3f(data[0] + foliageSize, data[1],
					data[2] + foliageSize);

			gl.glTexCoord2f(0f, 1f);
			gl.glVertex3f(data[0], data[1], data[2]
					+ foliageSize);
			gl.glTexCoord2f(0f, 0f);
			gl.glVertex3f(data[0], data[1] + foliageSize,
					data[2] + foliageSize);
			gl.glTexCoord2f(1f, 0f);
			gl.glVertex3f(data[0] + foliageSize, data[1]
					+ foliageSize, data[2]);
			gl.glTexCoord2f(1f, 1f);
			gl.glVertex3f(data[0] + foliageSize, data[1],
					data[2]);

			gl.glEnd();
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
	}

	private Vector3D lightN = new Vector3D(.7f, .7f, -1f);

	public void renderSkybox(GL2 gl) {
		gl.glEnable(GL.GL_TEXTURE_2D);
		skybox.bind();

		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4f(1f, 1f, 1f, 1f);

		// Bottom
		gl.glTexCoord2f(.33333f, .5f);
		gl.glVertex3f(-areaSize, -areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, .5f);
		gl.glVertex3f(areaSize, -areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, .75f);
		gl.glVertex3f(areaSize, -areaSize, areaSize);
		gl.glTexCoord2f(.33333f, .75f);
		gl.glVertex3f(-areaSize, -areaSize, areaSize);

		// Top
		gl.glTexCoord2f(.33333f, 0f);
		gl.glVertex3f(-areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, 0f);
		gl.glVertex3f(areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, .25f);
		gl.glVertex3f(areaSize, areaSize, areaSize);
		gl.glTexCoord2f(.33333f, .25f);
		gl.glVertex3f(-areaSize, areaSize, areaSize);

		// Mid Back
		gl.glTexCoord2f(.33333f, .25f);
		gl.glVertex3f(-areaSize, areaSize, areaSize);
		gl.glTexCoord2f(.66666f, .25f);
		gl.glVertex3f(areaSize, areaSize, areaSize);
		gl.glTexCoord2f(.66666f, .5f);
		gl.glVertex3f(areaSize, -areaSize, areaSize);
		gl.glTexCoord2f(.33333f, .5f);
		gl.glVertex3f(-areaSize, -areaSize, areaSize);

		// Mid Left
		gl.glTexCoord2f(0f, .25f);
		gl.glVertex3f(-areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.33333f, .25f);
		gl.glVertex3f(-areaSize, areaSize, areaSize);
		gl.glTexCoord2f(.33333f, .5f);
		gl.glVertex3f(-areaSize, -areaSize, areaSize);
		gl.glTexCoord2f(0f, .5f);
		gl.glVertex3f(-areaSize, -areaSize, -areaSize);

		// Mid Right
		gl.glTexCoord2f(1f, .25f);
		gl.glVertex3f(areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, .25f);
		gl.glVertex3f(areaSize, areaSize, areaSize);
		gl.glTexCoord2f(.66666f, .5f);
		gl.glVertex3f(areaSize, -areaSize, areaSize);
		gl.glTexCoord2f(1f, .5f);
		gl.glVertex3f(areaSize, -areaSize, -areaSize);

		// Mid Front
		gl.glTexCoord2f(.33333f, 1f);
		gl.glVertex3f(-areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, 1f);
		gl.glVertex3f(areaSize, areaSize, -areaSize);
		gl.glTexCoord2f(.66666f, .75f);
		gl.glVertex3f(areaSize, -areaSize, -areaSize);
		gl.glTexCoord2f(.33333f, .75f);
		gl.glVertex3f(-areaSize, -areaSize, -areaSize);

		gl.glEnd();
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

	public void render(GL2 gl, Vector3DNode n) {
		if (n instanceof TupleLeaf) {
			TupleLeaf l = (TupleLeaf) n;
			l.calcNormals();
			Vector3D normal = l.getNormal();
			Vector3D normalR = normal.clone().reverse();
			if (normalR.dist(lightN) < normal.dist(lightN)) {
				normal = normalR;
			}
			normal.add(lightN.clone().multiply(0.35f))
					.normalize();
			if (normal != null) {
				gl.glEnable(GL.GL_TEXTURE_2D);
				leafTexture.bind();
				gl.glBegin(GL.GL_TRIANGLE_FAN);
				gl.glColor4f(1f, 1f, 1f, 1f);// 0f, 1f, 0f, 0.75f);
				// gl.glNormal3f(normal.x, normal.y, normal.z);
				// gl.glTexCoord2f(0, 0);
				// gl.glVertex3f(l.x, l.y, l.z);
				for (int i = 0; i < l.getNodes().size(); i++) {
					gl.glTexCoord2f(i <= 2 && i > 0 ? 1f : 0f,
							i >= 2 ? 1f : 0f);
					gl.glNormal3f(normal.x, normal.y, normal.z);
					gl.glVertex3f(l.getNodes().get(i).x, l
							.getNodes().get(i).y, l.getNodes()
							.get(i).z);
				}
				gl.glEnd();
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
		} else {
			for (Vector3DNode node : n.getChildren()) {
				render(gl, node);
			}
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y,
			int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		// Define the world projection
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glViewport(0, 0, width, height);
		double aspect = ((double) height) / ((double) width);
		gl.glFrustum(-horizontalTan, horizontalTan, aspect
				* -horizontalTan, aspect * horizontalTan, 1,
				100000);
	}
}
