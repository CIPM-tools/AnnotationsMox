package org.somox.ejbmox.graphlearner.node;

import java.util.ArrayList;
import java.util.List;

public abstract class NestableNode extends Node {

	protected List<Node> children;

	public NestableNode() {
		children = new ArrayList<>();
	}
	
	protected void replaceChildren(Node child, Node substitute) {
		children.set(children.indexOf(child), substitute);
	}
	
	public List<Node> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return children.toString();
	}
	
}
