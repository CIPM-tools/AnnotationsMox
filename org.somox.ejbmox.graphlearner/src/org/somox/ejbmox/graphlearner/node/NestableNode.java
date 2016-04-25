package org.somox.ejbmox.graphlearner.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NestableNode extends Node {

	protected List<Node> children;

	public NestableNode() {
		children = new ArrayList<>();
	}

	public void addChild(Node node) {
		addChild(children.size(), node);
	}

	public void addChild(int index, Node node) {
		if (children.contains(node)) {
			throw new IllegalStateException("Cannot add node because it is already a child: " + node);
		}

		// remove node from current parent's children
		if (node.parent != null && node.parent != this) {
			node.parent.children.remove(node);
		}
		children.add(index, node);

		// set node's new parent
		node.parent = this; // don't call node.setParent(...)
	}

	public void removeChild(Node node) {
		if (!children.contains(node)) {
			throw new IllegalStateException("Cannot remove node because it is no child: " + node);
		}

		children.remove(node);
		node.parent = null;
	}

	protected void replaceChild(Node child, Node substitute) {
		children.set(children.indexOf(child), substitute);
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public String toString() {
		return children.toString();
	}

}
