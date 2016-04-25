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

	public boolean hasParent() {
		return parent != null;
	}
	
	public void setParent(NestableNode newParent) {
		if (parent == newParent) {
			return;
		}
		// remove this node from parent's children
		if (parent != null) {
			parent.removeChild(this);
		}
		parent = newParent;
		newParent.children.add(this); // don't call add children
	}

	public abstract <R> void accept(Visitor<R> v, R arg);

	public abstract <R> R accept(ReturnOrientedVisitor<R> v);

	public NestableNode insertParent(NestableNode newParent) {
		newParent.setParent(parent);
		if (parent != null) {
			parent.replaceChild(this, newParent);
		}
		parent = newParent;
		setParent(newParent); // TODO
		return newParent;
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
	 * @param position
	 *            0 == before, 1 == after
	 */
	private void insertBeforeOrAfter(Node node, int position) {
		if (node.getParent() == null) {
			throw new IllegalStateException("Cannot insert before/after " + node + " because that node has no parent");
		}
		// remove this node from parent's children
		if (parent != null) {
			parent.removeChild(this);
		}
		NestableNode newParent = node.getParent();
		newParent.addChild(newParent.getChildren().indexOf(node) + position, this);
		parent = newParent;
	}

	public void insertSeriesSuccessor(Node successor) {
		getParent().accept(new EnsureSeriesParent(this));
		successor.insertAfter(this);
	}

	public void insertSeriesPredecessor(Node predecessor) {
		getParent().accept(new EnsureSeriesParent(this));
		predecessor.insertBefore(this);
	}

	public void insertParallel(Node parallel) {
		getParent().accept(new EnsureParallelParent(this));
		parallel.insertAfter(this);
	}

}
