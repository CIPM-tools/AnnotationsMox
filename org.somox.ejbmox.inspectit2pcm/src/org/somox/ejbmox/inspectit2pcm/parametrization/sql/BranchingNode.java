package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BranchingNode<T> extends Node<T> {

	private List<NestableNodeChain<T>> branches = new ArrayList<>();

	public void addTransition(Node<T> root) {
		branches.add(new NestableNodeChain<>(root));
	}

	public List<NestableNodeChain<T>> getTransitions() {
		return Collections.unmodifiableList(branches);
	}

	@Override
	public boolean isBranch() {
		return true;
	}

	/**
	 * Converts the path to a branching node and replaces the path with the
	 * newly created branch node by adjusting successor/predecessor relations.
	 * The path becomes a branch in the branching node.
	 * 
	 * @param path
	 *            the path to be converted
	 * @return the newly created branch
	 */
	public static <T> BranchingNode<T> convertToAndReplaceWithBranch(Path<T> path) {
		BranchingNode<T> branch = new BranchingNode<>();

		branch.addTransition(path.first());

		// adjust predecessor/successor relation: predecessor of path <-> branch
		Node<T> predecessorOfPath = path.first().getPredecessor();
		predecessorOfPath.setSuccessor(branch);
		branch.setPredecessor(predecessorOfPath);
		path.first().clearPredecessor();

		// adjust predecessor/successor relation: branch <-> successor of path
		Node<T> successorOfPath = path.last().getSuccessor();
		branch.setSuccessor(successorOfPath);
		if(successorOfPath != null) {
			successorOfPath.setPredecessor(branch);	
		}
		path.last().clearSuccessor();

		return branch;
	}

	@Override
	public String toString() {
		String result = "[";
		for (NestableNodeChain<T> t : branches) {
			result += t + "|";
		}
		// delete trailing "|", if present
		if (result.lastIndexOf("|") == result.length() - 1) {
			result = result.substring(0, result.length() - 1);
		}
		result += "]";
		return result;
	}

}
