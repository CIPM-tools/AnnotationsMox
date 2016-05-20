package org.somox.ejbmox.inspectit2pcm.jobs;

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

public class ParametrizationFromMonitoringResults extends AbstractII2PCMJob {

	@Override
	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {

		// instantiate REST service clients
		RESTClient client = new RESTClient(getPartition().getConfiguration().getCmrUrl());
		IdentsServiceClient identService = new IdentsServiceClient(client);
		InvocationsServiceClient invocationsService = new InvocationsServiceClient(client);

		// TODO: comment
		InvocationTree2PCMMapper mapper = new InvocationTree2PCMMapper(getPartition().getSeffToFQNMap());
		Set<String> externalServicesFQN = new HashSet<>(getPartition().getSeffToFQNMap().values());
		Set<String> interfacesFQN = new HashSet<>(getPartition().getInterfaceToFQNMap().values());
		InvocationTreeScanner scanner = new InvocationTreeScanner(mapper.getScanningProgressDispatcher(),
				externalServicesFQN, interfacesFQN, identService, invocationsService);

		// scan all invocation trees available
		II2PCMConfiguration config = getPartition().getConfiguration();
		List<Long> invocationIds = invocationsService.getInvocationSequencesId();
		int i = 0;
		for (long invocationId : invocationIds) {
			if (++i < config.getWarmupLength()) {
				continue;
			}
			InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
			logger.info("Scanning invocation sequence " + (i - config.getWarmupLength()) + " out of "
					+ (invocationIds.size() - config.getWarmupLength()) + "...");
			scanner.scanInvocationTree(invocation);
		}

		// store resulting parametrization to blackboard
		PCMParametrization parametrization = mapper.getParametrization();
		getPartition().setParametrization(parametrization);
	}

	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		return "Parse InspectIT Measurements";
	}

}
