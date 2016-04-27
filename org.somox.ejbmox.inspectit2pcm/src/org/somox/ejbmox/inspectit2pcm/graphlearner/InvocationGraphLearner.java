package org.somox.ejbmox.inspectit2pcm.graphlearner;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.node.Node;

public class InvocationGraphLearner extends GraphLearner {

	@Override
	public void integratePath(Path path) {
		super.integratePath(path);
		Path integratedPath = findPathClosestTo(path);
		incrementCounterAlongPath(integratedPath);
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
