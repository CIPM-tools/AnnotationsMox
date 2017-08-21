package org.somox.ejbmox.inspectit2pcm.graphlearner;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.PathIntegrationListener;
import org.somox.ejbmox.graphlearner.ReorganizationListener;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.graphlearner.Sequence;
import org.somox.ejbmox.graphlearner.node.NestableNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;

public class InvocationGraphLearner<T> extends GraphLearner<T> {

    public InvocationGraphLearner() {
        MaintainInvocationCountNodeAttribute m = new MaintainInvocationCountNodeAttribute();
        addIntegrationListener(m);
        addReorganizationListener(m);
    }

    private class MaintainInvocationCountNodeAttribute implements PathIntegrationListener, ReorganizationListener {

        @Override
        public void notifyIntegration(Path originalPath, Sequence<?> addPath, Path combinedPath) {
            incrementCounterAlongPath(combinedPath);
        }

        @Override
        public void insertParallel(Node node, Node parallel) {
            // fix invocation count of parent ParallelNode
            recalculateInvocationCount(node.getParent());
            recalculateInvocationProbabilities();
        }

        @Override
        public void insertSeriesSuccessor(Node node, Node successor) {
            if (successor instanceof ParallelNode) {
                copyInvocationCount(node, successor);
            }
            recalculateInvocationProbabilities();
        }

        @Override
        public void insertSeriesPredecessor(Node node, Node predecessor) {
            recalculateInvocationProbabilities();
        }

        private void incrementCounterAlongPath(Path integratedPath) {
            for (Node node : integratedPath.getNodes()) {
                Integer invocationCount = getInvocationCount(node);
                node.setAttribute(NodeAttribute.INVOCATION_COUNT, ++invocationCount);
            }
        }

        private Integer getInvocationCount(Node node) {
            Integer invocationCount = (Integer) node.getAttribute(NodeAttribute.INVOCATION_COUNT);
            if (invocationCount == null) {
                invocationCount = 0;
            }
            return invocationCount;
        }

        private void setInvocationCount(Node node, Integer count) {
            node.setAttribute(NodeAttribute.INVOCATION_COUNT, count);
        }

        private void recalculateInvocationCount(NestableNode node) {
            int invocationsSum = 0;
            for (Node child : node.getChildren()) {
                invocationsSum += getInvocationCount(child);
            }
            setInvocationCount(node, invocationsSum);
        }

        private void recalculateInvocationProbabilities() {
            SPGraph graph = InvocationGraphLearner.this.getGraph();
            graph.getRoot().accept(new InvocationProbabilityVisitor(), null);
        }

        private void copyInvocationCount(Node from, Node to) {
            Integer invocationCount = getInvocationCount(from);
            to.setAttribute(NodeAttribute.INVOCATION_COUNT, invocationCount);
        }

    }

}
