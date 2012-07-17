package com.pi.gl.graphics.objects.lsystem;

import java.util.ArrayList;
import java.util.List;

import com.pi.gl.graphics.objects.CylinderTree.Vector3DNode;
import com.pi.gl.graphics.objects.Vector3D;

public class TupleLeaf extends TupleNode {

	private Vector3D normal;

	public TupleLeaf(Tuple tpl, TupleNode parent, int deep,
			String ID, float x, float y, float z) {
		super(tpl, parent, deep, ID, x, y, z);
	}

	@Override
	public List<Vector3DNode> getChildren() {
		return new ArrayList<Vector3DNode>();
	}

	public List<Vector3DNode> getNodes() {
		return children;
	}

	public void calcNormals() {
		if (normal == null && children.size() >= 2) {
			normal = new Vector3D(0, 0, 0);
			for (int i = 0; i < children.size(); i++) {
				normal =
						normal.add(Vector3D
								.crossProduct(
										children.get(i).clone()
												.subtract(this),
										children.get(
												i + 1 >= children
														.size() ? i
														+ 1
														- children
																.size()
														: i)
												.clone()
												.subtract(this))
								.normalize());
			}
			normal =
					normal.multiply(1f / children.size())
							.normalize();
			if (normal.y < 0)
				normal = normal.reverse();
		}
	}

	public Vector3D getNormal() {
		return normal;
	}
}
