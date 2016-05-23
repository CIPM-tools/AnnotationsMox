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
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		// instantiate REST service clients
		RESTClient client = new RESTClient(getPartition().getConfiguration().getCmrUrl());
		IdentsServiceClient identService = new IdentsServiceClient(client);
		InvocationsServiceClient invocationsService = new InvocationsServiceClient(client);

		// create mapper
		InvocationTree2PCMMapper mapper = new InvocationTree2PCMMapper(getPartition().getSeffToFQNMap());

		// create scanner and connect to mapper via scanning progress listener
		Set<String> externalServicesFQN = new HashSet<>(getPartition().getSeffToFQNMap().values());
		Set<String> interfacesFQN = new HashSet<>(getPartition().getInterfaceToFQNMap().values());
		InvocationTreeScanner scanner = new InvocationTreeScanner(mapper.getScanningProgressDispatcher(),
				externalServicesFQN, interfacesFQN, identService, invocationsService);

		// scan all invocation trees available
		II2PCMConfiguration config = getPartition().getConfiguration();
		List<Long> invocationIds = invocationsService.getInvocationSequencesId();
		int i = 0;
		logger.info("Skipping first " + config.getWarmupLength() + " invocation sequences treated as warmup phase");
		monitor.beginTask(getName(), invocationIds.size());
		for (long invocationId : invocationIds) {
			if (++i < config.getWarmupLength()) {
				continue;
			}
			InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
			logger.debug("Scanning invocation sequence " + i + " out of " + invocationIds.size() + "...");
			scanner.scanInvocationTree(invocation);
			monitor.worked(1);
		}

		// store resulting parametrization to blackboard
		PCMParametrization parametrization = mapper.getParametrization();
		getPartition().setParametrization(parametrization);
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// nothing to do
	}

	@Override
	public String getName() {
		return "Scan InspectIT invocation sequences";
	}

}
