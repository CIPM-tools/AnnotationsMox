package org.somox.ejbmox.graphlearner;

import org.somox.ejbmox.graphlearner.node.Node;

public interface ReorganizationListener {

    public void insertParallel(Node node, Node node2);

    public void insertSeriesSuccessor(Node node, Node successor);

    public void insertSeriesPredecessor(Node node, Node predecessor);
    
}
