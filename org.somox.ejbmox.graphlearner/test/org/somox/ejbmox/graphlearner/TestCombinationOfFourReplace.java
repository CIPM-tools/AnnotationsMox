package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestCombinationOfFourReplace {

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
    public void replaceFirst() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "B", "C", "D"));
        Assert.assertEquals("[A|X]BCD", learner.getGraph().toString());
    }

    @Test
    public void replaceSecond() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "X", "C", "D"));
        Assert.assertEquals("A[B|X]CD", learner.getGraph().toString());
    }

    @Test
    public void replaceThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "X", "D"));
        Assert.assertEquals("AB[C|X]D", learner.getGraph().toString());
    }

    @Test
    public void replaceFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "C", "X"));
        Assert.assertEquals("ABC[D|X]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 2 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void replaceFirstSecond() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "Y", "C", "D"));
        Assert.assertEquals("[AB|XY]CD", learner.getGraph().toString());
    }

    @Test
    public void replaceSecondThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "X", "Y", "D"));
        Assert.assertEquals("A[BC|XY]D", learner.getGraph().toString());
    }

    @Test
    public void replaceThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "B", "X", "Y"));
        Assert.assertEquals("AB[CD|XY]", learner.getGraph().toString());
    }

    @Test
    public void replaceFirstThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "B", "Y", "D"));
        Assert.assertEquals("[A|X]B[C|Y]D", learner.getGraph().toString());
    }

    @Test
    public void replaceFirstFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "B", "C", "Y"));
        Assert.assertEquals("[A|X]BC[D|Y]", learner.getGraph().toString());
    }

    @Test
    public void replaceSecondFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "X", "C", "Y"));
        Assert.assertEquals("A[B|X]C[D|Y]", learner.getGraph().toString());
    }

    ////////////////////////////////////////////////
    // 3 Nodes Missing
    ///////////////////////////////////////////////

    @Test
    public void replaceFirstSecondThird() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "Y", "Z", "D"));
        Assert.assertEquals("[ABC|XYZ]D", learner.getGraph().toString());
    }

    @Test
    public void replaceSecondThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("A", "X", "Y", "Z"));
        Assert.assertEquals("A[BCD|XYZ]", learner.getGraph().toString());
    }

    @Test
    public void replaceFirstSecondFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "Y", "C", "Z"));
        Assert.assertEquals("[AB|XY]C[D|Z]", learner.getGraph().toString());
    }

    @Test
    public void replaceFirstThirdFourth() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "D"));
        learner.integratePath(PathBuilder.path("X", "B", "Y", "Z"));
        Assert.assertEquals("[A|X]B[CD|YZ]", learner.getGraph().toString());
    }

}
