package com.pi.gl.graphics;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;

public class Camera1stPerson extends Camera {
	private static final double strafeMilli = 1;
	private static final double moveMilli = 1;

	private long lastMove = -1;
	private double xpos = 0, ypos = 300, zpos = 0;
	private double xrot = 0, yrot = 0, xrotrad = 0, yrotrad = 0;
	private boolean q, z, w, s, a, d, esc;

	private int lastX = -1, lastY = -1;

	private final Component c;
	private Robot robo;

	public Camera1stPerson(Component c) {
		this.c = c;
		try {
			this.robo = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
			this.robo = null;
		}
	}

	@Override
	public void translate(GL2 gl) {
		doMovement();
		gl.glRotatef((float) xrot, 1, 0, 0);
		gl.glRotatef((float) yrot, 0, 1, 0);
		gl.glTranslated(-xpos, -ypos, -zpos);
	}

	private void doMovement() {
		if (lastMove != -1) {
			double passed =
					System.currentTimeMillis() - lastMove;
			double strafed = strafeMilli * passed;
			double moved = moveMilli * passed;
			if (q) {
				xrot += 1;
				if (xrot > 360)
					xrot -= 360;
			}

			if (z) {
				xrot -= 1;
				if (xrot < -360)
					xrot += 360;
			}

			if (w) {
				yrotrad = Math.toRadians(yrot);
				xrotrad = Math.toRadians(xrot);
				xpos += Math.sin(yrotrad) * moved;
				zpos -= Math.cos(yrotrad) * moved;
				ypos -= Math.sin(xrotrad) * moved;
			}

			if (s) {
				yrotrad = Math.toRadians(yrot);
				xrotrad = Math.toRadians(xrot);
				xpos -= Math.sin(yrotrad) * moved;
				zpos += Math.cos(yrotrad) * moved;
				ypos += Math.sin(xrotrad) * moved;
			}

			if (d) {
				yrotrad = Math.toRadians(yrot);
				xpos += Math.cos(yrotrad) * strafed;
				zpos += Math.sin(yrotrad) * strafed;
			}

			if (a) {
				yrotrad = Math.toRadians(yrot);
				xpos -= Math.cos(yrotrad) * strafed;
				zpos -= Math.sin(yrotrad) * strafed;
			}
		}
		lastMove = System.currentTimeMillis();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_Q)
			q = true;
		else if (k == KeyEvent.VK_Z)
			z = true;
		else if (k == KeyEvent.VK_W)
			w = true;
		else if (k == KeyEvent.VK_S)
			s = true;
		else if (k == KeyEvent.VK_A)
			a = true;
		else if (k == KeyEvent.VK_D)
			d = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_Q)
			q = false;
		else if (k == KeyEvent.VK_Z)
			z = false;
		else if (k == KeyEvent.VK_W)
			w = false;
		else if (k == KeyEvent.VK_S)
			s = false;
		else if (k == KeyEvent.VK_A)
			a = false;
		else if (k == KeyEvent.VK_D)
			d = false;
		else if (k == KeyEvent.VK_ESCAPE)
			esc = !esc;
	}

	boolean robot = false;

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!esc) {
			if (e.getX() != c.getWidth() / 2
					|| e.getY() != c.getHeight() / 2) {
				int diffx = e.getX() - lastX;
				int diffy = e.getY() - lastY;
				lastX = e.getX();
				lastY = e.getY();
				xrot += (float) diffy / 10f;
				yrot += (float) diffx / 2f;
				if (robo != null) {
					robot = true;
					Point p = c.getLocationOnScreen();
					robo.mouseMove(p.x + c.getWidth() / 2, p.y
							+ c.getHeight() / 2);
					lastX = c.getWidth() / 2;
					lastY = c.getHeight() / 2;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "1st Person Cam: [" + xpos + "," + ypos + ","
				+ zpos + "] xRotation: " + xrot + " yRotation: "
				+ yrot;
	}
}
