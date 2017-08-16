package org.somox.ejbmox.graphlearner;

import org.somox.ejbmox.graphlearner.node.Node;

public interface DiffListener {

    void change(Path originalPath, Path revisedPath);

    void delete(Path deletePath);

    void insertAfter(Node reference, Path insertPath);
    
    void insertBefore(Node reference, Path insertPath);

}
