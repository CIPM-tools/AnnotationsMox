package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.graphlearner.util.TikzTracer;

/*
 * Hint: use \columnbreak to refine output
 */
public class GenerateExample {

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
    public void generateExampleA() {
        TikzTracer tracer = TikzTracer.trace(learner);

        Path p1 = PathBuilder.path("A", "X", "Y", "Z", "B");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "X", "B");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        Path p3 = PathBuilder.path("A", "Z", "B");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);

        System.out.println(tracer.getResult());
    }

    @Test
    public void generateExampleB() {
        TikzTracer tracer = TikzTracer.trace(learner);

        Path p1 = PathBuilder.path("X", "Y", "Z", "U");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "D", "Z", "U");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        Path p3 = PathBuilder.path("U");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);

        System.out.println(tracer.getResult());
    }

}
