package com.pi.math3d;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RenderPipeline implements Transformation {
    private List<Transformation> trans = new ArrayList<Transformation>();
    private Frustrum frustrum = null;

    public void setFrustrum(Frustrum f) {
	this.frustrum = f;
    }

    public void addTransformation(Transformation t) {
	trans.add(t);
    }

    public RenderPipeline() {

    }

    public RenderPipeline(Frustrum f) {
	setFrustrum(f);
    }

    public RenderPipeline(int near, int far, float verticalRadians, int width,
	    int height) {
	setFrustrum(new Frustrum(near, far, verticalRadians, width, height));
    }

    public void setSize(int width, int height) {
	frustrum.setSize(width, height);
    }

    public int getWidth() {
	return frustrum.getWidth();
    }

    public int getHeight() {
	return frustrum.getHeight();
    }

    public Point2D toScreen(Point3D p) {
	return frustrum.toScreen(translate(p));
    }

    public Point2D rawToScreen(Point3D p) {
	return frustrum.toScreen(p);
    }

    @Override
    public Point3D translate(Point3D p) {
	for (Transformation t : trans)
	    t.translate(p);
	return p;
    }

    public Frustrum getFrust() {
	return frustrum;
    }
}
