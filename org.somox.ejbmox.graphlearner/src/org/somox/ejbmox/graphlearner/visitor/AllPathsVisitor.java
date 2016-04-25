package org.somox.ejbmox.graphlearner.visitor;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class AllPathsVisitor implements Visitor<List<Path>> {

	@Override
	public void visit(LeafNode node, List<Path> paths) {
		concat(paths, node);
	}

	@Override
	public void visit(EpsilonLeafNode n, List<Path> arg) {
		// include only "real" leaf nodes
	}

	@Override
	public void visit(ParallelNode node, List<Path> paths) {
		List<Path> pathsNew = new ArrayList<>();
		for (Node child : node.getChildren()) {
			List<Path> pathsCopy = copy(paths);
			child.accept(this, pathsCopy);
			pathsNew.addAll(pathsCopy);
		}
		paths.clear();
		paths.addAll(pathsNew);
	}

	@Override
	public void visit(SeriesNode node, List<Path> paths) {
		for (Node child : node.getChildren()) {
			child.accept(this, paths);
		}
	}

	@Override
	public void visit(RootNode n, List<Path> arg) {
		n.getChild().accept(this, arg);
	}

	private List<Path> copy(List<Path> paths) {
		List<Path> copy = new ArrayList<>();
		for (Path path : paths) {
			try {
				copy.add(path.clone());
			} catch (CloneNotSupportedException e) {
				// should not happen
				throw new RuntimeException(e);
			}
		}
		return copy;
	}

	private List<Path> concat(List<Path> paths, Node node) {
		for (Path path : paths) {
			path.add(node);
		}
		return paths;
	}

}