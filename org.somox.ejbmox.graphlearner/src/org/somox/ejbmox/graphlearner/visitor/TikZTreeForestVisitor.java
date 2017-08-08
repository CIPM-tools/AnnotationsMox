package org.somox.ejbmox.graphlearner.visitor;

import java.util.List;

import org.somox.ejbmox.graphlearner.Visitor;
import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

public class TikZTreeForestVisitor implements Visitor<Integer> {

    private StringBuilder builder = new StringBuilder();

    private List<Node> boldNodes;

    public TikZTreeForestVisitor(List<Node> boldNodes) {
        this.boldNodes = boldNodes;
    }

    @Override
    public void visit(LeafNode n, Integer depth) {
        builder.append(indent(depth));
        builder.append("[{" + n.getContent() + "},squared");
        if (boldNodes.contains(n) && boldNodes.get(boldNodes.indexOf(n)) == n) {
            builder.append(",very thick");
        }
        builder.append("]\n");
    }

    @Override
    public void visit(EpsilonLeafNode n, Integer depth) {
        builder.append(indent(depth));
        builder.append("[{$\\epsilon$},squared]\n");
    }

    @Override
    public void visit(ParallelNode n, Integer depth) {
        builder.append(indent(depth));
        builder.append("[{P},circled");
        builder.append("\n");
        for (Node child : n.getChildren()) {
            child.accept(this, depth + 1);
        }
        builder.append("]");
    }

    @Override
    public void visit(SeriesNode n, Integer depth) {
        builder.append(indent(depth));
        builder.append("[{S},circled");
        builder.append("\n");
        for (Node child : n.getChildren()) {
            child.accept(this, depth + 1);
        }
        builder.append("]");
    }

    @Override
    public void visit(RootNode n, Integer depth) {
        builder.append("\\begin{forest}\n");
        builder.append("baseline,\n");
        builder.append("circled/.style={circle,draw},\n");
        builder.append("squared/.style={rectangle,draw}\n");
        n.getChild().accept(this, depth + 1);
        builder.append("\\end{forest}");
    }

    public String asString() {
        return builder.toString();
    }

    private static String indent(int length) {
        String indentation = "";
        for (int i = 0; i < length; i++) {
            indentation += "\t";
        }
        return indentation;
    }

}
