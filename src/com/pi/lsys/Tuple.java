package com.pi.lsys;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;

import com.pi.math3d.Point3D;
import com.pi.math3d.RenderPipeline;

public class Tuple {
    private final TupleConfig cfg;
    private int iteration = 0;
    private String state = "";
    private String start;
    private float segLength = 25;
    private float sigma = 22.5f;
    private Node parent;
    private int maxDepth = 0;
    private int nodes = 0;

    private int minThick = 5;
    private int maxThick = 15;

    private Color maxColor = new Color(50, 35, 25);
    private Color minColor = new Color(50, 50, 25);

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
	return new Color(c1.getRed() + (int) (r), c1.getGreen() + (int) (g),
		c1.getBlue() + (int) (b));
    }

    public Tuple(TupleConfig cfg, String start) {
	this.cfg = cfg;
	this.start = start;
	this.state = start;
    }

    public void update() {
	iteration++;
	state = cfg.apply(state);
	System.out.println(getIterationID() + ":\t" + getIteration());
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

    public int getIterationID() {
	return iteration;
    }

    public String getIteration() {
	return state;
    }

    /*
     * [H' L' U']=[H L U]*R_U
     * 
     * R_U(Z)=| cosA sinA 0 | | -sinA cosA 0 | | 0 0 1 |
     */
    /*
     * [H' L' U']=[H L U]*R_L
     * 
     * R_L(Y)=| cosA 0 -sinA| | 0 1 0| | sinA 0 cosA|
     */
    /*
     * [H' L' U']=[H L U]*R_H R_H(X)=| 1 0 0 | | 0 cosA -sinA | | 0 sinA cosA |
     */

    public void genTree(int sX, int sY, int sZ) {
	nodes = 0;
	LinkedList<Object[]> lStack = new LinkedList<Object[]>();
	Node current = parent = new Node(null, new Point3D(sX, sY, sZ), 0);
	char[] cA = state.toCharArray();
	double hA, lA, uA, nX1, nX2, nY1, nY2, nZ1, nZ2;
	float angleU = 0, angleL = 0, angleH = 0;
	int depth = 0;
	maxDepth = 0;
	for (int i = 0; i < cA.length; i++) {
	    char c = cA[i];
	    switch (c) {
	    case '[':
		lStack.addLast(new Object[] { current, angleU, angleL, angleH,
			depth });
		break;
	    case ']':
		Object[] dat = lStack.removeLast();
		current = (Node) dat[0];
		angleU = (Float) dat[1];
		angleL = (Float) dat[2];
		angleH = (Float) dat[3];
		depth = (Integer) dat[4];
		break;
	    case '+':
		angleU += sigma;
		break;
	    case '-':
		angleU -= sigma;
		break;
	    case '&':
		angleL += sigma;
		break;
	    case '^':
		angleL -= sigma;
		break;
	    case '<':
		angleH += sigma;
		break;
	    case '>':
		angleH -= sigma;
		break;
	    case '|':
		angleU += 180;
		break;
	    case 'f':
		nodes++;
		hA = Math.toRadians(angleH);
		lA = Math.toRadians(angleL);
		uA = Math.toRadians(angleU);

		nX1 = /* 0 * Math.cos(uA) */-segLength * Math.sin(uA);
		nY1 = /* 0 * Math.sin(uA) + */segLength * Math.cos(uA);

		nX2 = nX1 * Math.cos(lA);// + 0 * Math.sin(lA);
		nZ1 = -nX1 * Math.sin(lA);// + 0 * Math.cos(lA);

		nY2 = nY1 * Math.cos(hA) + nZ1 * Math.sin(hA);
		nZ2 = -nY1 * Math.sin(hA) + nZ1 * Math.cos(hA);

		Node nN = new Node(current, new Point3D((float) nX2
			+ current.getLocation().x, (float) nY2
			+ current.getLocation().y, (float) nZ2
			+ current.getLocation().z), depth);
		current.addChild(nN);
		current = nN;
		maxDepth = Math.max(depth, maxDepth);
		depth++;
		break;
	    }
	}
    }

    public void render(Graphics2D g, RenderPipeline p) {
	render(g, p, parent);
	g.setColor(Color.BLACK);
	g.drawString("Node count: " + nodes, 25, 100);
    }

    private void render(Graphics2D g, RenderPipeline p, Node n) {
	n.compile(p);
	if (n.getScreenLocation() != null && n.getParent() != null
		&& n.getParent().getScreenLocation() != null) {
	    float dDirty = (((float) n.getDepth()) / ((float) maxDepth));
	    float depthMapping = (float) (Math
		    .pow(Math.log(dDirty) / -2f, .75f));
	    if (n.getChildren().size() == 0) {
		g.setColor(Color.GREEN);
	    } else {
		g.setColor(mapColor(depthMapping, minColor, maxColor));
	    }
	    g.setStroke(new BasicStroke((depthMapping * depthMapping)
		    * (maxThick - minThick) + minThick));
	    g.drawLine((int) n.getParent().getScreenLocation().getX(), (int) n
		    .getParent().getScreenLocation().getY(), (int) n
		    .getScreenLocation().getX(), (int) n.getScreenLocation()
		    .getY());
	}
	for (Node c : n.getChildren()) {
	    render(g, p, c);
	}
    }

    public static void main(String[] args) {
	Tuple t = new Tuple(new TupleConfig(new char[] { 'f', 'x' },
		new String[] { "FF", "[F-[[X]+X]+F[+FX]-X]" }), "x");
	// Tuple t = new Tuple(new TupleConfig(new char[] { 'f', 'l' },
	// new String[] { "F[-&&<F][<++&&F]||F[--&&>F][+&&F]" }), "f");
	t.jumpItr(3);
	new RenderTuple(t);
    }
}
