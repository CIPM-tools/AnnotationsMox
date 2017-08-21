package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.Sequence;
import org.somox.ejbmox.graphlearner.TestUtils;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.somox.ejbmox.inspectit2pcm.graphlearner.NodeAttribute;

public class TestInvocationProbabilityVisitor2 {

    private static final double DELTA = 1.0 / 1_000_000;

    private GraphLearner<String> learner;

    private static Random random = new Random();

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Test
    public void testComplex() {
        learner = new InvocationGraphLearner<>();

        learner.integrateSequence("A", "B", "C", "D");
        learner.integrateSequence("A", "B", "C", "D");
        learner.integrateSequence("A", "B", "C", "D");
        learner.integrateSequence("A", "B", "X", "Y");
        learner.integrateSequence("A", "B", "X", "Y");
        learner.integrateSequence("A", "B", "X", "Z");

        Assert.assertEquals("AB[CD|X[Y|Z]]", learner.getGraph().toString());

        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        Assert.assertArrayEquals(
                new double[] { /* root */1.0, /* s */1.0, /* A */1.0, /* B */1.0, /* p */1.0, /* s */ 0.5, /* C */1.0,
                        /* D */1.0 },
                PathUtils.probabilities(PathBuilder.path("A", "B", "C", "D").toString(), learner), DELTA);
    }

    // private Path[] buildPaths(int count) {
    // String[] path = new String[]{"A", "B", "C", "D", "E", "F", "G"};
    // }

    private static String[] mutate(String[] path) {

        int MAX = 5;
        int MIN = 1;
        int length = intBetween(MIN, MAX);
        int start = intBetween(0, path.length - length); // TODO debug

        return delete(path, start, length);
    }

    private static int intBetween(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);

        // String[] res = delete(new String[] {"A", "B", "C"}, 1, 1);
        // System.out.println(Arrays.toString(res));

        for (int k = 0; k < 2000; k++) {
            GraphLearner<String> learner = new InvocationGraphLearner<>();

            String[] path = new String[] { "A", "B", "C", "D", "E", "F", "G" };
            List<Sequence<String>> mutations = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                String[] mutation = mutate(path);
                Sequence<String> mutationSequence = Sequence.from(mutation);
                mutations.add(mutationSequence);
                learner.integrateSequence(mutationSequence);
            }

            // calculate probabilities from invocation counts
            learner.getGraph().traverse(new InvocationProbabilityVisitor());

            List<Path> actualPaths = learner.getGraph().allPaths();
            Set<String> actualSequences = TestUtils.pathToSetOfStrings(actualPaths);

            for (Sequence<String> expectedSequence : mutations) {
                String expected = expectedSequence.toString();

                if (!actualSequences.contains(expected)) {
                    System.out.println("------------------");
                    System.out.println(expectedSequence + " not found in " + actualPaths);
                    System.out.println(mutations);
                }
            }
        }

    }

    private static double probability(Path p) {
        List<Node> nodes = p.excludeEpsilon().excludeNonLeaves().getNodes();
        double probability = 1;
        for (Node n : nodes) {
            double prob = (double) n.getAttribute(NodeAttribute.INVOCATION_PROBABILITY);
            probability *= prob;
        }
        return probability;
    }

    private static String[] delete(String[] path, int start, int length) {
        if (start + length > path.length) {
            throw new IllegalArgumentException();
        }

        String[] result = new String[path.length - length];
        int resultPos = 0;
        // before delete
        for (int i = 0; i < start; i++) {
            result[resultPos++] = path[i];
        }
        // after delete
        for (int i = start + length; i < path.length; i++) {
            result[resultPos++] = path[i];
        }
        return result;
    }

}
