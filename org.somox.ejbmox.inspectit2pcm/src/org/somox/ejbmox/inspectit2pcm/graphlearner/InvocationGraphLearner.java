package org.somox.ejbmox.inspectit2pcm.graphlearner;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.PathIntegrationListener;
import org.somox.ejbmox.graphlearner.node.Node;

public class InvocationGraphLearner extends GraphLearner {

    public InvocationGraphLearner() {
        addIntegrationListener(new MaintainInvocationCountNodeAttribute());
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
