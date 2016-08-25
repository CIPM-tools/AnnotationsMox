package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationsOfThreeReplace {

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
    public void differentStart() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("X", "B", "C"));
        Assert.assertEquals("[A|X]BC", learner.getGraph().toString());
    }

    @Test
    public void differentMiddle() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "X", "C"));
        Assert.assertEquals("A[B|X]C", learner.getGraph().toString());
    }

    @Test
    public void differentEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "X"));
        Assert.assertEquals("AB[C|X]", learner.getGraph().toString());
    }

    @Test
    public void differentStartAndEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("X", "B", "Y"));
        Assert.assertEquals("[A|X]B[C|Y]", learner.getGraph().toString());
    }

    @Test
    public void differentStartAndMiddle() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("X", "Y", "C"));
        Assert.assertEquals("[AB|XY]C", learner.getGraph().toString());
    }

    @Test
    public void differentMiddleAndEnd() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "X", "Y"));
        Assert.assertEquals("A[BC|XY]", learner.getGraph().toString());
    }

}
