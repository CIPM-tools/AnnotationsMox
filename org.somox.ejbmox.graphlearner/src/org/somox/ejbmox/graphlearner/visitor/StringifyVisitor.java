package org.somox.ejbmox.graphlearner.visitor;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class StringifyVisitor implements Visitor<Void> {

	private StringBuilder builder = new StringBuilder();

	private String epsilonRepresentation;

	public StringifyVisitor() {
		this("");
	}

	public StringifyVisitor(String epsilonRepresentation) {
		this.epsilonRepresentation = epsilonRepresentation;
	}

	@Override
	public void visit(LeafNode n, Void arg) {
		builder.append(n.getContent());
	}

	@Override
	public void visit(EpsilonLeafNode n, Void arg) {
		builder.append(epsilonRepresentation);
	}

	@Override
	public void visit(ParallelNode n, Void arg) {
		builder.append("[");
		for (Node child : n.getChildren()) {
			child.accept(this, arg);
			builder.append("|");
		}
		if (builder.charAt(builder.length() - 1) == '|') {
			builder.deleteCharAt(builder.length() - 1);
		}
		builder.append("]");
	}

	@Override
	public void visit(SeriesNode n, Void arg) {
		for (Node child : n.getChildren()) {
			child.accept(this, arg);
		}
	}

	@Override
	public void visit(RootNode n, Void arg) {
		n.getChild().accept(this, arg);
	}

	public String asString() {
		return builder.toString();
	}

}
