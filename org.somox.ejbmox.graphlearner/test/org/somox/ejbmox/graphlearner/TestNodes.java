package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestNodes {

    private GraphLearner learner1;

    private GraphLearner learner2;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner1 = new GraphLearner();
        learner2 = new GraphLearner();
    }

    @Test
    public void equalsTest1() {
        learner1.integratePath(PathBuilder.path("A"));
        learner2.integratePath(PathBuilder.path("A"));
        Node n1 = learner1.getGraph().allPaths().get(0).getNodes().get(0);
        Node n2 = learner2.getGraph().allPaths().get(0).getNodes().get(0);
        Assert.assertEquals(n1, n2);
    }

    @Test
    public void equalsTest2() {
        learner1.integratePath(PathBuilder.path("A", "A"));
        learner2.integratePath(PathBuilder.path("A", "A"));
        Node first1 = learner1.getGraph().allPaths().get(0).getNodes().get(0);
        Node second1 = learner1.getGraph().allPaths().get(0).getNodes().get(1);
        Node first2 = learner2.getGraph().allPaths().get(0).getNodes().get(0);
        Node second2 = learner2.getGraph().allPaths().get(0).getNodes().get(1);
        Assert.assertEquals(first1, first2);
        Assert.assertEquals(second1, second2);
        Assert.assertNotEquals(first1, second2);
        Assert.assertNotEquals(first2, second1);
    }

}
