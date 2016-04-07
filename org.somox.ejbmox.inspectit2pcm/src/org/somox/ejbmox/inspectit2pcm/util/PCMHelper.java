package org.somox.ejbmox.inspectit2pcm.util;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.seff_performance.ResourceCall;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;

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

	public static PCMRandomVariable createPCMRandomVariable(int count) {
		PCMRandomVariable rv = CoreFactory.eINSTANCE.createPCMRandomVariable();
		rv.setSpecification(Integer.toString(count));
		return rv;
	}
	
	public static PCMRandomVariable createPCMRandomVariable(double demand) {
		PCMRandomVariable rv = CoreFactory.eINSTANCE.createPCMRandomVariable();
		rv.setSpecification(Double.toString(demand));
		return rv;
	}

	public static StartAction findStartAction(ResourceDemandingSEFF seff) {
		return (StartAction) seff.getSteps_Behaviour().stream().filter(a -> a instanceof StartAction).findFirst().get();
	}
	
	public static void insertSQLStatementAsResourceCall(InternalAction action, SQLStatement stmt) {
		ResourceCall call = SeffPerformanceFactory.eINSTANCE.createResourceCall();
		call.setEntityName(stmt.getSql());
		call.setNumberOfCalls__ResourceCall(createPCMRandomVariable(1));
		// TODO set resource interface 
		action.getResourceCall__Action().add(call);
	}

}
