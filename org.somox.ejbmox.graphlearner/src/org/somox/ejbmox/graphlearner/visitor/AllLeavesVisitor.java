package org.somox.ejbmox.graphlearner.visitor;

import java.util.LinkedList;
import java.util.List;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class AllLeavesVisitor implements Visitor<Void> {

    private List<Node> leaves = new LinkedList<>();

    private boolean includeEpsilon = false; // TODO make configurable

    @Override
    public void visit(LeafNode n, Void arg) {
        leaves.add(n);
    }

    @Override
    public void visit(EpsilonLeafNode n, Void arg) {
        if (includeEpsilon) {
            leaves.add(n);
        }
    }

    @Override
    public void visit(ParallelNode n, Void arg) {
        for (Node child : n.getChildren()) {
            child.accept(this, arg);
        }
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

    public List<Node> getResult() {
        return leaves;
    }

}