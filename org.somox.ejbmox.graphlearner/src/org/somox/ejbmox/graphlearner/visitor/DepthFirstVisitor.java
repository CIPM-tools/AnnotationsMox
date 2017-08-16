package org.somox.ejbmox.graphlearner.visitor;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class DepthFirstVisitor implements Visitor<Void> {

    private List<Node> nodes = new ArrayList<>();

    private boolean includeEpsilon; // TODO make configurable

    private boolean includeSeriesAndParallel;

    public DepthFirstVisitor() {
        this(false);
    }

    public DepthFirstVisitor(boolean includeSeriesAndParallel) {
        this.includeSeriesAndParallel = includeSeriesAndParallel;
    }

    @Override
    public void visit(LeafNode n, Void arg) {
        nodes.add(n);
    }

    @Override
    public void visit(EpsilonLeafNode n, Void arg) {
        if (includeEpsilon) {
            nodes.add(n);
        }
    }

    @Override
    public void visit(ParallelNode n, Void arg) {
        if (includeSeriesAndParallel) {
            nodes.add(n);
        }
        for (Node child : n.getChildren()) {
            child.accept(this, arg);
        }
    }

    @Override
    public void visit(SeriesNode n, Void arg) {
        if (includeSeriesAndParallel) {
            nodes.add(n);
        }
        for (Node child : n.getChildren()) {
            child.accept(this, arg);
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public void visit(RootNode n, Void arg) {
        n.getChild().accept(this, arg);
    }

}
