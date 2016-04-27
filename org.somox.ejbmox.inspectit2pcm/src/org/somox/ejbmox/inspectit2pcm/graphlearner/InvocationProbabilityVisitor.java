package org.somox.ejbmox.inspectit2pcm.graphlearner;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

/**
 * Calculates invocation probabilities from invocation counts.
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationProbabilityVisitor implements Visitor<Void> {

	@Override
	public void visit(LeafNode n, Void arg) {
		calculateRelativeInvocationProbability(n);
	}

	@Override
	public void visit(EpsilonLeafNode n, Void arg) {
		calculateRelativeInvocationProbability(n);
	}

	@Override
	public void visit(ParallelNode n, Void arg) {
		calculateRelativeInvocationProbability(n);
		for (Node child : n.getChildren()) {
			child.accept(this, null);
		}
	}

	@Override
	public void visit(SeriesNode n, Void arg) {
		calculateRelativeInvocationProbability(n);
		for (Node child : n.getChildren()) {
			child.accept(this, null);
		}
	}

	@Override
	public void visit(RootNode n, Void arg) {
		n.setAttribute(NodeAttribute.INVOCATION_PROBABILITY, 1.0);
		n.getChild().accept(this, null);
	}

	private void calculateRelativeInvocationProbability(Node n) {
		int parentInvocations = (int) n.getParent().getAttribute(NodeAttribute.INVOCATION_COUNT);
		int invocations = (int) n.getAttribute(NodeAttribute.INVOCATION_COUNT);
		n.setAttribute(NodeAttribute.INVOCATION_PROBABILITY, invocations / (double) parentInvocations);
	}

}
