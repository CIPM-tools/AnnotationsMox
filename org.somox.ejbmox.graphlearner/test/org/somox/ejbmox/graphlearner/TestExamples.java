package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.util.PathBuilder;

public class TestExamples {
    
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
    public void testExample1() {
        learner.integratePath(PathBuilder.path("A", "B", "C", "G"));
        learner.integratePath(PathBuilder.path("A", "B"));
        learner.integratePath(PathBuilder.path("A", "F", "G"));
        Assert.assertEquals("A[B|F][[C|]G|]", learner.getGraph().toString());
    }

}
