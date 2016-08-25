package org.somox.ejbmox.graphlearner;

import org.somox.ejbmox.graphlearner.node.EpsilonLeafNode;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.ParallelNode;
import org.somox.ejbmox.graphlearner.node.RootNode;
import org.somox.ejbmox.graphlearner.node.SeriesNode;

/**
 * 
 * @author Philipp Merkle
 *
 * @param <T>
 *            the type of the visit method's second parameter
 */
public interface Visitor<T> {

    void visit(LeafNode n, T arg);

    void visit(EpsilonLeafNode n, T arg);

    void visit(ParallelNode n, T arg);

    void visit(SeriesNode n, T arg);

    void visit(RootNode n, T arg);

}
