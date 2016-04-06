package org.somox.ejbmox.inspectit2pcm;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.RESTClient;

/**
 * Extracts information on runtime (timing) behaviour from invocation sequences
 * measured with InspectIT, and uses them to parametrize a PCM model.
 * 
 * @author Philipp Merkle
 *
 */
public class InspectIT2PCM {

	private IdentsServiceClient identService;

	private InvocationsServiceClient invocationsService;

	public InspectIT2PCM(InspectIT2PCMConfiguration config) {
		RESTClient client = new RESTClient(config.getRestUrl());

		// instantiate REST service clients
		identService = new IdentsServiceClient(client);
		invocationsService = new InvocationsServiceClient(client);
	}

	public void parametrizeSEFFs(Map<ResourceDemandingSEFF, String> seffToFQNMap) {
		InvocationTree2PCMMapper mapper = new InvocationTree2PCMMapper(seffToFQNMap);
		Set<String> externalServicesFQN = new HashSet<>(seffToFQNMap.values());
		InvocationTreeScanner scanner = new InvocationTreeScanner(mapper.getScannerListener(), externalServicesFQN,
				identService, invocationsService);

		// scan all invocation trees available
		List<Long> invocationIds = invocationsService.getInvocationSequencesId();
		for (long invocationId : invocationIds) {
			InvocationSequence invocation = invocationsService.getInvocationSequence(invocationId);
			scanner.scanInvocationTree(invocation);
		}

		// parametrize PCM model
		mapper.parametrize(AggregationStrategy.MEAN);
	}

}
