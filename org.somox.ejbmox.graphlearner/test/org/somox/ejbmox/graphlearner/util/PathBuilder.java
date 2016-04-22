package org.somox.ejbmox.graphlearner.util;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;

public class PathBuilder {

	public static Path path(String... nodeNames) {
		List<Node> nodes = new ArrayList<>();
		for(String name : nodeNames) {
			nodes.add(new LeafNode(name));
		}
		return Path.fromNodes(nodes);
	}
	
}
