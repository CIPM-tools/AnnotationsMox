package org.somox.ejbmox.inspectit2pcm.workflow;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.ejbmox.inspectit2pcm.InvocationTree2PCMMapper;
import org.somox.ejbmox.inspectit2pcm.InvocationTreeScanner;
import org.somox.ejbmox.inspectit2pcm.ScanningProgressListener;
import org.somox.ejbmox.inspectit2pcm.anomalies.AnomalyDetection;
import org.somox.ejbmox.inspectit2pcm.launch.II2PCMConfiguration;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.ParametrizationExporter;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsProvider;
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
        int agentId = this.getPartition().getConfiguration().getAgentId();
        final RESTClient client = new RESTClient(this.getPartition().getConfiguration().getCmrUrl());
        final IdentsServiceClient identService = new IdentsServiceClient(client, agentId);

        final InvocationsServiceClient invocationsService = new InvocationsServiceClient(client, agentId);
        InvocationsProvider invocations = InvocationsProvider.fromService(invocationsService);
        this.logger.info(String.format("Found %s invocation sequences.", invocations.size()));

        // remove invocation sequences belonging to warmup phase
        this.logger.info(String.format("Removing first %s invocation sequences treated as warmup phase",
                config.getWarmupLength()));
        invocations = invocations.removeWarmup(config.getWarmupLength());
        if (invocations.size() == 0) {
            logger.warn("There are no invocation sequences or all are considered to belong to the warmup phase.");
        }

        // create mapper
        final InvocationTree2PCMMapper mapper = buildMapper();

        // create scanner and connect to mapper via scanning progress listener
        ScanningProgressListener listener = mapper.getScanningProgressDispatcher();
        Set<MethodIdent> methods = identService.listMethodIdents();
        final InvocationTreeScanner scanner = buildScanner(listener, methods);

        // detect and remove anomalies if desired
        boolean removeAnomalies = this.getPartition().getConfiguration().isRemoveAnomalies();
        if (removeAnomalies) {
            Collection<Long> anomalousInvocationIds = detectAnomalies(invocations);
            invocations = invocations.remove(anomalousInvocationIds);
        }

        scanInvocations(invocations, scanner, monitor);

        PCMParametrization parametrization = mapper.getParametrization();
        
        logger.info("Exporting parametrization to R...");
        ParametrizationExporter exporter = new ParametrizationExporter(parametrization);
        exporter.exportToR();

        // store resulting parametrization to blackboard
        this.getPartition().setParametrization(parametrization);
    }

    private Collection<Long> detectAnomalies(InvocationsProvider invocations) {
        logger.info("Detecting anomalies...");

        // request invocation overviews
        invocations.setDetailed(false);

        AnomalyDetection anomalyDetection = new AnomalyDetection(invocations);
        Set<Long> anomalies = anomalyDetection.detect();

        logger.info(String.format("Deteced %s anomalous measurements", anomalies.size())); // outlierCount
        return anomalies;
    }

    private void scanInvocations(InvocationsProvider invocations, final InvocationTreeScanner scanner,
            final IProgressMonitor monitor) {
        // request entire invocation trees
        invocations.setDetailed(true);

        monitor.beginTask(this.getName(), invocations.size());
        int i = 0;
        InvocationSequence lastInvocation = null;
        for (InvocationSequence invocation : invocations) {
            i++;

            // consider every i-th invocation and skip the rest
            // TODO make configurable
            int SAMPLING_DISTANCE = 1; // consider all invocations
            if (i % SAMPLING_DISTANCE != 0) {
                // skip this iteration
                continue;
            }

            // scan invocation sequence
            scanner.scanInvocationTree(invocation);
            monitor.worked(1);

            // log progress
            Date invocationTime = new Date(invocation.getTimeStamp().getTime());
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a zzz");
            if (i % 100 == 0) {
                this.logger.info(
                        String.format("Scanning invocation sequence %s out of %s. Invocation start timestamp = %s", i,
                                invocations.size(), ft.format(invocationTime)));
            }

            if (lastInvocation != null && invocation.getStart() < lastInvocation.getEnd()) {
                throw new RuntimeException(
                        "Inovation sequences obtained via REST service seem to be in the wrong order.");
            }
            lastInvocation = invocation;
        }
    }

    private InvocationTreeScanner buildScanner(ScanningProgressListener listener, Set<MethodIdent> methods) {
        final Set<String> externalServicesFQN = new HashSet<>(this.getPartition().getSeffToFQNMap().values());
        final InvocationTreeScanner scanner = new InvocationTreeScanner(listener, externalServicesFQN, methods);
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
