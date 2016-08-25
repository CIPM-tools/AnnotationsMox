package org.somox.ejbmox.inspectit2pcm.parametrization;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;

public class TestInvocationProbabilityVisitor {

    private static final double DELTA = 1.0 / 1_000_000;

    private GraphLearner learner;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new InvocationGraphLearner();
    }

    @Test
    public void testComplex() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "X", "Y"));
        learner.integratePath(PathBuilder.path("A", "B", "X", "Y"));
        learner.integratePath(PathBuilder.path("A", "B", "X", "Z"));

        Assert.assertEquals("AB[CD|X[Y|Z]]", learner.getGraph().toString());

        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        Assert.assertArrayEquals(
                new double[] { /* root */1.0, /* s */1.0, /* A */1.0, /* B */1.0, /* p */1.0, /* s */ 0.5, /* C */1.0,
                        /* D */1.0 },
                PathUtils.probabilities(PathBuilder.path("A", "B", "C", "D").toString(), learner), DELTA);
    }

}
