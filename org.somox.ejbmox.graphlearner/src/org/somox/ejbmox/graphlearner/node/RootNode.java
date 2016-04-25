package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;
import org.somox.ejbmox.graphlearner.Visitor;

public class RootNode extends NestableNode {

	public Node getChild() {
		if (getChildren().isEmpty()) {
			return null;
		}
		return getChildren().get(0);
	}

	@Override
	public NestableNode insertParent(NestableNode newParent) {
		throw new RuntimeException("Cannot change position of root node");
	}

	@Override
	public void insertAsChild(NestableNode newParent) {
		throw new RuntimeException("Cannot change position of root node");
	}

	@Override
	public void insertAfter(Node node) {
		throw new RuntimeException("Cannot change position of root node");
	}

	@Override
	public void insertBefore(Node node) {
		throw new RuntimeException("Cannot change position of root node");
	}

	@Override
	public void setParent(NestableNode parent) {
		throw new RuntimeException("Cannot set parent for root node");
	}

	@Override
	public <R> void accept(Visitor<R> v, R arg) {
		v.visit(this, arg);
	}

	@Override
	public <R> R accept(ReturnOrientedVisitor<R> v) {
		return v.visit(this);
	}

	@Override
	public void insertSeriesSuccessor(Node successor) {
		SeriesNode n = new SeriesNode();
		successor.setParent(n);
		n.insertAsChild(this);
		successor.insertAsChild(n);
	}

	@Override
	public void insertSeriesPredecessor(Node predecessor) {
		SeriesNode n = new SeriesNode();
		predecessor.setParent(n);
		n.insertAsChild(this);
		predecessor.insertAsChild(n);
	}

	@Override
	public void insertParallel(Node parallel) {
		ParallelNode n = new ParallelNode();
		parallel.setParent(n);
		n.insertAsChild(this);
		parallel.insertAsChild(n);
	}

}
