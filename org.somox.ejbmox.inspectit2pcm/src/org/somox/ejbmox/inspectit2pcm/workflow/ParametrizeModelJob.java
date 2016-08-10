package org.somox.ejbmox.inspectit2pcm.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.mdsdprofiles.api.ProfileAPI;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.inspectit2pcm.graphlearner.Graph2SEFFVisitor;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementsToPCM;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ParametrizeModelJob extends AbstractII2PCMJob {

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        this.logger.info("Storing monitored runtime behaviour to PCM model...");

        // TODO make configurable;
        final AggregationStrategy aggregation = AggregationStrategy.MEAN;
        switch (aggregation) {
        case HISTOGRAM:
            throw new UnsupportedOperationException();
            // break;
        case MEAN:
            this.parametrizeResourceDemandsWithMean(this.getPartition().getParametrization());
            this.parametrizeSQLStatementsWithMean(this.getPartition().getParametrization());
            this.parametrizeBranchingProbabilities(this.getPartition().getParametrization());
            break;
        case MEDIAN:
            throw new UnsupportedOperationException();
            // break;
        default:
            throw new RuntimeException("Unknown aggregation strategy: " + aggregation);
        }
    }

    @Override
    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Parametrize PCM Model";
    }

    private void resetBranchingProbabilities(final PCMParametrization parametrization) {
        // collect all branches for which there is at least one branching
        // probability
        final Set<BranchAction> branches = new HashSet<>();
        for (final AbstractBranchTransition t : parametrization.getBranchTransitionMap().keySet()) {
            branches.add(t.getBranchAction_AbstractBranchTransition());
        }

        // reset branching probabilities
        for (final BranchAction branch : branches) {
            for (final AbstractBranchTransition t : branch.getBranches_Branch()) {
                ((ProbabilisticBranchTransition) t).setBranchProbability(0);
            }
        }
    }

    private void parametrizeBranchingProbabilities(final PCMParametrization parametrization) {
        this.resetBranchingProbabilities(parametrization);
        for (final Entry<AbstractBranchTransition, Integer> e : parametrization.getBranchTransitionMap().entrySet()) {
            // summarize invocation count of this transition and all sibling
            // transitions
            final List<AbstractBranchTransition> transitions = e.getKey().getBranchAction_AbstractBranchTransition()
                    .getBranches_Branch();
            int totalCount = 0;
            for (final AbstractBranchTransition t : transitions) {
                final int count = parametrization.getBranchTransitionMap().getOrDefault(t, 0);
                totalCount += count;
            }
            final double probability = parametrization.getBranchTransitionMap().get(e.getKey()).doubleValue()
                    / totalCount;
            ((ProbabilisticBranchTransition) e.getKey()).setBranchProbability(probability);
            e.getKey().setEntityName("Measured branch probability");
        }
    }

    private void parametrizeResourceDemandsWithMean(final PCMParametrization parametrization) {
        for (final Entry<InternalAction, List<Double>> e : parametrization.getResourceDemandMap().entrySet()) {
            final InternalAction action = e.getKey();
            final List<Double> demands = e.getValue();

            // calculate mean
            final double sum = demands.stream().mapToDouble(Double::doubleValue).sum();
            final double mean = sum / demands.size();

            // parametrize action
            final PCMRandomVariable rv = PCMHelper.createPCMRandomVariable(mean);
            action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
        }
    }

    private boolean addPalladioTXProfile(final PCMParametrization parametrization) {
        final InternalAction arbitraryRepositoryAction = parametrization.getResourceDemandMap().keySet().iterator()
                .next();
        final Resource repositoryResource = arbitraryRepositoryAction.eResource();

        boolean sucess = false;
        try {
            ProfileAPI.applyProfile(repositoryResource, "PCMTransactional");
            sucess = true;
        } catch (final RuntimeException e) {
            this.logger.info("Failed to apply PCMTransactional profile. Can not parameterize SQL statements. Reason: "
                    + e.toString(), e);
        }
        return sucess;
    }

    // TODO simplify whole method
    private void parametrizeSQLStatementsWithMean(final PCMParametrization parametrization) {
        if (parametrization.getSqlStatementMap().size() == 0 || !this.addPalladioTXProfile(parametrization)) {
            this.logger.warn("Can not parametrize SQL statements with mean.");
            return;
        }

        for (final Entry<InternalAction, List<SQLStatementSequence>> e : parametrization.getSqlStatementMap()
                .entrySet()) {
            final SQLStatementsToPCM sql2pcm = new SQLStatementsToPCM();

            // learn graph from paths (SQL statement sequences)
            final InternalAction action = e.getKey();
            final List<SQLStatementSequence> sequences = e.getValue();
            for (final SQLStatementSequence s : sequences) {
                sql2pcm.addStatementSequence(s);
            }
            final SPGraph g = sql2pcm.getLearner().getGraph();

            // create SEFF from graph (assumes "verbose" representation)
            final ResourceDemandingBehaviour rdb = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
            g.toVerboseRepresentation();
            g.traverse(new InvocationProbabilityVisitor());
            g.traverse(new Graph2SEFFVisitor(this.getPartition().getTrace()), rdb); // stores SEFF
                                                                                    // in rdb
            // variable

            // store trace as new blackboard partition

            PCMHelper.replaceAction(action, rdb);
        }
    }

}
