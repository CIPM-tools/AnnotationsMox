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
 * @param <R>
 *            the visit method's return type
 */
public interface ReturnOrientedVisitor<R> {

	R visit(LeafNode n);

	R visit(EpsilonLeafNode n);

	R visit(ParallelNode n);

	R visit(SeriesNode n);
	
	R visit(RootNode n);

}
