package org.somox.ejbmox.inspectit2pcm.util;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;
import org.palladiosimulator.pcm.seff.seff_performance.ResourceCall;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;
import org.somox.analyzer.simplemodelanalyzer.builder.util.DefaultResourceEnvironment;
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

	public static ParametricResourceDemand createParametricResourceDemandCPU(double demand) {
		ParametricResourceDemand prd = SeffPerformanceFactory.eINSTANCE.createParametricResourceDemand();
		prd.setSpecification_ParametericResourceDemand(createPCMRandomVariable(demand));
		
		ProcessingResourceType cpu = DefaultResourceEnvironment.getCPUProcessingResourceType();
		prd.setRequiredResource_ParametricResourceDemand(cpu);
		
		return prd;
	}

	public static StartAction findStartAction(ResourceDemandingBehaviour behaviour) {
		return (StartAction) behaviour.getSteps_Behaviour().stream().filter(a -> a instanceof StartAction).findFirst()
				.get();
	}

	public static StopAction findStopAction(ResourceDemandingBehaviour behaviour) {
		return (StopAction) behaviour.getSteps_Behaviour().stream().filter(a -> a instanceof StopAction).findFirst()
				.get();
	}

	public static void insertSQLStatementAsResourceCall(InternalAction action, SQLStatement stmt) {
		ResourceCall call = SeffPerformanceFactory.eINSTANCE.createResourceCall();
		call.setEntityName(stmt.getSql());
		call.setNumberOfCalls__ResourceCall(createPCMRandomVariable(1));
		// TODO set resource interface
		action.getResourceCall__Action().add(call);
	}

}
