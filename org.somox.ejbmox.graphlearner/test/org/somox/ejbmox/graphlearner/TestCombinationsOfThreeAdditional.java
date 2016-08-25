package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationsOfThreeAdditional {

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
    public void additionalStart() {
        learner.integratePath(PathBuilder.path("B", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("[A|]BC", learner.getGraph().toString());
    }

    @Test
    public void additionalMiddle() {
        learner.integratePath(PathBuilder.path("A", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("A[B|]C", learner.getGraph().toString());
    }

    @Test
    public void additionalEnd() {
        learner.integratePath(PathBuilder.path("A", "B"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("AB[C|]", learner.getGraph().toString());
    }

    @Test
    public void additionalStartAndEnd() {
        learner.integratePath(PathBuilder.path("B"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("[A|]B[C|]", learner.getGraph().toString());
    }

    @Test
    public void additionalStartAndMiddle() {
        learner.integratePath(PathBuilder.path("C"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("[AB|]C", learner.getGraph().toString());
    }

    @Test
    public void additionalMiddleAndEnd() {
        learner.integratePath(PathBuilder.path("A"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("A[BC|]", learner.getGraph().toString());
    }

}
