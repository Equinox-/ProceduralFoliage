package com.pi.lsys;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.pi.math3d.Point3D;
import com.pi.math3d.RenderPipeline;

public class Node {
    private Point2D screen;
    private Point3D real;
    private Node parent;
    private List<Node> children;
    private final int depth;

    public Node(Node parent, Point3D real, final int deep) {
	this.real = real;
	this.parent = parent;
	this.depth = deep;
	this.children = new ArrayList<Node>(5);
    }

    public List<Node> getChildren() {
	return children;
    }

    public Point2D getScreenLocation() {
	return screen;
    }

    public Point3D getLocation() {
	return real;
    }

    public void compile(RenderPipeline p) {
	this.screen = p.toScreen(real.clone());
    }

    public Node getParent() {
	return parent;
    }

    public void addChild(Node current) {
	children.add(current);
    }

    public int getDepth(){
	return depth;
    }
}
