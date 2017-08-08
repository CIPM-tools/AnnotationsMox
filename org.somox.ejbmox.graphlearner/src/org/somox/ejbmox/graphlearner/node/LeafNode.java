package org.somox.ejbmox.graphlearner.node;

import org.somox.ejbmox.graphlearner.ReturnOrientedVisitor;
import org.somox.ejbmox.graphlearner.Visitor;

public class LeafNode extends Node {

    private Object content;

    public LeafNode(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        return content.toString();
    }

    @Override
    public <R> void accept(Visitor<R> v, R arg) {
        v.visit(this, arg);
    }

    @Override
    public <R> R accept(ReturnOrientedVisitor<R> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LeafNode other = (LeafNode) obj;
        if (getContent() != other.getContent())
            return false;
        return true;
    }

}
