package org.somox.ejbmox.inspectit2pcm.workflow;

import java.util.ArrayList;
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
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
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
            if (this.getPartition().getConfiguration().isRefineSQLStatements()) {
                this.parametrizeSQLStatementsWithMean(this.getPartition().getParametrization());
            } else {
                logger.info("Skipping extraction of SQL statements");
            }
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

    /**
     * Sets branching probabilities to "0" for all branches that are logged in the given
     * parametrization and contain at least one branch transition.
     * 
     * @param parametrization
     */
    private void resetBranchingProbabilities(final PCMParametrization parametrization) {
        // collect all branches with at least one branch transition
        final Set<BranchAction> branches = new HashSet<>();
        for (final AbstractBranchTransition t : parametrization.getBranchTransitionMap().keySet()) {
            branches.add(t.getBranchAction_AbstractBranchTransition());
        }

        // reset branching probabilities to "0"
        for (final BranchAction branch : branches) {
            for (final AbstractBranchTransition t : branch.getBranches_Branch()) {
                ((ProbabilisticBranchTransition) t).setBranchProbability(0);
            }
        }
    }

    private void parametrizeBranchingProbabilities(final PCMParametrization parametrization) {
        // set branching probabilities to "0"
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
//        parametrization.saveToFile();

        // check assumption used below
        PCMHelper.ensureUniqueKeys(parametrization.getResourceDemandMap());

        for (final Entry<InternalAction, List<Double>> e : parametrization.getResourceDemandMap().entrySet()) {
            final InternalAction action = e.getKey();
            final List<Double> demands = e.getValue();

            // calculate mean
            final double mean = calculateMean(demands);

            // if there is a demand > 0 already, something went wrong
            String existingDemand = action.getResourceDemand_Action().get(0)
                    .getSpecification_ParametericResourceDemand().getSpecification();
            if (!(existingDemand.equals("0") || existingDemand.equals("0.0"))) {
                throw new RuntimeException("Expecting demand '0' or '0.0'  for " + PCMHelper.entityToString(action)
                        + ", but demand is " + existingDemand);
            }

            // parametrize action
            final PCMRandomVariable rv = PCMHelper.createPCMRandomVariable(mean);
            action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
        }
    }

    private double calculateMean(List<Double> demands) {
        double sum = demands.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / demands.size();
        return mean;
    }

    private boolean addPalladioTXProfileToRepositoryModel(final PCMParametrization parametrization) {
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
        boolean profileAddedSuccessfully = this.addPalladioTXProfileToRepositoryModel(parametrization);
        if (parametrization.getSqlStatementMap().size() == 0) {
            this.logger.warn("Did not encounter any SQL statements while extracting SQL statements");
            return;
        }
        if (!profileAddedSuccessfully) {
            this.logger.warn(
                    "Could not add Palladio.TX profile to repository model, skipping extraction of SQL statements");
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

            // visitor stores resulting SEFF in rdb variable as side effect
            g.traverse(new Graph2SEFFVisitor(this.getPartition().getTrace()), rdb);

            boolean keepReplaceAction = true;
            PCMHelper.replaceAction(action, rdb, keepReplaceAction);
            
            if (keepReplaceAction) {
                // calculate adjusted resource demand of replace action
                double unadjustedMeanDemand = calculateMean(parametrization.getResourceDemandMap().get(action));
                double meanDemandCausedBySQLs = calculateMeanDemandOfSQLStatementSequence(sequences);
                double adjustedDemand = unadjustedMeanDemand - meanDemandCausedBySQLs; // should be
                                                                                       // > 0

                // adjust resource demand of replace action
                final PCMRandomVariable rv = PCMHelper.createPCMRandomVariable(adjustedDemand);
                action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
            }
        }
    }

    private double calculateMeanDemandOfSQLStatementSequence(List<SQLStatementSequence> sequences) {
        // contains the cumulative demand ("duration") for each SQLStatementSequence contained in
        // sequences
        List<Double> demands = new ArrayList<>();
        for (SQLStatementSequence sequence : sequences) {
            // TODO move to class SQLStatementSequence?
            double sumOfDuration = 0;
            for (SQLStatement stmt : sequence.getSequence()) {
                sumOfDuration += stmt.getDuration();
            }
            demands.add(sumOfDuration);
        }
        double mean = calculateMean(demands);
        return mean;
    }

}
