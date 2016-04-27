package org.somox.ejbmox.graphlearner;

import java.util.List;

import org.apache.log4j.Logger;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.Equalizer;

/**
 * Constructs a series-parallel graph [1] from a path set (e.g. traces) such
 * that the resulting graph contains every of these paths.
 * 
 * [1] http://www.graphclasses.org/classes/gc_875.html
 * 
 * @author Philipp Merkle
 *
 */
public class GraphLearner {

	private static final Logger LOG = Logger.getLogger(GraphLearner.class);

	private SPGraph graph;

	public void integratePath(Path path) {
		if (graph == null) {
			graph = SPGraph.fromPath(path);
		} else {
			Path closestPath = findPathClosestTo(path);
			integrate(closestPath.excludeEpsilon(), path.excludeEpsilon());
		}
	}

	public SPGraph getGraph() {
		return graph;
	}

	/**
	 * Finds and returns the path closest to the specified path.
	 * 
	 * @param path
	 * @return
	 */
	protected Path findPathClosestTo(Path path) {
		List<Path> paths = graph.allPaths(true);
		LOG.debug("Collected paths: " + paths);
		int minCost = Integer.MAX_VALUE;
		Path minPath = null;
		for (Path p : paths) {
			Patch<Node> patch = DiffUtils.diff(p.excludeEpsilon().getNodes(), path.excludeEpsilon().getNodes(),
					new NodeEqualiser());
			int cost = cost(patch);
			if (cost < minCost) {
				minCost = cost;
				minPath = p;
			}
		}
		return minPath;
	}

	// TODO cost calculation could be improved
	protected int cost(Patch<Node> patch) {
		int cost = 0;
		for (Delta<Node> d : patch.getDeltas()) {
			cost += Math.max(d.getOriginal().size(), d.getRevised().size());
		}
		return cost;
	}

	// TODO simplify and get rid of duplicated code
	protected void integrate(Path closestPath, Path path) {
		LOG.debug("Integrating " + path + " into path " + closestPath);

		Patch<Node> patch = DiffUtils.diff(closestPath.getNodes(), path.getNodes(), new NodeEqualiser());
		for (Delta<Node> delta : patch.getDeltas()) {
			LOG.debug("Delta: " + delta);
			Path originalPath = Path.fromNodes(delta.getOriginal().getLines());
			Path revisedPath = Path.fromNodes(delta.getRevised().getLines());
			switch (delta.getType()) {
			case CHANGE:
				if (haveSameParent(originalPath)) { // TODO actually needed!?
					SPGraph.insertParallel(originalPath.first(), revisedPath.first());
					Node lastNode = originalPath.first();
					for (Node insertNode : originalPath.subPathStartingAt(1).getNodes()) {
						SPGraph.insertSeriesSuccessor(lastNode, insertNode);
						lastNode = insertNode;
					}

					lastNode = revisedPath.first();
					for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
						SPGraph.insertSeriesSuccessor(lastNode, insertNode);
						lastNode = insertNode;
					}
				} else {
					// TODO implement? not sure, if different parents can happen
					throw new UnsupportedOperationException();
				}
				break;
			case DELETE: {
				Node firstDeleteNode = originalPath.first();
				SPGraph.insertParallel(firstDeleteNode, new EpsilonLeafNode());
				Node lastNode = firstDeleteNode;
				for (Node insertNode : originalPath.subPathStartingAt(1).getNodes()) {
					SPGraph.insertSeriesSuccessor(lastNode, insertNode);
					lastNode = insertNode;
				}
				break;
			}
			case INSERT:
				if (delta.getOriginal().getPosition() == 0) { // head
					// inserting optional node at head
					Node firstInsertNode = revisedPath.first();
					SPGraph.insertSeriesPredecessor(graph.getSource(), firstInsertNode);
					SPGraph.insertParallel(firstInsertNode, new EpsilonLeafNode());
					Node lastNode = firstInsertNode;
					for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
						SPGraph.insertSeriesSuccessor(lastNode, insertNode);
						lastNode = insertNode;
					}
				} else {
					Node firstInsertNode = revisedPath.first();
					Node nodeBeforeInsert = closestPath.getNodes().get(delta.getOriginal().getPosition() - 1);
					SPGraph.insertSeriesSuccessor(nodeBeforeInsert, firstInsertNode);
					SPGraph.insertParallel(firstInsertNode, new EpsilonLeafNode());
					Node lastNode = firstInsertNode;
					for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
						SPGraph.insertSeriesSuccessor(lastNode, insertNode);
						lastNode = insertNode;
					}
				}
				break;
			}
		}
		LOG.debug("Result: " + graph);
	}

	private boolean haveSameParent(Path path) {
		Node lastNode = null;
		for (Node node : path.getNodes()) {
			if (lastNode != null) {
				if (!node.getParent().equals(lastNode.getParent())) {
					return false;
				}
			}
			lastNode = node;
		}
		return true;
	}

	private static class NodeEqualiser implements Equalizer<Node> {
		@Override
		public boolean equals(Node original, Node revised) {
			return (((LeafNode) original).getContent().equals(((LeafNode) revised).getContent()));
		}
	}

}
