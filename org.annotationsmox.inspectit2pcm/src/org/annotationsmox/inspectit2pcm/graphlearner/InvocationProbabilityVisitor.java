package org.annotationsmox.inspectit2pcm.graphlearner;

import org.annotationsmox.graphlearner.Visitor;
import org.annotationsmox.graphlearner.node.EpsilonLeafNode;
import org.annotationsmox.graphlearner.node.LeafNode;
import org.annotationsmox.graphlearner.node.Node;
import org.annotationsmox.graphlearner.node.ParallelNode;
import org.annotationsmox.graphlearner.node.RootNode;
import org.annotationsmox.graphlearner.node.SeriesNode;

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
        if (n.getAttribute(NodeAttribute.INVOCATION_COUNT) != null) {
            int invocations = (int) n.getAttribute(NodeAttribute.INVOCATION_COUNT);
            n.setAttribute(NodeAttribute.INVOCATION_PROBABILITY, invocations / (double) parentInvocations);
        } else {
            // TODO log error / warning!
            n.setAttribute(NodeAttribute.INVOCATION_COUNT, 0);
            n.setAttribute(NodeAttribute.INVOCATION_PROBABILITY, 0.0);
        }
    }

}
