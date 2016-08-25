package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationOfFour {

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

    ////////////////////////////////////////////////
    // 1 Node Missing
    ///////////////////////////////////////////////

    @Test
    public void missingFirst() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("B", "C", "D"));
        Assert.assertEquals("[A|]BCD", learner.getGraph().toString());
    }

    @Test
    public void missingSecond() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "C", "D"));
        Assert.assertEquals("A[B|]CD", learner.getGraph().toString());
    }

    @Test
    public void missingThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "D"));
        Assert.assertEquals("AB[C|]D", learner.getGraph().toString());
    }

    @Test
    public void missingFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        Assert.assertEquals("ABC[D|]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 2 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void missingFirstSecond() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("C", "D"));
        Assert.assertEquals("[AB|]CD", learner.getGraph().toString()); // or [A|][B|]CD?
    }

    @Test
    public void missingSecondThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "D"));
        Assert.assertEquals("A[BC|]D", learner.getGraph().toString()); // or A[B|][C|]D?
    }

    @Test
    public void missingThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B"));
        Assert.assertEquals("AB[CD|]", learner.getGraph().toString()); // or AB[C|][D|]?
    }

    @Test
    public void missingFirstThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("B", "D"));
        Assert.assertEquals("[A|]B[C|]D", learner.getGraph().toString());
    }

    @Test
    public void missingFirstFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("B", "C"));
        Assert.assertEquals("[A|]BC[D|]", learner.getGraph().toString());
    }

    @Test
    public void missingSecondFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "C"));
        Assert.assertEquals("A[B|]C[D|]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 3 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void missingFirstSecondThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("D"));
        Assert.assertEquals("[ABC|]D", learner.getGraph().toString()); // or [A|][B|][C|]D?
    }

    @Test
    public void missingSecondThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A"));
        Assert.assertEquals("A[BCD|]", learner.getGraph().toString()); // or A[B|][C|][D|]?
    }

    @Test
    public void missingFirstSecondFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("C"));
        Assert.assertEquals("[AB|]C[D|]", learner.getGraph().toString()); // or [A|][B|]C[D|]?
    }

    @Test
    public void missingFirstThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("B"));
        Assert.assertEquals("[A|]B[CD|]", learner.getGraph().toString()); // or [A|]B[C|][D|]?
    }

}
