package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationsOfThree {

    private GraphLearner learner;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new GraphLearner();
    }

    @Test
    public void sequenceOfThree() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("ABC", learner.getGraph().toString());
    }

    @Test
    public void missingStart() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("B", "C"));
        Assert.assertEquals("[A|]BC", learner.getGraph().toString());
    }

    @Test
    public void missingMiddle() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "C"));
        Assert.assertEquals("A[B|]C", learner.getGraph().toString());
    }

    @Test
    public void missingEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "B"));
        Assert.assertEquals("AB[C|]", learner.getGraph().toString());
    }

    @Test
    public void missingStartAndEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("B"));
        Assert.assertEquals("[A|]B[C|]", learner.getGraph().toString());
    }

    @Test
    public void missingStartAndMiddle() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("C"));
        Assert.assertEquals("[AB|]C", learner.getGraph().toString()); // or [A|][B|]C?
    }

    @Test
    public void missingMiddleAndEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A"));
        Assert.assertEquals("A[BC|]", learner.getGraph().toString()); // or A[B|][C|]?
    }

    @Test
    public void missingAll() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("D"));
        Assert.assertEquals("[ABC|D]", learner.getGraph().toString()); // or [ABC|D]?
    }

}
