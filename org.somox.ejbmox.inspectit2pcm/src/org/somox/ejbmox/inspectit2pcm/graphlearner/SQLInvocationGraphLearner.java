package org.somox.ejbmox.inspectit2pcm.graphlearner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.PathIntegrationListener;
import org.somox.ejbmox.graphlearner.Sequence;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;

public class SQLInvocationGraphLearner extends InvocationGraphLearner<SQLStatement> {

    public SQLInvocationGraphLearner() {
        addIntegrationListener(new MaintainDurationsNodeAttribute());
    }

    private static class MaintainDurationsNodeAttribute implements PathIntegrationListener {

        @Override
        public void notifyIntegration(Path originalPath, Sequence<?> addedSequence, Path integratedPath) {
            // List<Node> addNodes = addPath.excludeNonLeaves().excludeEpsilon().getNodes();
            List<Node> combinedNodes = integratedPath.excludeNonLeaves().excludeEpsilon().getNodes();

            // assert a central assumption underlying this method's implementation
            ensureSameSize(addedSequence, combinedNodes);

            Iterator<?> addIterator = addedSequence.iterator();
            Iterator<Node> combinedIterator = combinedNodes.iterator();
            while (addIterator.hasNext()) {
                Object element = addIterator.next();
                LeafNode combinedNode = (LeafNode) combinedIterator.next();

                // check for another possible programming error
                if (!element.equals(combinedNode.getContent())) {
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
                double addDuration = ((SQLStatement) element).getDuration();
                durations.add(addDuration);
            }
        }

        private void ensureSameSize(Sequence<?> sequence, List<Node> nodes) {
            if (sequence.size() != nodes.size()) {
                // fail fast, if the assumption stated above is violated; we need to reconsider
                // this method's implementation then
                throw new IllegalStateException(
                        "Unexpected mismatch between number of added nodes and number of combined nodes");
            }
        }

    }

}
