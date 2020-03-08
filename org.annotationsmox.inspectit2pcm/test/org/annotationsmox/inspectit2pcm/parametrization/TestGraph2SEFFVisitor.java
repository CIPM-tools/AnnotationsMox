package org.annotationsmox.inspectit2pcm.parametrization;

import org.annotationsmox.graphlearner.SPGraph;
import org.annotationsmox.inspectit2pcm.aggregation.MeanAggregationStrategy;
import org.annotationsmox.inspectit2pcm.graphlearner.Graph2SEFFVisitor;
import org.annotationsmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.annotationsmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.annotationsmox.inspectit2pcm.util.PCMHelper;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.SeffPackage;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class TestGraph2SEFFVisitor {

    private InvocationGraphLearner<String> learner;

    @BeforeClass
    public static void setup() {
        // log4j basic setup
        BasicConfigurator.configure();
    }

    @Before
    public void beforeTest() {
        learner = new InvocationGraphLearner<>();
    }

    @Test
    public void testSingleNode() {
        learner.integrateSequence("A");

        SPGraph learnedGraph = learner.getGraph();
        ResourceDemandingBehaviour behaviour = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
        learnedGraph.toVerboseRepresentation();
        learnedGraph.traverse(new InvocationProbabilityVisitor());
        learnedGraph.traverse(new Graph2SEFFVisitor(new MeanAggregationStrategy()), behaviour);

        Assert.assertEquals(3, behaviour.getSteps_Behaviour().size());

        StartAction start = PCMHelper.findStartAction(behaviour);
        StopAction stop = PCMHelper.findStopAction(behaviour);
        Assert.assertTrue(start.getSuccessor_AbstractAction().equals(stop.getPredecessor_AbstractAction()));
    }

    @Test
    public void testTwoNodes() {
        learner.integrateSequence("A", "B");

        SPGraph learnedGraph = learner.getGraph();
        ResourceDemandingBehaviour behaviour = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
        learnedGraph.toVerboseRepresentation();
        learnedGraph.traverse(new InvocationProbabilityVisitor());
        learnedGraph.traverse(new Graph2SEFFVisitor(new MeanAggregationStrategy()), behaviour);

        Assert.assertEquals(4, behaviour.getSteps_Behaviour().size());

        StartAction start = PCMHelper.findStartAction(behaviour);
        StopAction stop = PCMHelper.findStopAction(behaviour);
        Assert.assertTrue(start.getSuccessor_AbstractAction().getSuccessor_AbstractAction()
                .equals(stop.getPredecessor_AbstractAction()));
    }

    @Test
    public void testTwoNodes_oneParallel() {
        learner.integrateSequence("A", "B");
        learner.integrateSequence("A", "C");

        SPGraph learnedGraph = learner.getGraph();
        ResourceDemandingBehaviour behaviour = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
        learnedGraph.toVerboseRepresentation();
        learnedGraph.traverse(new InvocationProbabilityVisitor());
        learnedGraph.traverse(new Graph2SEFFVisitor(new MeanAggregationStrategy()), behaviour);

        Assert.assertEquals(4, behaviour.getSteps_Behaviour().size());

        // Stop must be reachable from Start: Start -> ... -> ... -> Stop with 2
        // actions in between
        StartAction start = PCMHelper.findStartAction(behaviour);
        StopAction stop = PCMHelper.findStopAction(behaviour);
        Assert.assertTrue(start.getSuccessor_AbstractAction().getSuccessor_AbstractAction()
                .getSuccessor_AbstractAction().equals(stop));

        // predecessor of Stop must be a BranchAction
        Assert.assertTrue(SeffPackage.eINSTANCE.getBranchAction().isInstance(stop.getPredecessor_AbstractAction()));

        // Branch must contain two branch transitions
        BranchAction branch = (BranchAction) stop.getPredecessor_AbstractAction();
        Assert.assertEquals(2, branch.getBranches_Branch().size());

        // each Branch transition must contain a sequence Start -> ... -> Stop
        // with 1 action in between
        for (AbstractBranchTransition transition : branch.getBranches_Branch()) {
            ResourceDemandingBehaviour transitionBehaviour = transition.getBranchBehaviour_BranchTransition();
            StartAction transitionStart = PCMHelper.findStartAction(transitionBehaviour);
            StopAction transitionStop = PCMHelper.findStopAction(transitionBehaviour);
            Assert.assertTrue(
                    transitionStart.getSuccessor_AbstractAction().getSuccessor_AbstractAction().equals(transitionStop));
        }
    }

}
