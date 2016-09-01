package org.somox.ejbmox.inspectit2pcm.graphlearner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.PathIntegrationListener;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;

public class InvocationGraphLearner extends GraphLearner {

    public InvocationGraphLearner() {
        addIntegrationListener(new MaintainInvocationCountNodeAttribute());
        addIntegrationListener(new MaintainDurationTotalNodeAttribute());
    }

    private static class MaintainDurationTotalNodeAttribute implements PathIntegrationListener {

        @Override
        public void notifyIntegration(Path originalPath, Path addPath, Path combinedPath) {
            List<Node> addNodes = addPath.excludeNonLeaves().excludeEpsilon().getNodes();
            List<Node> combinedNodes = combinedPath.excludeNonLeaves().excludeEpsilon().getNodes();

            // assert a central assumption underlying this method's implementation
            ensureSameSize(addNodes, combinedNodes);

            Iterator<Node> addIterator = addNodes.iterator();
            Iterator<Node> combinedIterator = combinedNodes.iterator();
            while (addIterator.hasNext()) {
                LeafNode addNode = (LeafNode) addIterator.next();
                LeafNode combinedNode = (LeafNode) combinedIterator.next();

                // check for possible programming error
                if (!(addNode.getContent() instanceof SQLStatement)
                        || !(combinedNode.getContent() instanceof SQLStatement)) {
                    throw new IllegalStateException("Unexpected content type of node.");
                }

                // check for another possible programming error
                if (!addNode.getContent().equals(combinedNode.getContent())) {
                    throw new IllegalStateException("Node mismatch");
                }

                List<Double> durations = (List<Double>) combinedNode.getAttribute(NodeAttribute.DURATIONS);
                if (durations == null) {
                    durations = new ArrayList<>();
                    combinedNode.setAttribute(NodeAttribute.DURATIONS, durations);
                }
                // the use of (inclusive) duration should make no difference to using exclusive
                // duration because SQL statements don't have child invocations, so that
                // inclusive == exclusive
                double addDuration = ((SQLStatement) addNode.getContent()).getDuration();
                durations.add(addDuration);
            }
        }

        private void ensureSameSize(List<Node> addNodes, List<Node> combinedNodes) {
            if (addNodes.size() != combinedNodes.size()) {
                // fail fast, if the assumption stated above is violated; we need to reconsider
                // this method's implementation then
                throw new IllegalStateException(
                        "Unexpected mismatch between number of added nodes and number of combined nodes");
            }
        }

    }

    private static class MaintainInvocationCountNodeAttribute implements PathIntegrationListener {

        @Override
        public void notifyIntegration(Path originalPath, Path addPath, Path combinedPath) {
            incrementCounterAlongPath(combinedPath);
        }

        private void incrementCounterAlongPath(Path integratedPath) {
            for (Node node : integratedPath.getNodes()) {
                Integer invocationCount = (Integer) node.getAttribute(NodeAttribute.INVOCATION_COUNT);
                if (invocationCount == null) {
                    invocationCount = 0;
                }
                node.setAttribute(NodeAttribute.INVOCATION_COUNT, ++invocationCount);
            }
        }

    }

}
