package com.pi.gl.gui;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.pi.gl.graphics.DisplayManager;

public class GLFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	DisplayManager mgr;

	public GLFrame() {
		super("JOGL - Beginner");
		setSize(500, 500);
		setLocation(0, 0);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		mgr = new DisplayManager();
		mgr.getGLCanvas().setSize(500, 500);
		mgr.getGLCanvas().setLocation(0, 0);
		add(mgr.getGLCanvas());
		setVisible(true);
		mgr.start();
	}

	@Override
	public void dispose() {
		mgr.dispose();
		super.dispose();
	}

	public static void main(String[] args) {
		new GLFrame();
	}
}
