package org.somox.ejbmox.graphlearner;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.graphlearner.util.TikzTracer;
import org.somox.ejbmox.graphlearner.util.TikzTreeVisitorFactory;
import org.somox.ejbmox.graphlearner.visitor.TikZTreeVisitor;

/*
 * Hint: use \columnbreak to refine output
 */
public class GenerateExample {

    private GraphLearner learner;

    private TikzTreeVisitorFactory visitorFactory;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new GraphLearner();
        visitorFactory = TikZTreeVisitor.getFactory();
    }

    @Test
    public void generateExampleA() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

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
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

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

    @Test
    public void generateExampleC() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

        Path p1 = PathBuilder.path("C", "D", "E", "F", "G");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "B", "C", "F", "G");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        Path p3 = PathBuilder.path("A", "B", "G");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);

        System.out.println(tracer.getResult());
    }

    @Test
    public void generateExampleD() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

        Path p1 = PathBuilder.path("A", "B", "C", "G");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("F", "G");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        Path p3 = PathBuilder.path("A", "F", "G");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);

        System.out.println(tracer.getResult());

    }

    @Test
    public void generateExampleE() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

        Path p1 = PathBuilder.path("A", "B", "C");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "C");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        Path p3 = PathBuilder.path("C");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);

        System.out.println(tracer.getResult());
    }

    @Test
    public void generateExampleG() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

        Path p1 = PathBuilder.path("A", "B", "C");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "D");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);

        System.out.println(tracer.getResult());
    }
    
}
