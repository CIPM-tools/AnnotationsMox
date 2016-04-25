package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;

public class EnsureSeriesParent implements ReturnOrientedVisitor<SeriesNode> {

	private Node node;

	public EnsureSeriesParent(Node node) {
		this.node = node;
	}

	@Override
	public SeriesNode visit(SeriesNode currentParent) {
		return currentParent; // return unmodified parent
	}

	@Override
	public SeriesNode visit(ParallelNode currentParent) {
		SeriesNode newParent = new SeriesNode();
		currentParent.replaceChild(node, newParent);
		node.setParent(newParent);
		return newParent;
	}

	@Override
	public SeriesNode visit(RootNode currentParent) {
		throw new RuntimeException("Cannot transform root node");
	}

	@Override
	public SeriesNode visit(LeafNode n) {
		throw new RuntimeException("This visitor should be applied to parent nodes only");
	}

	@Override
	public SeriesNode visit(EpsilonLeafNode n) {
		throw new RuntimeException("This visitor should be applied to parent nodes only");
	}

}