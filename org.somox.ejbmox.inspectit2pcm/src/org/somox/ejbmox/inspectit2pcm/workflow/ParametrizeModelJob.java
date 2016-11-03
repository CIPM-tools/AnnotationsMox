package org.somox.ejbmox.inspectit2pcm.workflow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.mdsdprofiles.api.ProfileAPI;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.inspectit2pcm.graphlearner.Graph2SEFFVisitor;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.DistributionAggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.InternalActionInvocation;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.ParametrizationTrace;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementsToPCM;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ParametrizeModelJob extends AbstractII2PCMJob {

    // TODO make configurable via launch configuration
    private final static boolean SAVE_DEBUG_OUTPUT = true;

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        this.logger.info("Storing monitored runtime behaviour to PCM model...");

        final PCMParametrization parametrization = this.getPartition().getParametrization();
        if (parametrization.isEmpty()) {
            logger.warn("Could not find any parametrization information. Skipping parametrization.");
            return;
        }

        // TODO make configurable
        final AggregationStrategy aggregation = new DistributionAggregationStrategy();
        // final AggregationStrategy aggregation = new MeanAggregationStrategy();
        boolean refineSQL = this.getPartition().getConfiguration().isRefineSQLStatements();

        if (refineSQL) {
            // try adding Palladio.TX profile to PCM Repository
            InternalAction arbitraryInternalAction = parametrization.getInternalActionMap().keySet().iterator().next();
            final Resource repositoryResource = arbitraryInternalAction.eResource();
            boolean profileAddedSuccessfully = this.addPalladioTXProfileToRepositoryModel(repositoryResource);
            if (!profileAddedSuccessfully) {
                this.logger.warn(
                        "Could not add Palladio.TX profile to repository model, skipping extraction of SQL statements");
                refineSQL = false;
            }
        } else {
            logger.info("Skipping extraction of SQL statements");
        }

        logger.info("Parameterizing Internal Actions with resource demands...");
        this.parametrizeInternalActions(parametrization, aggregation, refineSQL);

        logger.info("Parameterizing Branches with branching probabilities...");
        this.parametrizeBranchingProbabilities(parametrization);
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

    private void parametrizeInternalActions(final PCMParametrization parametrization,
            AggregationStrategy aggregationStrategy, boolean refineSQL) {
        // check assumption used below
        PCMHelper.ensureUniqueKeys(parametrization.getInternalActionMap());

        for (final Entry<InternalAction, List<InternalActionInvocation>> e : parametrization.getInternalActionMap()
                .entrySet()) {
            final InternalAction action = e.getKey();
            final List<InternalActionInvocation> invocations = e.getValue();

            logger.info("Calculating resource demand for " + PCMHelper.entityToString(action) + " from "
                    + invocations.size() + " observed invocations");

            // if there is a demand > 0 already, something went wrong
            String existingDemand = action.getResourceDemand_Action().get(0)
                    .getSpecification_ParametericResourceDemand().getSpecification();
            if (!(existingDemand.equals("0") || existingDemand.equals("0.0"))) {
                throw new RuntimeException("Expecting demand '0' or '0.0'  for " + PCMHelper.entityToString(action)
                        + ", but demand is " + existingDemand);
            }

            if (!refineSQL) {
                parametrizeInternalActionWithoutSQL(aggregationStrategy, action, invocations);
            } else {
                parametrizeInternalActionWithSQL(aggregationStrategy, action, invocations);
            }
        }

        if (SAVE_DEBUG_OUTPUT) {
            dumpParametrizationToFile(parametrization);
        }
    }

    private void parametrizeInternalActionWithoutSQL(AggregationStrategy aggregationStrategy,
            final InternalAction action, final List<InternalActionInvocation> invocations) {
        // perform desired aggregation and obtain PCM random variable
        List<Double> durations = InternalActionInvocation.selectDurations(invocations);
        PCMRandomVariable resourceDemand = aggregationStrategy.aggregate(durations);

        // parametrize action
        boolean SPLIT = false; // TODO make configurable
        double CPU_FRACTION = 0.6; // TODO make configurable

        if (SPLIT) {
            splitResourceDemandBetweenCpuAndDelay(action, resourceDemand, CPU_FRACTION);
        } else {
            assignResourceDemandEntirelyToCpu(action, resourceDemand);
        }
    }

    private void splitResourceDemandBetweenCpuAndDelay(InternalAction action, PCMRandomVariable resourceDemand,
            double cpuFraction) {
        // create CPU resource demand
        String cpuStoEx = resourceDemand.getSpecification() + "*" + Double.toString(cpuFraction);
        PCMRandomVariable cpuResourceDemand = PCMHelper.createPCMRandomVariable(cpuStoEx);

        // create DELAY resource demand
        double delayFraction = 1 - cpuFraction;
        String delayStoEx = resourceDemand.getSpecification() + "*" + Double.toString(delayFraction);
        PCMRandomVariable delayResourceDemand = PCMHelper.createPCMRandomVariable(delayStoEx);

        // assign resource demands to action
        action.getResourceDemand_Action().add(PCMHelper.createParametricResourceDemandCPU(cpuResourceDemand));
        action.getResourceDemand_Action().add(PCMHelper.createParametricResourceDemandDELAY(delayResourceDemand));
    }

    private void assignResourceDemandEntirelyToCpu(InternalAction action, PCMRandomVariable resourceDemand) {
        action.getResourceDemand_Action().clear();
        action.getResourceDemand_Action().add(PCMHelper.createParametricResourceDemandCPU(resourceDemand));
    }

    private void dumpParametrizationToFile(final PCMParametrization parametrization) {
        // create URI to debug file, which may be platform specific, i.e. of the form platform:/...
        URI debugFileUri = createDebugFileURI();

        // transform platform-relative path to file system path
        URL resolvedUrl = resolveURI(debugFileUri);

        File f = new File(resolvedUrl.getFile());
        parametrization.saveToFile(f);
    }

    private URI createDebugFileURI() {
        String outputFolder = (String) getPartition().getConfiguration().getAttributes()
                .get(AbstractMoxConfiguration.SOMOX_OUTPUT_FOLDER);
        URI outputFolderUri = URI.createPlatformResourceURI(outputFolder, true);
        URI debugFileUri = outputFolderUri.appendSegment("ejbmox.ii2pcm.parametrization.txt");
        return debugFileUri;
    }

    private URL resolveURI(URI debugFileUri) {
        URL resolvedUrl;
        try {
            URL url = new URL(debugFileUri.toString());
            resolvedUrl = FileLocator.resolve(url);
        } catch (IOException e) {
            throw new RuntimeException("Could not resolve URL " + debugFileUri.toString(), e);
        }
        return resolvedUrl;
    }

    private boolean addPalladioTXProfileToRepositoryModel(Resource repositoryResource) {
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

    private void parametrizeInternalActionWithSQL(AggregationStrategy aggregationStrategy, final InternalAction action,
            final List<InternalActionInvocation> invocations) {
        if (InternalActionInvocation.selectNonEmptySQLSequences(invocations).isEmpty()) {
            parametrizeInternalActionWithoutSQL(aggregationStrategy, action, invocations);
            return;
        }

        // learn graph from paths (SQL statement sequences)
        final SQLStatementsToPCM sql2pcm = new SQLStatementsToPCM();
        final List<SQLStatementSequence> sequences = InternalActionInvocation.selectNonEmptySQLSequences(invocations);
        for (final SQLStatementSequence s : sequences) {
            sql2pcm.addStatementSequence(s);
        }
        final SPGraph g = sql2pcm.getLearner().getGraph();

        // create SEFF from graph (assumes "verbose" representation)
        final ResourceDemandingBehaviour rdb = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
        g.toVerboseRepresentation();
        g.traverse(new InvocationProbabilityVisitor());

        // visitor stores resulting SEFF in rdb variable as side effect
        ParametrizationTrace trace = this.getPartition().getTrace();
        Graph2SEFFVisitor visitor = new Graph2SEFFVisitor(trace, aggregationStrategy);
        g.traverse(visitor, rdb);

        // TODO make configurable?
        final boolean KEEP_REPLACE_ACTION = true;
        PCMHelper.replaceAction(action, rdb, KEEP_REPLACE_ACTION);

        if (KEEP_REPLACE_ACTION) {
            // perform desired aggregation and obtain PCM random variable
            List<Double> exclusiveDurations = InternalActionInvocation.selectDurationsWithoutSQL(invocations);
            PCMRandomVariable rv = aggregationStrategy.aggregate(exclusiveDurations);

            // adjust resource demand of replace action
            action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
        }

    }

}
