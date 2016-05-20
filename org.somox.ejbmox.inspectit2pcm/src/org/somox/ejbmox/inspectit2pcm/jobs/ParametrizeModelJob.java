package org.somox.ejbmox.inspectit2pcm.jobs;

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
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementsToPCM;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ParametrizeModelJob extends AbstractII2PCMJob {

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		logger.info("Storing monitored runtime behaviour to PCM model...");

		// TODO make configurable;
		AggregationStrategy aggregation = AggregationStrategy.MEAN;
		switch (aggregation) {
		case HISTOGRAM:
			throw new UnsupportedOperationException();
			// break;
		case MEAN:
			parametrizeResourceDemandsWithMean(getPartition().getParametrization());
			parametrizeSQLStatementsWithMean(getPartition().getParametrization());
			parametrizeBranchingProbabilities(getPartition().getParametrization());
			break;
		case MEDIAN:
			throw new UnsupportedOperationException();
			// break;
		default:
			throw new RuntimeException("Unknown aggregation strategy: " + aggregation);
		}
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// nothing to do
	}

	@Override
	public String getName() {
		return "Parametrize PCM Model";
	}

	private void resetBranchingProbabilities(PCMParametrization parametrization) {
		// collect all branches for which there is at least one branching
		// probability
		Set<BranchAction> branches = new HashSet<>();
		for (AbstractBranchTransition t : parametrization.getBranchTransitionMap().keySet()) {
			branches.add(t.getBranchAction_AbstractBranchTransition());
		}

		// reset branching probabilities
		for (BranchAction branch : branches) {
			for (AbstractBranchTransition t : branch.getBranches_Branch()) {
				((ProbabilisticBranchTransition) t).setBranchProbability(0);
			}
		}
	}

	private void parametrizeBranchingProbabilities(PCMParametrization parametrization) {
		resetBranchingProbabilities(parametrization);
		for (Entry<AbstractBranchTransition, Integer> e : parametrization.getBranchTransitionMap().entrySet()) {
			// summarize invocation count of this transition and all sibling
			// transitions
			List<AbstractBranchTransition> transitions = e.getKey().getBranchAction_AbstractBranchTransition()
					.getBranches_Branch();
			int totalCount = 0;
			for (AbstractBranchTransition t : transitions) {
				int count = parametrization.getBranchTransitionMap().getOrDefault(t, 0);
				totalCount += count;
			}
			double probability = parametrization.getBranchTransitionMap().get(e.getKey()).doubleValue() / totalCount;
			((ProbabilisticBranchTransition) e.getKey()).setBranchProbability(probability);
			e.getKey().setEntityName("Measured branch probability");
		}
	}

	private void parametrizeResourceDemandsWithMean(PCMParametrization parametrization) {
		for (Entry<InternalAction, List<Double>> e : parametrization.getResourceDemandMap().entrySet()) {
			InternalAction action = e.getKey();
			List<Double> demands = e.getValue();

			// calculate mean
			double sum = demands.stream().mapToDouble(Double::doubleValue).sum();
			double mean = sum / demands.size();

			// parametrize action
			PCMRandomVariable rv = PCMHelper.createPCMRandomVariable(mean);
			action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
		}
	}

	private void addPalladioTXProfile(PCMParametrization parametrization) {
		InternalAction arbitraryRepositoryAction = parametrization.getResourceDemandMap().keySet().iterator().next();
		Resource repositoryResource = arbitraryRepositoryAction.eResource();

		ProfileAPI.applyProfile(repositoryResource, "PCMTransactional");
	}

	// TODO simplify whole method
	private void parametrizeSQLStatementsWithMean(PCMParametrization parametrization) {
		addPalladioTXProfile(parametrization);

		for (Entry<InternalAction, List<SQLStatementSequence>> e : parametrization.getSqlStatementMap().entrySet()) {
			SQLStatementsToPCM sql2pcm = new SQLStatementsToPCM();

			// learn graph from paths (SQL statement sequences)
			InternalAction action = e.getKey();
			List<SQLStatementSequence> sequences = e.getValue();
			for (SQLStatementSequence s : sequences) {
				sql2pcm.addStatementSequence(s);
			}
			SPGraph g = sql2pcm.getLearner().getGraph();

			// create SEFF from graph (assumes "verbose" representation)
			ResourceDemandingBehaviour rdb = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
			g.toVerboseRepresentation();
			g.traverse(new InvocationProbabilityVisitor());
			g.traverse(new Graph2SEFFVisitor(), rdb); // stores SEFF in rdb
														// variable

			PCMHelper.replaceAction(action, rdb);
		}
	}

}
