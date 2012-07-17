package com.pi.gl.graphics.objects.lsystem;

import java.awt.Color;
import java.util.LinkedList;

import com.pi.gl.graphics.objects.Vector3D;
import com.pi.mathz.Matrix;
import com.pi.mathz.TransMatrix;

public class Tuple {
	private final TupleConfig cfg;
	private int iteration = 0;
	private String state = "";
	private String start;
	private double sigma = 22.5f;
	public TupleNode parent;
	int maxDepth = 0;
	public int nodes = 0;

	int minThick = 1;
	int maxThick = 10;

	Color maxColor = new Color(161, 124, 71);
	Color minColor = new Color(161, 124, 71).darker().darker();
	private TransMatrix startMatrix;
	private Vector3D heading;

	static Color mapColor(float blend, Color c1, Color c2) {
		if (blend < 0)
			blend = 0;
		if (blend > 1)
			blend = 1;
		float r = c2.getRed() - c1.getRed();
		float g = c2.getGreen() - c1.getGreen();
		float b = c2.getBlue() - c1.getBlue();
		r *= blend;
		g *= blend;
		b *= blend;
		return new Color(c1.getRed() + (int) (r), c1.getGreen()
				+ (int) (g), c1.getBlue() + (int) (b));
	}

	public Tuple(TupleConfig cfg, String start, double d) {
		this.cfg = cfg;
		this.start = start;
		this.state = start;
		this.sigma = d;
		this.startMatrix = new TransMatrix().identity();
		this.heading = new Vector3D(0, 1, 0);
	}

	public void update() {
		iteration++;
		state = cfg.apply(state);
	}

	public void setHeading(Vector3D v) {
		this.heading = v;
	}

	public void jumpItr(int jump) {
		if (jump == iteration)
			return;
		if (jump < iteration) {
			iteration = 0;
			state = start;
		}
		while (iteration < jump) {
			update();
		}
	}

	public void setStartMatrix(TransMatrix m) {
		this.startMatrix = m;
	}

	public int getIterationID() {
		return iteration;
	}

	public String getIteration() {
		return state;
	}

	public void genTree(float x, float y, float z) {
		nodes = 0;
		LinkedList<Object[]> lStack = new LinkedList<Object[]>();
		TupleNode current =
				parent =
						new TupleNode(this, null, 0, null, x, y,
								z);
		TransMatrix trans =
				new TransMatrix((Matrix) startMatrix.clone());
		char[] cA = state.toCharArray();
		int depth = 0;
		maxDepth = 0;
		boolean inPolygon = false;
		LinkedList<Vector3D> polygonVectors =
				new LinkedList<Vector3D>();

		for (int i = 0; i < cA.length; i++) {
			char c = cA[i];
			switch (c) {
			case '{':
				if (inPolygon) {
					throw new RuntimeException("Double '{'");
				}
				polygonVectors.add(current.cloneVector());
				inPolygon = true;
			case '[':
				lStack.addLast(new Object[] { current,
						trans.clone(), depth });
				break;
			case '}':
				if (!inPolygon) {
					throw new RuntimeException("Unopened '}'");
				}
				inPolygon = false;
				if (polygonVectors.size() > 0) {
					Vector3D vec = new Vector3D(0, 0, 0);
					for (Vector3D n : polygonVectors)
						vec.add(n);
					vec.multiply(1f / ((float) polygonVectors
							.size()));
					TupleLeaf nN =
							new TupleLeaf(this, current, depth,
									"", vec.x, vec.y, vec.z);
					for (Vector3D v : polygonVectors) {
						nN.addChild(new TupleNode(this, nN,
								depth + 1, "", v.x, v.y, v.z));
					}
					current.addChild(nN);
					polygonVectors.clear();
				}
			case ']':
				Object[] dat = lStack.removeLast();
				current = (TupleNode) dat[0];
				trans = new TransMatrix((Matrix) dat[1]);
				depth = (Integer) dat[2];
				break;
			case '+':
				trans =
						trans.multiply(new TransMatrix()
								.setXRotation(sigma));
				break;
			case '-':
				trans =
						trans.multiply(new TransMatrix()
								.setXRotation(-sigma));
				break;
			case '&':
				trans =
						trans.multiply(new TransMatrix()
								.setZRotation(sigma));
				break;
			case '^':
				trans =
						trans.multiply(new TransMatrix()
								.setZRotation(-sigma));
				break;
			case '\\':
			case '<':
				trans =
						trans.multiply(new TransMatrix()
								.setYRotation(sigma));
				break;
			case '/':
			case '>':
				trans =
						trans.multiply(new TransMatrix()
								.setYRotation(-sigma));
				break;
			case '|':
				trans =
						trans.multiply(new TransMatrix()
								.setXRotation(Math.PI));
				break;
			case 'f':
				nodes++;

				String ID = null;
				if (i < cA.length - 1) {
					if (cA[i + 1] == '{') {
						int nX = state.indexOf('}', i + 2);
						if (nX == -1) {
							throw new RuntimeException(
									"Unclosed '{'");
						}
						ID = state.substring(i + 2, nX);
						i = nX;
					}
				}
				int size = 1;
				if (i < cA.length - 1) {
					if (cA[i + 1] == '|') {
						int nX = state.indexOf('|', i + 2);
						if (nX == -1) {
							throw new RuntimeException(
									"Unclosed '|'");
						}
						try {
							size =
									Integer.valueOf(state
											.substring(i + 2, nX));
						} catch (NumberFormatException e) {
						}
						i = nX;
					}
				}
				Vector3D v =
						trans.multiply(heading.clone().multiply(
								size));
				if (inPolygon) {
					v.add(polygonVectors.getLast());
					polygonVectors.add(v);
				} else {
					v.add(current);
					TupleNode nN =
							new TupleNode(this, current, depth,
									ID, v.x, v.y, v.z);
					current.addChild(nN);
					current = nN;
					maxDepth = Math.max(depth, maxDepth);
					depth++;
				}
				break;
			}
		}
	}

	public static Tuple example1(Vector3D base, int itr) {

		/*
		 * Tuple t = new Tuple(new TupleConfig(new char[] { 'a', 'f', 'l', 's'
		 * }, new String[] { "[&FL!A]/////’[&FL!A]///////’[&FL!A]", "S ///// F",
		 * "[’’’^^{-f+f+f-|-f+f+f}]", "F L" }), "a", Math.toRadians(22.5f));
		 */
		String start = "[&GGFL!A]";
		for (int i = 0; i < 2; i++)
			if (Math.random() > 0.5) {
				start += "/////’[&GGFL!A]";
			}
		/*
		 * Tuple t = new Tuple(new TupleConfig(new char[] { 'a', 'f', 'l', 's'
		 * }, new String[] { "[&FL!A]/////’[&FL!A]///////’[&FL!A]", "S ///// F",
		 * "[’’’^^{-f+f+f-|-f+f+f}]", "F L" }, new float[] { .75f, .75f, 1f,
		 * .75f }), start, Math.toRadians(22.5f));
		 */
		Tuple t =
				new Tuple(
						new TupleConfig(
								new char[] { 'a', 'f', 'l', 's' },
								new String[] {
										"[&FL!A]/////’[&FL!A]///////’[&FL!A]",
										"S ///// F",
										"[’’’^^{----f|3|++++f|3|++++f|3|++++f|3|}]",
										"F L" }, new float[] {
										.75f, .75f, 1f, .75f }),
						start, Math.toRadians(22.5f));

		t.setStartMatrix(new TransMatrix().identity()
				.setYRotation(Math.random() * 2.0 * Math.PI));
		t.setHeading(new Vector3D((float) Math.random() - 0.5f,
				30, (float) Math.random() - 0.5f));
		t.jumpItr(itr);
		t.genTree(base.x, base.y, base.z);
		return t;
	}
}
