package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationOfFourAdditional {

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
    public void additionalFirst() {
        learner.integratePath(PathBuilder.path("B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[A|]BCD", learner.getGraph().toString());
    }

    @Test
    public void additionalSecond() {
        learner.integratePath(PathBuilder.path("A", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("A[B|]CD", learner.getGraph().toString());
    }

    @Test
    public void additionalThird() {
        learner.integratePath(PathBuilder.path("A", "B", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("AB[C|]D", learner.getGraph().toString());
    }

    @Test
    public void additionalFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("ABC[D|]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 2 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void additionalFirstSecond() {
        learner.integratePath(PathBuilder.path("C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[AB|]CD", learner.getGraph().toString()); // or [A|][B|]CD?
    }

    @Test
    public void additionalSecondThird() {
        learner.integratePath(PathBuilder.path("A", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("A[BC|]D", learner.getGraph().toString()); // or A[B|][C|]D?
    }

    @Test
    public void additionalThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("AB[CD|]", learner.getGraph().toString()); // or AB[C|][D|]?
    }

    @Test
    public void additionalFirstThird() {
        learner.integratePath(PathBuilder.path("B", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[A|]B[C|]D", learner.getGraph().toString());
    }

    @Test
    public void additionalFirstFourth() {
        learner.integratePath(PathBuilder.path("B", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[A|]BC[D|]", learner.getGraph().toString());
    }

    @Test
    public void additionalSecondFourth() {
        learner.integratePath(PathBuilder.path("A", "C"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("A[B|]C[D|]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 3 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void additionalFirstSecondThird() {
        learner.integratePath(PathBuilder.path("D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[ABC|]D", learner.getGraph().toString()); // or [A|][B|][C|]D?
    }

    @Test
    public void additionalSecondThirdFourth() {
        learner.integratePath(PathBuilder.path("A"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("A[BCD|]", learner.getGraph().toString()); // or A[B|][C|][D|]?
    }

    @Test
    public void additionalFirstSecondFourth() {
        learner.integratePath(PathBuilder.path("C"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[AB|]C[D|]", learner.getGraph().toString()); // or [A|][B|]C[D|]?
    }

    @Test
    public void additionalFirstThirdFourth() {
        learner.integratePath(PathBuilder.path("B"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        Assert.assertEquals("[A|]B[CD|]", learner.getGraph().toString()); // or [A|]B[C|][D|]?
    }

}
