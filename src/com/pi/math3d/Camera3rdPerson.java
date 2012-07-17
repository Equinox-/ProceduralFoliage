package com.pi.math3d;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Camera3rdPerson implements Transformation, KeyListener {
    private static final float yawMilli = .1f;
    private static final float pitchMilli = .1f;
    private static final float magMilli = 2.5f;

    private float centerDist;
    private float pitch = 0, yaw = 0;
    private TransformationMatrix trans1 = new TransformationMatrix(),
	    trans2 = new TransformationMatrix();
    private boolean u, l, d, r, limit, i, o;
    private long lastMoveProc = -1;

    public Camera3rdPerson(int centerDist, boolean limit) {
	this.centerDist = centerDist;
	this.limit = limit;
	trans1.setYRotation(Math.toRadians(this.yaw));
	trans2.setXRotation(Math.toRadians(360 - this.pitch));
    }

    public float getPitch() {
	return pitch;
    }

    public void compile() {
	trans1.setYRotation(Math.toRadians(this.yaw));
	trans2.setXRotation(Math.toRadians(360 - this.pitch));
    }

    public boolean doModification() {
	boolean mod = false;
	if (lastMoveProc != -1) {
	    long passed = System.currentTimeMillis() - lastMoveProc;
	    if (u) {
		mod = true;
		modPitch(passed * pitchMilli);
	    } else if (d) {
		mod = true;
		modPitch(-passed * pitchMilli);
	    }
	    if (l) {
		mod = true;
		modYaw(-passed * yawMilli);
	    } else if (r) {
		mod = true;
		modYaw(passed * yawMilli);
	    }
	    if (i) {
		mod = true;
		centerDist -= passed * magMilli;
	    } else if (o) {
		mod = true;
		centerDist += passed * magMilli;
	    }
	    if (mod) {
		trans1.setYRotation(Math.toRadians(this.yaw));
		trans2.setXRotation(Math.toRadians(360 - this.pitch));
	    }
	}
	lastMoveProc = System.currentTimeMillis();
	return mod;
    }

    public float getYaw() {
	return yaw;
    }

    public void modPitch(float val) {
	if (!limit || (pitch + val >= 0 && pitch + val <= 90)) {
	    pitch += val;
	}
    }

    public void modYaw(float val) {
	yaw += val;
	if (yaw < 0)
	    yaw += 360;
	if (yaw >= 360)
	    yaw -= 360;
    }

    public Point3D getLocation() {
	return new Point3D(getCameraX(), getCameraY(), getCameraZ());
    }

    public float getCameraX() {
	double cDist = centerDist * Math.cos(Math.toRadians(360 - pitch));
	return (float) (cDist * Math.cos(Math.toRadians(yaw - 90)));
    }

    public float getCameraY() {
	return (float) (centerDist * Math.sin(Math.toRadians(180 - pitch)));
    }

    public float getCameraZ() {
	double cDist = centerDist * Math.cos(Math.toRadians(360 - pitch));
	return (float) (cDist * Math.sin(Math.toRadians(yaw - 90)));
    }

    @Override
    public Point3D translate(Point3D p) {
	return trans2.translate(trans1.translate(p))
		.translate(0, 0, centerDist);
    }

    @Override
    public String toString() {
	return "3rd Person Camera: Yaw: " + yaw + " Pitch: " + pitch
		+ " Magnitude: " + centerDist;
    }

    @Override
    public void keyPressed(KeyEvent e) {
	if (e.getKeyCode() == KeyEvent.VK_UP) {
	    u = true;
	} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
	    d = true;
	} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	    l = true;
	} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	    r = true;
	} else if (e.getKeyCode() == KeyEvent.VK_Q) {
	    i = true;
	} else if (e.getKeyCode() == KeyEvent.VK_E) {
	    o = true;
	}
    }

    @Override
    public void keyReleased(KeyEvent e) {
	if (e.getKeyCode() == KeyEvent.VK_UP) {
	    u = false;
	} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
	    d = false;
	} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	    l = false;
	} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	    r = false;
	} else if (e.getKeyCode() == KeyEvent.VK_Q) {
	    i = false;
	} else if (e.getKeyCode() == KeyEvent.VK_E) {
	    o = false;
	}
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
