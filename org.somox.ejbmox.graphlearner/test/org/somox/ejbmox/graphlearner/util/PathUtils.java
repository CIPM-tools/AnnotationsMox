package org.somox.ejbmox.graphlearner.util;

import java.util.List;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;

public class PathUtils {

	public static int[] pathCounts(String pathRepresentation, GraphLearner learner) {
		Path path = findPath(pathRepresentation, learner);
		int[] counts = new int[path.size()];
		for (int i = 0; i < path.size(); i++) {
			counts[i] = path.getNodes().get(i).getCounter();
		}
		return counts;
	}

	public static Path findPath(String pathRepresentation, GraphLearner learner) {
		List<Path> paths = learner.getGraph().allPaths();
		for (Path path : paths) {
			if (path.toString().equals(pathRepresentation)) {
				return path;
			}
		}
		throw new RuntimeException("Could not find path represented by " + pathRepresentation);
	}
	
}
