package org.somox.ejbmox.inspectit2pcm.util;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;

/**
 * Static helper methods to simplify working with PCM models.
 * 
 * @author Philipp Merkle
 *
 */
public class PCMHelper {

	private PCMHelper() {
		// do not instantiate
	}

	public static PCMRandomVariable createPCMRandomVariable(double demand) {
		PCMRandomVariable rv = CoreFactory.eINSTANCE.createPCMRandomVariable();
		rv.setSpecification(Double.toString(demand));
		return rv;
	}

	public static StartAction findStartAction(ResourceDemandingSEFF seff) {
		return (StartAction) seff.getSteps_Behaviour().stream().filter(a -> a instanceof StartAction).findFirst().get();
	}

}
