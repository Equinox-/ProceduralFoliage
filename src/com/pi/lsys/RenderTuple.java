package com.pi.lsys;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import com.pi.gl.graphics.objects.Vector3D;
import com.pi.math3d.Camera3rdPerson;
import com.pi.math3d.Point3D;
import com.pi.math3d.RenderPipeline;
import com.pi.math3d.VectorUtil;

public class RenderTuple extends JFrame {
    private static final long serialVersionUID = 1L;
    Tuple t;
    boolean render = false;

    RenderPipeline frust = new RenderPipeline(-100000, 100000,
	    (float) (Math.PI / 4), 1000, 1000);
    Camera3rdPerson camera;

    public RenderTuple(Tuple tT) {
	super("Tuple!");
	camera = new Camera3rdPerson(200, false);
	frust.addTransformation(camera);
	addKeyListener(camera);
	this.t = tT;
	setSize(1000, 1000);
	setLocation(0, 0);
	setVisible(true);
	setIgnoreRepaint(true);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (!render) {
		    render = true;
		    t.update();
		    t.genTree(0, 0, 0);
		    render();
		    render = false;
		}
	    }
	});
	t.genTree(0, 0, 0);
	render();
	BufferedReader read = new BufferedReader(new InputStreamReader(
		System.in));
	while (isVisible()) {
	    if (camera.doModification()) {
		render();
	    }
	    try {
		if (read.ready()) {
		    String[] in = read.readLine().split(" ");
		    if (in[0].equalsIgnoreCase("jump")) {
			render = true;
			t.jumpItr(Integer.valueOf(in[1]));
			t.genTree(0, 0, 0);
			render();
			render = false;
		    }
		}
	    } catch (Exception e1) {
		e1.printStackTrace();
	    }
	}
    }

    public void render() {
	Graphics2D g = (Graphics2D) getGraphics();
	g.clearRect(0, 0, getWidth(), getHeight());
	g.drawString("Cam: " + camera.toString(), 50, 75);
	t.render(g, frust);
    }
}
