package org.somox.ejbmox.graphlearner;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.Equalizer;

/**
 * Constructs a series-parallel graph [1] from a path set (e.g. traces) such that the resulting
 * graph contains every of these paths.
 * 
 * [1] http://www.graphclasses.org/classes/gc_875.html
 * 
 * @author Philipp Merkle
 *
 */
public class GraphLearner {

    private static final Logger LOG = Logger.getLogger(GraphLearner.class);

    private SPGraph graph;

    private List<PathIntegrationListener> integrationListeners = new CopyOnWriteArrayList<>();

    private List<DiffListener> diffListeners = new CopyOnWriteArrayList<>();

    private List<ReorganizationListener> reorganizationListeners = new CopyOnWriteArrayList<>();

    public void integratePath(Path path) {
        if (graph == null) {
            graph = SPGraph.fromPath(path);
            Path closestPath = findPathClosestTo(path);
            notifyIntegrationListeners(null, path, closestPath);
        } else {
            Path closestPath = findPathClosestTo(path);
            integrationListeners.forEach(l -> l.notifyClosestPath(closestPath));
            integrate(closestPath.excludeNonLeaves().excludeEpsilon(), path.excludeNonLeaves().excludeEpsilon());
            notifyIntegrationListeners(closestPath, path, findPathClosestTo(path));
        }
    }

    public void addIntegrationListener(PathIntegrationListener l) {
        integrationListeners.add(l);
    }

    public void removeIntegrationListener(PathIntegrationListener l) {
        integrationListeners.remove(l);
    }

    public void addReorganizationListener(ReorganizationListener l) {
        reorganizationListeners.add(l);
    }

    public void removeReorganizationListener(ReorganizationListener l) {
        reorganizationListeners.remove(l);
    }

    public void addDiffListener(DiffListener l) {
        diffListeners.add(l);
    }

    public void removeDiffListener(DiffListener l) {
        diffListeners.remove(l);
    }

    /**
     * @param originalPath
     *            the unmodified path before integration of {@code addPath}; {@code null}, if a path
     *            is integrated into an empty {@link SPGraph}.
     * @param addPath
     *            the path to be integrated into {@code originalPath}
     * @param combinedPath
     *            the path resulting from integrating {@code addPath} into {@code originalPath}
     * 
     */
    private void notifyIntegrationListeners(Path originalPath, Path addPath, Path combinedPath) {
        integrationListeners.forEach(l -> l.notifyIntegration(originalPath, addPath, combinedPath));
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
    public Path findPathClosestTo(Path path) {
        List<Path> paths = graph.allPaths();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Collected paths: " + paths);
        }
        int minCost = Integer.MAX_VALUE;
        Path minPath = null;
        for (Path p : paths) {
            Patch<Node> patch = DiffUtils.diff(p.excludeNonLeaves().excludeEpsilon().getNodes(),
                    path.excludeNonLeaves().excludeEpsilon().getNodes(), new NodeEqualiser());
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Integrating " + path + " into path " + closestPath);
        }

        Patch<Node> patch = DiffUtils.diff(closestPath.getNodes(), path.getNodes(), new NodeEqualiser());
        for (Delta<Node> delta : patch.getDeltas()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delta: " + delta);
            }
            Path originalPath = Path.fromNodes(delta.getOriginal().getLines());
            Path revisedPath = Path.fromNodes(delta.getRevised().getLines());
            switch (delta.getType()) {
            case CHANGE:
                diffListeners.forEach(l -> l.change(originalPath, revisedPath));
                if (haveSameParent(originalPath)) { // TODO actually needed!?
                    SPGraph.insertParallel(originalPath.first(), revisedPath.first());
                    reorganizationListeners.forEach(l -> l.insertParallel(originalPath.first(), revisedPath.first()));
                    Node lastNode = originalPath.first();
                    for (Node insertNode : originalPath.subPathStartingAt(1).getNodes()) {
                        Node anchor = lastNode;
                        SPGraph.insertSeriesSuccessor(anchor, insertNode);
                        reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insertNode));
                        lastNode = insertNode;
                    }

                    lastNode = revisedPath.first();
                    for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
                        Node anchor = lastNode;
                        SPGraph.insertSeriesSuccessor(anchor, insertNode);
                        reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insertNode));
                        lastNode = insertNode;
                    }
                } else {
                    // TODO implement? not sure, if different parents can happen
                    throw new UnsupportedOperationException();
                }
                break;
            case DELETE: {
                diffListeners.forEach(l -> l.delete(originalPath));
                Node firstDeleteNode = originalPath.first();
                Node epsilon = new EpsilonLeafNode();
                SPGraph.insertParallel(firstDeleteNode, epsilon);
                reorganizationListeners.forEach(l -> l.insertParallel(firstDeleteNode, epsilon));
                Node lastNode = firstDeleteNode;
                for (Node insertNode : originalPath.subPathStartingAt(1).getNodes()) {
                    Node anchor = lastNode;
                    SPGraph.insertSeriesSuccessor(anchor, insertNode);
                    reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insertNode));
                    lastNode = insertNode;
                }
                break;
            }
            case INSERT:
                if (delta.getOriginal().getPosition() == 0) { // head --> insert after root
                    // inserting optional node at head
                    Node firstInsertNode = revisedPath.first();
                    diffListeners.forEach(l -> l.insert(originalPath, graph.getSource()));
                    SPGraph.insertSeriesPredecessor(graph.getSource(), firstInsertNode);
                    reorganizationListeners.forEach(l -> l.insertSeriesPredecessor(graph.getSource(), firstInsertNode));
                    Node epsilon = new EpsilonLeafNode();
                    SPGraph.insertParallel(firstInsertNode, epsilon);
                    reorganizationListeners.forEach(l -> l.insertParallel(firstInsertNode, epsilon));
                    Node lastNode = firstInsertNode;
                    for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
                        Node anchor = lastNode;
                        SPGraph.insertSeriesSuccessor(anchor, insertNode);
                        reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insertNode));
                        lastNode = insertNode;
                    }
                } else {
                    Node firstInsertNode = revisedPath.first();
                    Node nodeBeforeInsert = closestPath.getNodes().get(delta.getOriginal().getPosition() - 1);
                    diffListeners.forEach(l -> l.insert(originalPath, nodeBeforeInsert));
                    SPGraph.insertSeriesSuccessor(nodeBeforeInsert, firstInsertNode);
                    reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(nodeBeforeInsert, firstInsertNode));
                    Node epsilon = new EpsilonLeafNode();
                    SPGraph.insertParallel(firstInsertNode, epsilon);
                    reorganizationListeners.forEach(l -> l.insertParallel(firstInsertNode, epsilon));
                    Node lastNode = firstInsertNode;
                    for (Node insertNode : revisedPath.subPathStartingAt(1).getNodes()) {
                        Node anchor = lastNode;
                        SPGraph.insertSeriesSuccessor(anchor, insertNode);
                        reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insertNode));
                        lastNode = insertNode;
                    }
                }
                break;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result: " + graph);
        }
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
