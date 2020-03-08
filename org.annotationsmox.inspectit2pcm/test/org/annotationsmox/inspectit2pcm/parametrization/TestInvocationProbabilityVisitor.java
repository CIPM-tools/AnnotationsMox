package org.annotationsmox.inspectit2pcm.parametrization;

import org.annotationsmox.graphlearner.GraphLearner;
import org.annotationsmox.graphlearner.util.PathBuilder;
import org.annotationsmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.annotationsmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestInvocationProbabilityVisitor {

    private static final double DELTA = 1.0 / 1_000_000;

    private GraphLearner<String> learner;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new InvocationGraphLearner<>();
    }

    @Test
    public void testComplex() {
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

}
