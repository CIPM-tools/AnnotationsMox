package org.somox.ejbmox.inspectit2pcm.parametrization;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.util.PathBuilder;
import org.somox.ejbmox.graphlearner.util.TikzTracer;
import org.somox.ejbmox.graphlearner.util.TikzTreeVisitorFactory;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationGraphTikzTreeVisitor;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;

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
        learner = new InvocationGraphLearner();
        visitorFactory = InvocationGraphTikzTreeVisitor.getFactory();
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
        learner.getGraph().traverse(new InvocationProbabilityVisitor());
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "C");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);
        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        Path p3 = PathBuilder.path("C");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);
        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        tracer.forcePlot();

        System.out.println(tracer.getResult());
    }
    
    @Test
    public void generateExampleF() {
        TikzTracer tracer = TikzTracer.trace(learner, visitorFactory);

        Path p1 = PathBuilder.path("A", "B", "C", "G");
        tracer.announceIntegration(p1);
        learner.integratePath(p1);
        learner.getGraph().traverse(new InvocationProbabilityVisitor());
        tracer.forcePlot();

        Path p2 = PathBuilder.path("A", "B");
        tracer.announceIntegration(p2);
        learner.integratePath(p2);
        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        Path p3 = PathBuilder.path("A", "F", "G");
        tracer.announceIntegration(p3);
        learner.integratePath(p3);
        learner.getGraph().traverse(new InvocationProbabilityVisitor());

        tracer.forcePlot();

        System.out.println(tracer.getResult());
    }
    

}
