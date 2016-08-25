package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.List;

import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.inspectit2pcm.graphlearner.NodeAttribute;

public class PathUtils {

    public static double[] probabilities(String pathRepresentation, GraphLearner learner) {
        Path path = findPath(pathRepresentation, learner).excludeEpsilon();
        double[] probabilities = new double[path.size()];
        for (int i = 0; i < path.size(); i++) {
            probabilities[i] = (double) path.getNodes().get(i).getAttribute(NodeAttribute.INVOCATION_PROBABILITY);
        }
        return probabilities;
    }

    public static int[] pathCountLeaves(String pathRepresentation, GraphLearner learner) {
        Path path = findPath(pathRepresentation, learner).excludeNonLeaves().excludeEpsilon();
        int[] counts = new int[path.size()];
        for (int i = 0; i < path.size(); i++) {
            counts[i] = (int) path.getNodes().get(i).getAttribute(NodeAttribute.INVOCATION_COUNT);
        }
        return counts;
    }

    public static Path findPath(String pathRepresentation, GraphLearner learner) {
        List<Path> paths = learner.getGraph().allPaths();
        for (Path path : paths) {
            if (path.excludeNonLeaves().excludeEpsilon().toString().equals(pathRepresentation)) {
                return path;
            }
        }
        throw new RuntimeException("Could not find path represented by " + pathRepresentation);
    }

}
