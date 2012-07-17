package com.pi.gl.graphics.objects.lsystem;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.pi.gl.graphics.objects.CylinderTree.Vector3DNode;

public class TupleNode extends Vector3DNode {
	private TupleNode parent;
	protected List<Vector3DNode> children;
	private final int depth;
	private Tuple tuple;
	private String ID;

	public TupleNode(Tuple tpl, TupleNode parent,
			final int deep, String ID, float x, float y, float z) {
		super(x, y, z);
		this.tuple = tpl;
		this.parent = parent;
		this.depth = deep;
		this.ID = ID;
		this.children = new ArrayList<Vector3DNode>(5);
	}

	@Override
	public List<Vector3DNode> getChildren() {
		return children;
	}

	@Override
	public TupleNode getParent() {
		return parent;
	}

	public void addChild(TupleNode current) {
		children.add(current);
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public String getType() {
		return ID;
	}

	@Override
	public float getRadius() {
		if (getChildren().size() == 0) {
			return tuple.minThick;
		}
		float dDirty =
				(((float) getDepth() + 1f) / ((float) tuple.maxDepth + 1f));
		float depthMapping =
				(float) (Math.pow(Math.log(dDirty) / -2f, .75f));
		return (depthMapping * depthMapping)
				* (tuple.maxThick - tuple.minThick)
				+ tuple.minThick;

	}

	@Override
	public Color getColor() {
		return Color.WHITE;/*
		float dDirty =
				(((float) getDepth()) / ((float) tuple.maxDepth));
		float depthMapping =
				(float) (Math.pow(Math.log(dDirty) / -2f, .75f));
		return Tuple.mapColor(depthMapping, tuple.minColor,
				tuple.maxColor);*/
	}
}
