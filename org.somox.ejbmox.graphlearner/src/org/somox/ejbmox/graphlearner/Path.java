package org.somox.ejbmox.graphlearner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.NestableNode;
import org.somox.ejbmox.graphlearner.node.Node;

public class Path implements Cloneable {

	private List<Node> nodes = new ArrayList<>();
	
	public Path() {
		nodes = new ArrayList<>();
	}
	
	public static Path emptyPath() {
		return new Path();
	}
	
	public void add(Node node) {
		nodes.add(node);
	}

	public static Path fromNodes(Node... nodes) {
		return fromNodes(Arrays.asList(nodes));
	}
	
	public static Path fromNodes(List<Node> nodes) {
		Path path = new Path();
		path.nodes = nodes;
		return path;
	}
	
	@Override
	public String toString() {
		return nodes.toString();
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public int size() {
		return nodes.size();
	}
	
	public boolean isEmpty() {
		return nodes.isEmpty();
	}
	
	public Node first() {
		return nodes.get(0);
	}
	
	/**
	 * 
	 * @param fromIndex inclusive
	 * @return
	 */
	public Path subPathStartingAt(int fromIndex) {
		return fromNodes(nodes.subList(fromIndex, nodes.size()));
	}
	
	// TODO better use visitor to avoid instanceof?
	public Path excludeEpsilon() {
		List<Node> result = new ArrayList<>();
		for(Node n : getNodes()) {
			if(!(n instanceof EpsilonLeafNode)) {
				result.add(n);
			}
		}
		return fromNodes(result);
	}

	// TODO better use visitor to avoid instanceof?
	public Path excludeNonLeaves() {
		List<Node> result = new ArrayList<>();
		for(Node n : getNodes()) {
			if(!(n instanceof NestableNode)) {
				result.add(n);
			}
		}
		return fromNodes(result);
	}

	@Override
	public Path clone() throws CloneNotSupportedException {
		Path cloned = new Path();
		cloned.nodes = new ArrayList<>(nodes);
		return cloned;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
}
