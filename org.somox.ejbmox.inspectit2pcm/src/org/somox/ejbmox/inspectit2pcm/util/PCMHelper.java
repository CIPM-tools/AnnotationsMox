package org.somox.ejbmox.inspectit2pcm.util;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.SeffPackage;
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

	public static ExternalCallAction findNextExternalCall(AbstractAction startingPoint) {
		AbstractAction currentAction = startingPoint;
		while (currentAction.getSuccessor_AbstractAction() != null) {
			if (currentAction instanceof ExternalCallAction) {
				return (ExternalCallAction) currentAction;
			}
			currentAction = currentAction.getSuccessor_AbstractAction();
		}
		return null;
	}

	public static ExternalCallAction findFirstExternalCall(ResourceDemandingBehaviour behaviour) {
		return findNextExternalCall(findStartAction(behaviour));
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

	public static InternalAction createInternalActionStub(ResourceDemandingBehaviour container, String name) {
		InternalAction ia = SeffFactory.eINSTANCE.createInternalAction();
		ia.setResourceDemandingBehaviour_AbstractAction(container);
		ia.setEntityName(name);
		ia.getResourceDemand_Action().add(PCMHelper.createParametricResourceDemandCPU(0));
		return ia;
	}

	public static String entityToString(Entity entity) {
		return entity.eClass().getName() + ": " + entity.getEntityName() + " [" + entity.getId() + "]";
	}

	private static ResourceDemandingSEFF findSeffForBehaviour(ResourceDemandingBehaviour behaviour) {
		if (SeffPackage.eINSTANCE.getResourceDemandingSEFF().isInstance(behaviour)) {
			return (ResourceDemandingSEFF) behaviour;
		} else if (behaviour.getAbstractBranchTransition_ResourceDemandingBehaviour() != null) {
			AbstractBranchTransition transition = behaviour.getAbstractBranchTransition_ResourceDemandingBehaviour();
			BranchAction branch = transition.getBranchAction_AbstractBranchTransition();
			return findSeffForBehaviour(branch.getResourceDemandingBehaviour_AbstractAction()); 
		} else {
			throw new RuntimeException("Unexpected behaviour type: " + behaviour.eClass());
		}
	}

	public static ResourceDemandingSEFF findSeffForInternalAction(InternalAction action) {
		ResourceDemandingBehaviour behaviour = action.getResourceDemandingBehaviour_AbstractAction();
		return findSeffForBehaviour(behaviour);
	}
	
	public static void replaceAction(AbstractAction replaceAction, ResourceDemandingBehaviour behaviour) {
		// first collect all actions in a new ArrayList to avoid
		// ConcurrentModificationException thrown by EMF
		List<AbstractAction> insertActions = new ArrayList<>(behaviour.getSteps_Behaviour());
		for (AbstractAction insertAction : insertActions) {
			// ignore Start and Stop actions
			if (insertAction instanceof StartAction || insertAction instanceof StopAction) {
				continue;
			}
			insertAction.setResourceDemandingBehaviour_AbstractAction(
					replaceAction.getResourceDemandingBehaviour_AbstractAction());
		}

		AbstractAction predecessor = replaceAction.getPredecessor_AbstractAction();
		predecessor.setSuccessor_AbstractAction(PCMHelper.findStartAction(behaviour).getSuccessor_AbstractAction());

		AbstractAction successor = replaceAction.getSuccessor_AbstractAction();
		successor.setPredecessor_AbstractAction(PCMHelper.findStopAction(behaviour).getPredecessor_AbstractAction());

		// remove action that has been replaced
		replaceAction.setResourceDemandingBehaviour_AbstractAction(null);
	}

}
