package org.somox.ejbmox.graphlearner.util;

import org.somox.ejbmox.graphlearner.visitor.TikZTreeVisitor;

public interface TikzTreeVisitorFactory {

    TikZTreeVisitor create(); 
    
}
