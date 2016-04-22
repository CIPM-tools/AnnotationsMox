package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;
import org.somox.ejbmox.graphlearner.Visitor;

public abstract class Node {

	private static final int INSERT_BEFORE = 0;
	private static final int INSERT_AFTER = 1;
	
	protected NestableNode parent;

	public NestableNode getParent() {
		return parent;
	}

	public void setParent(NestableNode parent) {
		this.parent = parent;
	}

	public abstract <R> void accept(Visitor<R> v, R arg);

	public abstract <R> R accept(ReturnOrientedVisitor<R> v);

	public NestableNode insertParent(NestableNode newParent) {
		newParent.setParent(parent);
		if (parent != null) {
			parent.replaceChildren(this, newParent);
		}
		parent = newParent;
		insertAsChild(newParent); // TODO
		return newParent;
	}

	public void insertAsChild(NestableNode newParent) {
		// remove this node from parent's children
		if (parent != null) {
			parent.getChildren().remove(this);
		}
		newParent.getChildren().add(this);
		parent = newParent;
	}

	public void insertAfter(Node node) {
		insertBeforeOrAfter(node, INSERT_AFTER);
	}

	public void insertBefore(Node node) {
		insertBeforeOrAfter(node, INSERT_BEFORE);
	}
	
	/**
	 * 
	 * @param node
	 * @param position 0 == before, 1 == after
	 */
	public void insertBeforeOrAfter(Node node, int position) {
		if (node.getParent() == null) {
			throw new IllegalStateException("Cannot insert before/after " + node + " because that node has no parent");
		}
		// remove this node from parent's children
		if (parent != null) {
			parent.getChildren().remove(this);
		}
		NestableNode newParent = node.getParent();
		newParent.getChildren().add(newParent.getChildren().indexOf(node) + position, this);
		parent = newParent;
	}

}
