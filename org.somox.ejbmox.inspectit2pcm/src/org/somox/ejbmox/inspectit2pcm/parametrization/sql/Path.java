package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A sequence of {@link ContentNode}s that represents a single path (or a
 * partial path) through a {@link NestableNodeChain}.
 * 
 * @author Philipp Merkle
 *
 * @param <T>
 */
public class Path<T> {

	private List<ContentNode<T>> nodes;

	private Path(List<ContentNode<T>> nodes) {
		this.nodes = nodes;
	}

	public static <T> Path<T> fromNode(Node<T> node) {
		ArrayList<Node<T>> nodeList = new ArrayList<>();
		nodeList.add(node);
		return fromNodeList(nodeList);
	}

	public static <T> Path<T> fromNodeList(List<? extends Node<T>> nodes) {
		if (nodes == null) {
			throw new IllegalArgumentException("Nodes may not be null");
		}
		if (nodes.isEmpty()) {
			return Path.emptyPath();
		}
		Node<T> last = null;
		for (Node<T> current : nodes) {
			// check that all consecutive nodes are pairwise successors
			if (last != null) {
				if (!last.getSuccessor().equals(current)) {
					throw new RuntimeException("All consecutive nodes need to be pairwise successors, but " + current
							+ " is not the successor of " + last);
				}
				last.setSuccessor(current);
			}
			// check for branching nodes
			if (current.isBranch()) {
				throw new RuntimeException("Branching nodes are not allowed in a path");
			}
			last = current;
		}
		// only content nodes at this point
		List<ContentNode<T>> contentNodes = new ArrayList<>();
		for (Node<T> n : nodes) {
			contentNodes.add((ContentNode<T>) n);
		}
		return new Path<>(contentNodes);
	}

	public static <T> Path<T> emptyPath() {
		return new Path<>(Collections.emptyList());
	}

	public List<ContentNode<T>> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public int size() {
		return nodes.size();
	}

	public ContentNode<T> first() {
		if (nodes.isEmpty()) {
			return null;
		}
		return nodes.get(0);
	}

	public ContentNode<T> last() {
		if (nodes.isEmpty()) {
			return null;
		}
		return nodes.get(nodes.size() - 1);
	}

	/**
	 * Adds to a path ABC a node N at the beginning of the path and returns the
	 * extended path NABC.
	 * 
	 * @param node
	 *            the new first node N to be added to the path's beginning
	 */
	public Path<T> expandAtBegin(Node<T> node) {
		if (node == null) {
			throw new IllegalArgumentException("Node may not be null");
		}
		if (node.isBranch()) {
			throw new RuntimeException("Branching nodes are not allowed in a path");
		}
		ContentNode<T> contentNode = (ContentNode<T>) node;
		List<ContentNode<T>> nodeList = new ArrayList<>();
		nodeList.add(contentNode);
		nodeList.addAll(nodes);
		return new Path<>(nodeList);
	}

	/**
	 * Adds to a path ABC a node N at the beginning of the path and returns the
	 * extended path NABC.
	 * 
	 * @param node
	 *            the new first node N to be added to the path's beginning
	 */
	public Path<T> expandAtBegin(Path<T> path) {
		// iterate backwards, beginning with path's last node
		ListIterator<ContentNode<T>> it = path.getNodes().listIterator(path.size());
		Path<T> intermediate = this;
		while (it.hasPrevious()) {
			intermediate = intermediate.expandAtBegin(it.previous());
		}
		return intermediate;
	}

	/**
	 * Adds to a path ABC a node N at the end of the path and returns the
	 * extended path ABCN.
	 * 
	 * @param node
	 *            the new last node N to be added to the path's end
	 */
	public Path<T> expandAtEnd(Node<T> node) {
		if (node == null) {
			throw new IllegalArgumentException("Node may not be null");
		}
		if (node.isBranch()) {
			throw new RuntimeException("Branching nodes are not allowed in a path");
		}
		ContentNode<T> contentNode = (ContentNode<T>) node;
		List<ContentNode<T>> nodeList = new ArrayList<>();
		nodeList.addAll(nodes);
		nodeList.add(contentNode);
		return new Path<>(nodeList);
	}

	@Override
	public String toString() {
		String result = "<";
		for (Node<T> n : nodes) {
			result += n;
		}
		result += ">";
		return result;
	}

}
