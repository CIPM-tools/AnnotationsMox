package org.somox.ejbmox.inspectit2pcm.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.ejbmox.inspectit2pcm.II2PCMConfiguration;
import org.somox.ejbmox.inspectit2pcm.InvocationTree2PCMMapper;
import org.somox.ejbmox.inspectit2pcm.InvocationTreeScanner;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.RESTClient;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class ParametrizationFromMonitoringResultsJob extends AbstractII2PCMJob {

    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        // instantiate REST service clients
        final RESTClient client = new RESTClient(this.getPartition().getConfiguration().getCmrUrl());
        final IdentsServiceClient identService = new IdentsServiceClient(client);
        final InvocationsServiceClient invocationsService = new InvocationsServiceClient(client);
        final boolean ensureInternalActionsBeforeSTOPAction = this.getPartition().getConfiguration()
                .isEnsureInternalActionsBeforeSTOPAction();

        // create mapper
        final InvocationTree2PCMMapper mapper = new InvocationTree2PCMMapper(this.getPartition().getSeffToFQNMap(),
                ensureInternalActionsBeforeSTOPAction);

        // create scanner and connect to mapper via scanning progress listener
        final Set<String> externalServicesFQN = new HashSet<>(this.getPartition().getSeffToFQNMap().values());
        final Set<String> interfacesFQN = new HashSet<>(this.getPartition().getInterfaceToFQNMap().values());
        final InvocationTreeScanner scanner = new InvocationTreeScanner(mapper.getScanningProgressDispatcher(),
                externalServicesFQN, interfacesFQN, identService, invocationsService);

        // scan all invocation trees available
        final II2PCMConfiguration config = this.getPartition().getConfiguration();
        final List<Long> invocationIds = invocationsService.getInvocationSequencesId();
        this.logger.info("Found " + invocationIds.size() + " invocation sequences.");
        this.logger
                .info("Skipping first " + config.getWarmupLength() + " invocation sequences treated as warmup phase");

        if (config.getWarmupLength() > invocationIds.size()) {
            logger.warn("All available invocation sequences are considered to lie in the warmup phase. "
                    + "Reduce the warmup phase size or increase the number of available invocation sequences.");
        }

        monitor.beginTask(this.getName(), invocationIds.size());
        int i = 0;
        for (final long invocationId : invocationIds) {
            if (++i < config.getWarmupLength()) {
                continue;
            }
            final InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
            if (i % 100 == 0) {
                this.logger.info("Scanning invocation sequence " + i + " out of " + invocationIds.size() + ". Invocation id = " + invocationId + ", start = " + invocation.getStart());
            }
            scanner.scanInvocationTree(invocation);
            monitor.worked(1);
        }

        // store resulting parametrization to blackboard
        final PCMParametrization parametrization = mapper.getParametrization();
        this.getPartition().setParametrization(parametrization);
    }

    @Override
    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Scan InspectIT invocation sequences";
    }

}
