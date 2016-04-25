package org.somox.ejbmox.graphlearner.visitor;

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
			n.insertParent(new SeriesNode());
		} else if (n.getParent() != null && n.getParent() instanceof ParallelNode) {
			n.insertParent(new SeriesNode());
		}
	}

	@Override
	public void visit(EpsilonLeafNode n, Void arg) {
		if (n.getParent() != null && n.getParent() instanceof ParallelNode) {
			n.insertParent(new SeriesNode());
		}
	}

	@Override
	public void visit(ParallelNode n, Void arg) {
		for (Node child : n.getChildren()) {
			child.accept(this, arg);
		}
	}

	@Override
	public void visit(SeriesNode n, Void arg) {
		for (Node child : n.getChildren()) {
			child.accept(this, arg);
		}
	}

	@Override
	public void visit(RootNode n, Void arg) {
		n.getChild().accept(this, arg);
	}

}
