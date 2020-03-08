package org.annotationsmox.inspectit2pcm.graphlearner;

import java.util.List;
import java.util.Stack;

import org.annotationsmox.graphlearner.Visitor;
import org.annotationsmox.graphlearner.node.EpsilonLeafNode;
import org.annotationsmox.graphlearner.node.LeafNode;
import org.annotationsmox.graphlearner.node.Node;
import org.annotationsmox.graphlearner.node.ParallelNode;
import org.annotationsmox.graphlearner.node.RootNode;
import org.annotationsmox.graphlearner.node.SeriesNode;
import org.annotationsmox.inspectit2pcm.aggregation.AggregationStrategy;
import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.annotationsmox.inspectit2pcm.parametrization.ParametrizationTrace;
import org.annotationsmox.inspectit2pcm.util.PCMHelper;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class Graph2SEFFVisitor implements Visitor<ResourceDemandingBehaviour> {

    private Stack<AbstractAction> lastActionStack;

    private ParametrizationTrace trace;

    private AggregationStrategy aggregationStrategy;

    public Graph2SEFFVisitor(AggregationStrategy aggregationStrategy) {
        this(new ParametrizationTrace(), aggregationStrategy);
    }

    public Graph2SEFFVisitor(ParametrizationTrace trace, AggregationStrategy aggregationStrategy) {
        this.trace = trace;
        this.aggregationStrategy = aggregationStrategy;
        lastActionStack = new Stack<>();
    }

    @Override
    public void visit(LeafNode n, ResourceDemandingBehaviour arg) {
        AbstractAction previousAction = lastActionStack.pop();

        // TODO extract statements below to separate method in separate class
        
        InternalAction ia = SeffFactory.eINSTANCE.createInternalAction();
        ia.setResourceDemandingBehaviour_AbstractAction(arg);
        ia.setEntityName(n.getContent().toString());
        if (n.getContent() instanceof SQLStatement) {
            SQLStatement stmt = (SQLStatement) n.getContent();

            // aggregate durations
            int invocationCount = (int) n.getAttribute(NodeAttribute.INVOCATION_COUNT);
            List<Double> durations = (List<Double>) n.getAttribute(NodeAttribute.DURATIONS);
            PCMRandomVariable rv = aggregationStrategy.aggregate(durations, ia);

            // store mean duration into model
            ia.getResourceDemand_Action().add(PCMHelper.createParametricResourceDemandCPU(rv));
            trace.addInternalActionToSQLStatementLink(ia, stmt);
        }

        ia.setPredecessor_AbstractAction(previousAction);
        lastActionStack.push(ia);
    }

    @Override
    public void visit(EpsilonLeafNode n, ResourceDemandingBehaviour arg) {
        // do nothing, which produces StartAction -> StopAction
    }

    @Override
    public void visit(ParallelNode n, ResourceDemandingBehaviour arg) {
        AbstractAction previousAction = lastActionStack.pop();

        // assumes that node is a branch (might also be a loop in future)
        BranchAction branch = SeffFactory.eINSTANCE.createBranchAction();
        branch.setResourceDemandingBehaviour_AbstractAction(arg);
        for (Node node : n.getChildren()) {
            ProbabilisticBranchTransition transition = SeffFactory.eINSTANCE.createProbabilisticBranchTransition();
            transition.setBranchAction_AbstractBranchTransition(branch);
            transition.setBranchBehaviour_BranchTransition(SeffFactory.eINSTANCE.createResourceDemandingBehaviour());
            transition.setBranchProbability((double) node.getAttribute(NodeAttribute.INVOCATION_PROBABILITY));
            node.accept(this, transition.getBranchBehaviour_BranchTransition());
        }

        branch.setPredecessor_AbstractAction(previousAction);
        lastActionStack.push(branch);
    }

    @Override
    public void visit(SeriesNode n, ResourceDemandingBehaviour arg) {
        StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
        startAction.setResourceDemandingBehaviour_AbstractAction(arg);
        lastActionStack.push(startAction);

        for (Node child : n.getChildren()) {
            child.accept(this, arg);
        }

        StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
        stopAction.setResourceDemandingBehaviour_AbstractAction(arg);
        stopAction.setPredecessor_AbstractAction(lastActionStack.pop());
    }

    @Override
    public void visit(RootNode n, ResourceDemandingBehaviour arg) {
        n.getChild().accept(this, arg);
    }

}
