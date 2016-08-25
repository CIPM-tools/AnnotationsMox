package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;

public class EnsureParallelParent implements ReturnOrientedVisitor<ParallelNode> {

    private Node node;

    public EnsureParallelParent(Node node) {
        this.node = node;
    }

    @Override
    public ParallelNode visit(SeriesNode currentParent) {
        ParallelNode newParent = new ParallelNode();

        // TODO copying attributes may not be appropriate in any case
        newParent.copyAttributesFrom(node);

        currentParent.replaceChild(node, newParent);
        node.setParent(newParent);
        return newParent;
    }

    @Override
    public ParallelNode visit(ParallelNode currentParent) {
        return currentParent; // return unmodified parent
    }

    @Override
    public ParallelNode visit(RootNode currentParent) {
        throw new RuntimeException("Cannot transform root node");
    }

    @Override
    public ParallelNode visit(LeafNode n) {
        throw new RuntimeException("This visitor should be applied to parent nodes only");
    }

    @Override
    public ParallelNode visit(EpsilonLeafNode n) {
        throw new RuntimeException("This visitor should be applied to parent nodes only");
    }

}
