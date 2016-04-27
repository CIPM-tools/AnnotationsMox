package org.somox.ejbmox.graphlearner.visitor;

import java.util.ArrayList;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class VerboseGraphVisitor implements Visitor<Void> {

	@Override
	public void visit(LeafNode n, Void arg) {
		if (n.getParent() == null) { // leaf node is root
			insertSeriesParent(n);
		} else if (n.getParent() != null && n.getParent() instanceof ParallelNode) {
			insertSeriesParent(n);
		}
	}

	@Override
	public void visit(EpsilonLeafNode n, Void arg) {
		if (n.getParent() != null && n.getParent() instanceof ParallelNode) {
			insertSeriesParent(n);
		}
	}

	@Override
	public void visit(ParallelNode n, Void arg) {
		// use new ArrayList to avoid concurrent modification exception
		for (Node child : new ArrayList<>(n.getChildren())) {
			child.accept(this, arg);
		}
	}

	@Override
	public void visit(SeriesNode n, Void arg) {
		// use new ArrayList to avoid concurrent modification exception
		for (Node child : new ArrayList<>(n.getChildren())) {
			child.accept(this, arg);
		}
	}

	@Override
	public void visit(RootNode n, Void arg) {
		n.getChild().accept(this, arg);
	}

	private void insertSeriesParent(Node node) {
		SeriesNode seriesParent = new SeriesNode();
		seriesParent.copyAttributesFrom(node);
		node.insertParent(seriesParent);
	}

}
