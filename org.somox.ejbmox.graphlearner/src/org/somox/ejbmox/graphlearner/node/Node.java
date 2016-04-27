package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;
import org.somox.ejbmox.graphlearner.Visitor;

public abstract class Node {

	private static final int INSERT_BEFORE = 0;
	private static final int INSERT_AFTER = 1;

	protected NestableNode parent;

	private int counter;

	public NestableNode getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public void setParent(NestableNode parent) {
		if (parent == null) {
			parent = null;
		} else {
			parent.addChild(this);
		}
	}

	public abstract <R> void accept(Visitor<R> v, R arg);

	public abstract <R> R accept(ReturnOrientedVisitor<R> v);

	public void insertParent(NestableNode newParent) {
		newParent.setParent(getParent());
		setParent(newParent);
	}

	private void insertAfter(Node node) {
		insertBeforeOrAfter(node, INSERT_AFTER);
	}

	private void insertBefore(Node node) {
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
		NestableNode newParent = node.getParent();
		newParent.addChild(newParent.getChildren().indexOf(node) + position, this);
	}

	private int childIndex() {
		if (parent == null) {
			return -1;
		} else {
			int i = 0;
			for (Node sibling : parent.children) {
				// dont't use equals to avoid stack overflow error!
				if (sibling == this) {
					return i;
				}
				i++;
			}
			throw new RuntimeException("Could not find this node in parent's collection of children");
		}
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

	public void incrementCounter() {
		counter++;
	}

	public int getCounter() {
		return counter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + childIndex();
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (childIndex() != other.childIndex())
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
