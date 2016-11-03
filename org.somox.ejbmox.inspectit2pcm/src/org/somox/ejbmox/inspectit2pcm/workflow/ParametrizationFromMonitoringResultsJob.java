package org.somox.ejbmox.inspectit2pcm.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.ejbmox.inspectit2pcm.InvocationTree2PCMMapper;
import org.somox.ejbmox.inspectit2pcm.InvocationTreeScanner;
import org.somox.ejbmox.inspectit2pcm.ScanningProgressListener;
import org.somox.ejbmox.inspectit2pcm.launch.II2PCMConfiguration;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
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
        final II2PCMConfiguration config = this.getPartition().getConfiguration();

        // instantiate REST service clients
        final RESTClient client = new RESTClient(this.getPartition().getConfiguration().getCmrUrl());
        final IdentsServiceClient identService = new IdentsServiceClient(client);
        final InvocationsServiceClient invocationsService = new InvocationsServiceClient(client);

        // create mapper
        final InvocationTree2PCMMapper mapper = buildMapper();

        // create scanner and connect to mapper via scanning progress listener
        ScanningProgressListener listener = mapper.getScanningProgressDispatcher();
        Set<MethodIdent> methods = identService.listMethodIdents();
        final InvocationTreeScanner scanner = buildScanner(listener, methods);

        // obtain all invocation sequence ids
        final List<Long> invocationIds = invocationsService.getInvocationSequencesId();
        this.logger.info(String.format("Found %s invocation sequences.", invocationIds.size()));

        // log warmup phase infos
        this.logger.info(String.format("Skipping first %s invocation sequences treated as warmup phase",
                config.getWarmupLength()));
        if (config.getWarmupLength() > invocationIds.size()) {
            logger.warn("All available invocation sequences are considered to lie in the warmup phase. "
                    + "Reduce the warmup phase size or increase the number of available invocation sequences.");
        }

        monitor.beginTask(this.getName(), invocationIds.size());
        int i = 0;
        for (final long invocationId : invocationIds) {
            // skip invocations considered to belong to the warmup phase
            if (++i < config.getWarmupLength()) {
                // skip this iteration
                continue;
            }

            // consider every i-th invocation and skip the rest
            // TODO make configurable
            int SAMPLING_DISTANCE = 1; // consider all invocations
            if (i % SAMPLING_DISTANCE != 0) {
                // skip this iteration
                continue;
            }

            // scan invocation sequence
            final InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
            scanner.scanInvocationTree(invocation);
            monitor.worked(1);

            // log progress
            if (i % 100 == 0) {
                this.logger.info("Scanning invocation sequence " + i + " out of " + invocationIds.size()
                        + ". Invocation id = " + invocationId + ", start = " + invocation.getStart());
            }
        }

        // store resulting parametrization to blackboard
        final PCMParametrization parametrization = mapper.getParametrization();
        this.getPartition().setParametrization(parametrization);
    }

    private InvocationTreeScanner buildScanner(ScanningProgressListener listener, Set<MethodIdent> methods) {
        final Set<String> externalServicesFQN = new HashSet<>(this.getPartition().getSeffToFQNMap().values());
        final Set<String> interfacesFQN = new HashSet<>(this.getPartition().getInterfaceToFQNMap().values());
        final InvocationTreeScanner scanner = new InvocationTreeScanner(listener, externalServicesFQN, interfacesFQN,
                methods);
        return scanner;
    }

    private InvocationTree2PCMMapper buildMapper() {
        final boolean ensureInternalActionsBeforeSTOPAction = this.getPartition().getConfiguration()
                .isEnsureInternalActionsBeforeSTOPAction();
        final InvocationTree2PCMMapper mapper = new InvocationTree2PCMMapper(this.getPartition().getSeffToFQNMap(),
                ensureInternalActionsBeforeSTOPAction);
        return mapper;
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
