package org.annotationsmox.inspectit2pcm.parametrization;

import org.annotationsmox.graphlearner.GraphLearner;
import org.annotationsmox.graphlearner.util.PathBuilder;
import org.annotationsmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCounter {

    private GraphLearner<String> learner;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new InvocationGraphLearner<String>();
    }

    @Test
    public void testOneNode() {
        learner.integrateSequence("A");
        learner.integrateSequence("A");
        Assert.assertArrayEquals(new int[] { 2 }, PathUtils.pathCountLeaves(PathBuilder.path("A").toString(), learner));
    }

    @Test
    public void testTwoNodesSerial() {
        learner.integrateSequence("A", "B");
        learner.integrateSequence("A", "B");
        Assert.assertArrayEquals(new int[] { 2, 2 },
                PathUtils.pathCountLeaves(PathBuilder.path("A", "B").toString(), learner));
    }

    @Test
    public void testTwoNodesParallel() {
        learner.integrateSequence("A");
        learner.integrateSequence("A", "B");
        Assert.assertArrayEquals(new int[] { 2, 1 },
                PathUtils.pathCountLeaves(PathBuilder.path("A", "B").toString(), learner));
        Assert.assertArrayEquals(new int[] { 2 }, PathUtils.pathCountLeaves(PathBuilder.path("A").toString(), learner));
    }

    @Test
    public void testComplex() {
        learner.integrateSequence("A", "B");
        learner.integrateSequence("A", "B", "C");
        learner.integrateSequence("A", "X", "C");
        learner.integrateSequence("A", "X", "C", "A");

        Assert.assertArrayEquals(new int[] { 4, 2 },
                PathUtils.pathCountLeaves(PathBuilder.path("A", "B").toString(), learner));
        Assert.assertArrayEquals(new int[] { 4, 2, 3 },
                PathUtils.pathCountLeaves(PathBuilder.path("A", "X", "C").toString(), learner));
        Assert.assertArrayEquals(new int[] { 4, 2, 3, 1 },
                PathUtils.pathCountLeaves(PathBuilder.path("A", "X", "C", "A").toString(), learner));
    }

}
