package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

public abstract class Node<T> {

	private int count;

	private Node<T> predecessor;

	private Node<T> successor;

	public Node() {
		this(1);
	}

	public Node(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public Node<T> getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(Node<T> predecessor) {
		this.predecessor = predecessor;
	}

	public void clearPredecessor() {
		predecessor = null;
	}

	public Node<T> getSuccessor() {
		return successor;
	}

	public void setSuccessor(Node<T> successor) {
		this.successor = successor;
	}

	public void clearSuccessor() {
		successor = null;
	}

	public boolean isBranch() {
		return false;
	}

}
