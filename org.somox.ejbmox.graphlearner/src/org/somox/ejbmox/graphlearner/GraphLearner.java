package org.somox.ejbmox.graphlearner;

import java.util.LinkedList;
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
            integrate(closestPath, path);
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

    public boolean contains(Path path) {
        Path closestPath = findPathClosestTo(path);
        Patch<Node> patch = differences(closestPath, path);
        return patch.getDeltas().isEmpty();
    }

    // TODO simplify and get rid of duplicated code
    protected void integrate(Path closestPath, Path path) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Integrating " + path + " into path " + closestPath);
        }

        Patch<Node> patch = differences(closestPath, path);
        for (Delta<Node> delta : patch.getDeltas()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delta: " + delta);
            }
            Path originalPath = Path.fromNodes(delta.getOriginal().getLines());
            Path revisedPath = Path.fromNodes(delta.getRevised().getLines());
            switch (delta.getType()) {
            case CHANGE:
                diffListeners.forEach(l -> l.change(originalPath, revisedPath));
                List<Node> subtreesLeft = Node.findSubtrees(originalPath.getNodes());
                List<Node> subtreesRight = Node.findSubtrees(revisedPath.getNodes());
                makeParallel(subtreesLeft, subtreesRight);
                break;
            case DELETE: {
                diffListeners.forEach(l -> l.delete(originalPath));
                List<Node> subtrees = Node.findSubtrees(originalPath.getNodes());
                Node epsilon = new EpsilonLeafNode();
                makeParallel(subtrees, epsilon);
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
                    Node nodeBeforeInsert = closestPath.excludeEpsilon().excludeNonLeaves().getNodes()
                            .get(delta.getOriginal().getPosition() - 1);
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

    private Patch<Node> differences(Path left, Path right) {
        List<Node> leftNodes = left.excludeEpsilon().excludeNonLeaves().getNodes();
        List<Node> rightNodes = right.excludeEpsilon().excludeNonLeaves().getNodes();
        Patch<Node> patch = DiffUtils.diff(leftNodes, rightNodes, new NodeEqualiser());
        return patch;
    }

    private void makeParallel(List<Node> leftSubtrees, Node rightNode) {
        List<Node> list = new LinkedList<>();
        list.add(rightNode);
        makeParallel(leftSubtrees, list);
    }

    /**
     * 
     * @param leftSubtrees
     *            subtree represented by its subtree root
     * @param rightSubtrees
     *            other subtree represented by its subtree root
     */
    private void makeParallel(List<Node> leftSubtrees, List<Node> rightSubtrees) {
        SPGraph.insertParallel(leftSubtrees.get(0), rightSubtrees.get(0));
        reorganizationListeners.forEach(l -> l.insertParallel(leftSubtrees.get(0), rightSubtrees.get(0)));
        for (int i = 1; i < leftSubtrees.size(); i++) {
            Node anchor = leftSubtrees.get(i - 1);
            Node insert = leftSubtrees.get(i);
            SPGraph.insertSeriesSuccessor(anchor, insert);
            reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insert));
        }
        for (int i = 1; i < rightSubtrees.size(); i++) {
            Node anchor = rightSubtrees.get(i - 1);
            Node insert = rightSubtrees.get(i);
            SPGraph.insertSeriesSuccessor(anchor, insert);
            reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(anchor, insert));
        }
    }

    private static class NodeEqualiser implements Equalizer<Node> {
        @Override
        public boolean equals(Node original, Node revised) {
            return (((LeafNode) original).getContent().equals(((LeafNode) revised).getContent()));
        }
    }

}
