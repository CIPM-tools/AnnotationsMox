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
 * Constructs a two-terminal series-parallel graph [1] from a set of symbol sequences (e.g. a set of
 * invoked methods) such that the resulting graph contains every sequence.
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
                List<Node> subtreesOriginal = Node.findCompletelyCoveredSubtrees(originalPath.getNodes());
                List<Node> subtreesRevised = Node.findCompletelyCoveredSubtrees(revisedPath.getNodes());
                change(subtreesOriginal, subtreesRevised);
                break;
            case DELETE: {
                diffListeners.forEach(l -> l.delete(originalPath));
                List<Node> subtrees = Node.findCompletelyCoveredSubtrees(originalPath.getNodes());
                delete(subtrees);
                break;
            }
            case INSERT:
                /*
                 * special case because there is no node to the *left* to be used as a reference;
                 * instead we used the source node (to be be confused with the root) as a reference
                 * and insert to the left of the reference node.
                 */
                if (delta.getOriginal().getPosition() == 0) {
                    diffListeners.forEach(l -> l.insertBefore(graph.getSource(), revisedPath));
                    Node reference = graph.getSource();
                    insertToTheLeft(reference, revisedPath.getNodes());
                } else { // regular case
                    Node reference = closestPath.excludeEpsilon().excludeNonLeaves().getNodes()
                            .get(delta.getOriginal().getPosition() - 1);
                    diffListeners.forEach(l -> l.insertAfter(reference, revisedPath));
                    insertToTheRight(reference, revisedPath.getNodes());
                }
                break;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result: " + graph);
        }
    }

    /**
     * 
     * @param nodesOriginal
     *            the nodes to be changed into nodesRevised; may also be the root node of a subtree
     * @param nodesRevised
     *            may also be the root node of a subtree
     */
    private void change(List<Node> nodesOriginal, List<Node> nodesRevised) {
        List<List<Node>> groups = groupBySiblings(nodesOriginal);
        makeParallel(groups.get(0), nodesRevised);
        for (int i = 1; i < groups.size(); i++) {
            delete(groups.get(i));
        }
    }

    private void delete(List<Node> nodes) {
        for (List<Node> nodeGroup : groupBySiblings(nodes)) {
            makeParallel(nodeGroup, Node.asList(createEpsilonNode()));
        }
    }

    private EpsilonLeafNode createEpsilonNode() {
        return new EpsilonLeafNode();
    }

    private EpsilonLeafNode createEpsilonNodeFor(Node node) {
        EpsilonLeafNode epsilonNode = new EpsilonLeafNode();
        epsilonNode.copyAttributesFrom(node);
        return epsilonNode;
    }

    private void insertToTheLeft(Node referenceNode, List<Node> revisedNodes) {
        Node firstInsertNode = revisedNodes.get(0);
        SPGraph.insertSeriesPredecessor(referenceNode, firstInsertNode);
        reorganizationListeners.forEach(l -> l.insertSeriesPredecessor(referenceNode, firstInsertNode));
        Node epsilon = createEpsilonNodeFor(referenceNode);
        SPGraph.insertParallel(firstInsertNode, epsilon);
        reorganizationListeners.forEach(l -> l.insertParallel(firstInsertNode, epsilon));

        makeSeries(revisedNodes);

    }

    private void insertToTheRight(Node nodeBeforeInsert, List<Node> nodes) {
        Node firstInsertNode = nodes.get(0);
        SPGraph.insertSeriesSuccessor(nodeBeforeInsert, firstInsertNode);
        reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(nodeBeforeInsert, firstInsertNode));
        Node epsilon = createEpsilonNodeFor(nodeBeforeInsert);
        SPGraph.insertParallel(firstInsertNode, epsilon);
        reorganizationListeners.forEach(l -> l.insertParallel(firstInsertNode, epsilon));

        makeSeries(nodes);
    }

    /**
     * @return group of nodes so that within each group the contained nodes form a chain with
     *         respect to the predecessor-successor relationship.
     */
    private List<List<Node>> groupBySiblings(List<Node> node) {
        List<List<Node>> siblingGroups = new LinkedList<>();
        Node lastNode = null;
        List<Node> currentGroup = null;
        for (Node currentNode : node) {
            if (lastNode == null) {
                currentGroup = new LinkedList<>();
                siblingGroups.add(currentGroup);
            } else if (!areSiblings(lastNode, currentNode)) {
                currentGroup = new LinkedList<>();
                siblingGroups.add(currentGroup);
            }
            currentGroup.add(currentNode);
            lastNode = currentNode;
        }
        return siblingGroups;
    }

    /**
     * @return true, if both nodes have the same parent node and both nodes are in a direct
     *         successor-predecessor relationship, without any other node in between.
     */
    private boolean areSiblings(Node nodeOne, Node nodeTwo) {
        if (!nodeOne.getParent().equals(nodeTwo.getParent())) {
            return false;
        }
        int idxOne = nodeOne.getParent().getChildren().indexOf(nodeOne);
        int idxTwo = nodeTwo.getParent().getChildren().indexOf(nodeTwo);
        int distance = Math.abs(idxOne - idxTwo);
        return distance == 1;
    }

    private Patch<Node> differences(Path left, Path right) {
        List<Node> leftNodes = left.excludeEpsilon().excludeNonLeaves().getNodes();
        List<Node> rightNodes = right.excludeEpsilon().excludeNonLeaves().getNodes();
        Patch<Node> patch = DiffUtils.diff(leftNodes, rightNodes, new NodeEqualiser());
        return patch;
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
            Node reference = leftSubtrees.get(i - 1);
            Node insert = leftSubtrees.get(i);
            SPGraph.insertSeriesSuccessor(reference, insert);
            reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(reference, insert));
        }
        for (int i = 1; i < rightSubtrees.size(); i++) {
            Node reference = rightSubtrees.get(i - 1);
            Node insert = rightSubtrees.get(i);
            SPGraph.insertSeriesSuccessor(reference, insert);
            reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(reference, insert));
        }
    }

    private void makeSeries(List<Node> nodes) {
        for (int i = 1; i < nodes.size(); i++) {
            Node reference = nodes.get(i - 1);
            Node successor = nodes.get(i);
            SPGraph.insertSeriesSuccessor(reference, successor);
            reorganizationListeners.forEach(l -> l.insertSeriesSuccessor(reference, successor));
        }
    }

    private static class NodeEqualiser implements Equalizer<Node> {
        @Override
        public boolean equals(Node original, Node revised) {
            return (((LeafNode) original).getContent().equals(((LeafNode) revised).getContent()));
        }
    }

}
