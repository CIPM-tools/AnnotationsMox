package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * A chain of nestable {@link Node}s.
 * <p>
 * Nodes form a <strong>chain</strong> because each node has exactly one
 * predecessor (except for the root node), and exactly one successor (except for
 * the last node in the chain).
 * <p>
 * The node chain is <strong>nestable</strong> because {@link BranchingNode}
 * accomodate one or more {@link NestableNodeChain}, one chain for each
 * branching transition. Being nestable chains by themselves, branching
 * transitions may again contain branching nodes, and so on.
 * 
 * @author Philipp Merkle
 *
 * @param <T>
 */
public class NestableNodeChain<T> {

	private Node<T> root;

	public NestableNodeChain(Node<T> root) {
		this.root = root;
	}

	/**
	 * @return the root of this node chain
	 */
	public Node<T> getRoot() {
		return root;
	}

	/**
	 * Collects and returns all possible paths between the root node and the
	 * last node of this chain. Without {@link BranchingNode}s, there is only
	 * one such path. With a single branch that contains two transitions, there
	 * are two such paths, and so on.
	 * 
	 * @return the collected paths
	 */
	public List<Path<T>> collectAllPaths() {
		List<Path<T>> paths = reachablePathsFrom(root);
		return paths;
	}

	/**
	 * Collects and returns all paths (of maximum length) reachable from the
	 * given starting node.
	 * <p>
	 * A single path is returned if this chain does not contain any
	 * {@link BranchingNode}s.
	 * <p>
	 * One or more paths are returned if this chain contain
	 * {@link BranchingNode}s:
	 * <ul>
	 * <li>1 path is return if this chain contains one branch with one
	 * transition; for example, the chain A[B]C returns the path ABC.</li>
	 * <li>2 paths are returned if this chain contains one branch with two
	 * transitions; for example, the chain A[B|C]D returns the paths ABD and
	 * ACD.</li>
	 * </ul>
	 * 
	 * 
	 * @param node
	 * @return the collected paths
	 */
	private static <T> List<Path<T>> reachablePathsFrom(Node<T> node) {
		List<Path<T>> paths = new ArrayList<>();
		if (node == null) {
			paths.add(Path.emptyPath());
			return paths;
		}

		if (node.isBranch()) {
			BranchingNode<T> branch = (BranchingNode<T>) node;
			for (NestableNodeChain<T> transition : branch.getTransitions()) {
				for (Path<T> path : reachablePathsFrom(transition.getRoot())) {
					paths.addAll(concatenate(path, reachablePathsFrom(node.getSuccessor())));
				}
			}
		} else {
			paths = concatenate(node, reachablePathsFrom(node.getSuccessor()));
		}
		return paths;
	}

	/**
	 * Concatenates head with tail, where head is a {@link Path} and tail
	 * comprises one or more {@link Path}s.
	 * <p>
	 * If tail comprises <strong>one path</strong> (denoted by {@code tail}),
	 * this method returns one concatenated path: {@code head || tail}.
	 * <p>
	 * If tail comprises <strong>two paths</strong> (denoted by {@code tail1}
	 * and {@code tail2}), this method returns two concatenated paths:
	 * <ol>
	 * <li>{@code head || tail1}</li>
	 * <li>{@code head || tail2}</li>
	 * </ol>
	 * Three or more tail paths are handled analogously.
	 * 
	 * @param headPath
	 *            the head
	 * @param tailPaths
	 *            the tail
	 * @return the concatenated paths
	 */
	private static <T> List<Path<T>> concatenate(Path<T> headPath, List<Path<T>> tailPaths) {
		List<Path<T>> concatPaths = new ArrayList<>();
		for (Path<T> tailPath : tailPaths) {
			Path<T> concat = tailPath.expandAtBegin(headPath);
			concatPaths.add(concat);
		}
		return concatPaths;
	}

	/**
	 * Concatenates head with tail, where head is a {@link Node} and tail
	 * comprises one or more {@link Path}s.
	 * <p>
	 * If tail comprises <strong>one path</strong> (denoted by {@code tail}),
	 * this method returns one concatenated path: {@code head || tail}.
	 * <p>
	 * If tail comprises <strong>two paths</strong> (denoted by {@code tail1}
	 * and {@code tail2}), this method returns two concatenated paths:
	 * <ol>
	 * <li>{@code head || tail1}</li>
	 * <li>{@code head || tail2}</li>
	 * </ol>
	 * Three or more tail paths are handled analogously.
	 * 
	 * @param headNode
	 *            the head
	 * @param tailPaths
	 *            the tail
	 * @return the concatenated paths
	 */
	private static <T> List<Path<T>> concatenate(Node<T> headNode, List<Path<T>> tailPaths) {
		List<Path<T>> concatPaths = new ArrayList<>();
		for (Path<T> tailPath : tailPaths) {
			Path<T> concat = tailPath.expandAtBegin(headNode);
			concatPaths.add(concat);
		}
		return concatPaths;
	}

	public boolean isEmpty() {
		return root == null;
	}

	@Override
	public String toString() {
		String result = "";
		Node<T> current = root;
		do {
			result += current;
		} while ((current = current.getSuccessor()) != null);
		result += "";
		return result;
	}

}
