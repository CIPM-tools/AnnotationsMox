package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;
import org.somox.ejbmox.graphlearner.Visitor;

public class EpsilonLeafNode extends LeafNode {

    public static String EPSILON_CONTENT = "e";

    public EpsilonLeafNode() {
        super(EPSILON_CONTENT);
    }

    @Override
    public <R> R accept(ReturnOrientedVisitor<R> v) {
        return v.visit(this);
    }

    @Override
    public <R> void accept(Visitor<R> v, R arg) {
        v.visit(this, arg);
    }

}
