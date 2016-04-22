package org.somox.ejbmox.graphlearner;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.graphlearner.node.NestableNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;
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

	private Node root;

	public SPGraph(Node root) {
		this.root = root;
	}

	public static SPGraph fromPath(Path path) {
		if (path.isEmpty()) {
			throw new RuntimeException("Path may not be empty");
		}
		SPGraph graph = new SPGraph(path.first());
		Node lastNode = graph.root;
		for (Node node : path.subPathStartingAt(1).getNodes()) {
			graph.insertSuccessor(lastNode, node);
			lastNode = node;
		}
		return graph;
	}

	public Node getRoot() {
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

	public NestableNode ensureSeriesParent(Node anchor) {
		if (anchor.getParent() == null) { // anchor is root
			SeriesNode n = new SeriesNode();
			anchor.insertAsChild(n);
			root = n;
			return n;
		} else if (anchor.getParent() instanceof SeriesNode) {
			return (SeriesNode) anchor.getParent();
		} else if (anchor.getParent() instanceof ParallelNode) {
			NestableNode n = anchor.insertParent(new SeriesNode());
			return n;
		} else {
			throw new RuntimeException("Unknown type: " + anchor.getParent().getClass());
		}
	}

	public NestableNode ensureParallelParent(Node anchor) {
		if (anchor.getParent() == null) { // anchor is root
			ParallelNode n = new ParallelNode();
			anchor.insertAsChild(n);
			root = n;
			return n;
		} else if (anchor.getParent() instanceof SeriesNode) {
			NestableNode n = anchor.insertParent(new ParallelNode());
			return n;
		} else if (anchor.getParent() instanceof ParallelNode) {
			return anchor.getParent();
		} else {
			throw new RuntimeException("Unknown type: " + anchor.getParent().getClass());
		}
	}

	public void insertSuccessor(Node anchor, Node successor) {
		ensureSeriesParent(anchor);
		successor.insertAfter(anchor);
	}

	public void insertPredecessor(Node anchor, Node predecessor) {
		ensureSeriesParent(anchor);
		predecessor.insertBefore(anchor);
	}

	public void insertParallel(Node anchor, Node parallel) {
		ensureParallelParent(anchor);
		parallel.insertAfter(anchor);
	}

	@Override
	public String toString() {
		StringifyVisitor visitor = new StringifyVisitor();
		getRoot().accept(visitor, null);
		return visitor.asString();
	}

}
