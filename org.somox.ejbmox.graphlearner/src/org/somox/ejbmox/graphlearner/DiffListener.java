package org.somox.ejbmox.graphlearner;

import org.somox.ejbmox.graphlearner.node.Node;

public interface DiffListener {

    void change(Path originalPath, Path revisedPath);

    void delete(Path deletePath);

    void insert(Path insertPath, Node nodeBeforeInsert);

}
