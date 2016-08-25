package org.somox.ejbmox.graphlearner;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.visitor.AllPathsVisitor;
import org.somox.ejbmox.graphlearner.visitor.DepthFirstVisitor;
import org.somox.ejbmox.graphlearner.visitor.StringifyVisitor;
import org.somox.ejbmox.graphlearner.visitor.VerboseGraphVisitor;

/**
 * A series-parallel graph as defined by [1].
 * 
 * [1] http://www.graphclasses.org/classes/gc_875.html
 * 
 * @author Philipp Merkle
 *
 */
public class SPGraph {

    private RootNode root;

    public SPGraph() {
        this.root = new RootNode();
    }

    public static SPGraph fromPath(Path path) {
        if (path.isEmpty()) {
            throw new RuntimeException("Path may not be empty");
        }
        SPGraph graph = new SPGraph();
        Node lastNode = graph.root;
        for (Node node : path.getNodes()) {
            lastNode.insertSeriesSuccessor(node);
            lastNode = node;
        }
        return graph;
    }

    public RootNode getRoot() {
        return root;
    }

    public Node getSource() {
        return allNodesDepthFirst().get(0);
    }

    public Node getSink() {
        List<Node> nodes = allNodesDepthFirst();
        // return last node
        return nodes.get(nodes.size() - 1);
    }

    public void traverse(Visitor<Void> visitor) {
        getRoot().accept(visitor, null);
    }

    public <R> void traverse(Visitor<R> visitor, R arg) {
        getRoot().accept(visitor, arg);
    }

    public List<Path> allPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(Path.emptyPath());
        getRoot().accept(new AllPathsVisitor(), paths);
        return paths;
    }

    public List<Node> allNodesDepthFirst() {
        DepthFirstVisitor visitor = new DepthFirstVisitor();
        getRoot().accept(visitor, null);
        return visitor.getNodes();
    }

    public void toVerboseRepresentation() {
        VerboseGraphVisitor v = new VerboseGraphVisitor();
        getRoot().accept(v, null);
    }

    public static void insertSeriesSuccessor(Node node, Node successor) {
        node.insertSeriesSuccessor(successor);
    }

    public static void insertSeriesPredecessor(Node node, Node predecessor) {
        node.insertSeriesPredecessor(predecessor);
    }

    public static void insertParallel(Node node, Node parallel) {
        node.insertParallel(parallel);
    }

    @Override
    public String toString() {
        StringifyVisitor visitor = new StringifyVisitor();
        getRoot().accept(visitor, null);
        return visitor.asString();
    }

}
