package org.somox.ejbmox.inspectit2pcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffPackage;
import org.palladiosimulator.pcm.seff.StopAction;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Relates the events produced by a {@link InvocationTreeScanner} to SEFF
 * actions of a PCM model. This allows to collect, for instance, the execution
 * times of InternalActions. These are collected in a {@link PCMParametrization}
 * instance, and are finally used to parametrize the PCM model (
 * {@link #parametrize(AggregationStrategy)}).
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationTree2PCMMapper {

	private static final Logger logger = Logger.getLogger(InvocationTree2PCMMapper.class);

	private Map<ResourceDemandingSEFF, String> seffToFQNMap;

	private ScanningProgressDispatcher dispatcher = new ScanningProgressDispatcher();

	private PCMParametrization parametrization;

	static {
		// TODO use config file
		// Logger.getRootLogger().setLevel(Level.INFO);
	}

	public InvocationTree2PCMMapper(Map<ResourceDemandingSEFF, String> seffToFQNMap) {
		this.seffToFQNMap = seffToFQNMap;
		this.parametrization = new PCMParametrization();

		ensureInternalActionBeforeStopActions();
	}

	/**
	 * Insert one InternalAction before each SEFF's stop, if not present yet;
	 * serves as a placeholder for SQL statements issued at the end of the
	 * service invocation
	 */
	private void ensureInternalActionBeforeStopActions() {
		for (Entry<ResourceDemandingSEFF, String> entry : seffToFQNMap.entrySet()) {
			StopAction stop = PCMHelper.findStopAction(entry.getKey());
			AbstractAction stopPredecessor = stop.getPredecessor_AbstractAction();
			if (!(stopPredecessor instanceof InternalAction)) {
				InternalAction placeholder = PCMHelper.createInternalActionStub(
						stop.getResourceDemandingBehaviour_AbstractAction(),
						"Inserted by InspectIT2PCM processor @ " + entry.getValue());
				placeholder.setPredecessor_AbstractAction(stopPredecessor);
				stop.setPredecessor_AbstractAction(placeholder);
			}
		}
	}

	public ScanningProgressListener getScanningProgressDispatcher() {
		return dispatcher;
	}

	public PCMParametrization getParametrization() {
		return parametrization;
	}

	private static ResourceDemandingSEFF seffFromFQN(Map<ResourceDemandingSEFF, String> seffToFQNMap, String fqn) {
		try {
			return seffToFQNMap.entrySet().stream().filter(e -> e.getValue().equals(fqn)).findFirst().get().getKey();
		} catch (NoSuchElementException e) {
			throw new RuntimeException("Could not find SEFF with FQN " + fqn, e);
		}
	}

	private static boolean isSameService(ExternalCallAction firstExternalCall, MethodIdent calledService) {
		if (firstExternalCall == null) {
			return false;
		}
		OperationSignature signature = firstExternalCall.getCalledService_ExternalService();
		// TODO very hack solution right now!! check if interface/bean do
		// match
		if (calledService.getMethodName().equals(signature.getEntityName())) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isWithinSameService(InternalAction action, MethodIdent callingService,
			DetectionContext context) {
		ResourceDemandingSEFF seff = PCMHelper.findSeffForInternalAction(action);
		String seffFQN = context.seffToFQNMap.get(seff);

		boolean isSame = callingService.toFQN().equals(seffFQN);
		if (!isSame) {
			logErrorInContext("Expected internal action within SEFF " + seffFQN
					+ ", but encountered internal action in scope of " + callingService.toFQN(), context);
		}

		return isSame;
	}

	private static String message(String message, DetectionContext ctx) {
		return ctx.getShortName() + ": " + message;
	}

	private static void logErrorInContext(String message, DetectionContext ctx) {
		logger.error(message(message, ctx));
	}

	private static void logWarnInContext(String message, DetectionContext ctx) {
		logger.warn(message(message, ctx));
	}

	private static void logInfoInContext(String message, DetectionContext ctx) {
		logger.info(message(message, ctx));
	}

	private static void logDebugInContext(String message, DetectionContext ctx) {
		logger.debug(message(message, ctx));
	}

	private class ScanningProgressDispatcher implements ScanningProgressListener {

		private DetectionContext rootContext;

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			logger.debug("Dispatching begin of system call " + calledService.toFQN());

			if (rootContext != null) {
				throw new IllegalStateException(
						"Encountered begin of system call" + " although another system call is still being processed.");
			}
			ResourceDemandingBehaviour behaviour = seffFromFQN(seffToFQNMap, calledService.toFQN());
			DetectionContext parent = null;
			rootContext = new DetectionContext(calledService.toFQN(), parent, behaviour, parametrization, seffToFQNMap);

			// delegate to allow for setup operations
			rootContext.getDetector().systemCallEnd(calledService, time);
		}

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			logger.debug("Dispatching end of system call " + calledService.toFQN());

			// delegate to allow for clean up operations
			rootContext.getDetector().systemCallEnd(calledService, time);

			if (rootContext == null) {
				throw new IllegalStateException(
						"Encountered end of system call, but there is no system call being processed.");
			}
			rootContext = null;
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			logger.debug("Dispatching begin of external call " + calledService.toFQN() + ", called by "
					+ callingService.toFQN());
			rootContext.getDetector().externalCallBegin(callingService, calledService, time);
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			logger.debug("Dispatching end of external call " + calledService.toFQN() + ", called by "
					+ callingService.toFQN());
			rootContext.getDetector().externalCallEnd(callingService, calledService, time);
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			logger.debug("Dispatching begin of internal action, called by " + callingService.toFQN());
			rootContext.getDetector().internalActionBegin(callingService, time);
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			logger.debug("Dispatching end of internal action, called by " + callingService.toFQN());
			rootContext.getDetector().internalActionEnd(callingService, time);
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			logger.debug("Dispatching SQL statement, called by " + callingService.toFQN());
			rootContext.getDetector().sqlStatement(callingService, statement);
		}

	}

	private static class DetectionContext {

		private ResourceDemandingBehaviour behaviour;

		private DetectionContext parent;

		private AbstractAction expectedAction;

		private PCMParametrization parametrization;

		private ScanningProgressListener currentDetector;

		private DetectionContext nestedContext;

		private Map<ResourceDemandingSEFF, String> seffToFQNMap;

		private String name;

		public DetectionContext(String name, DetectionContext parent, ResourceDemandingBehaviour behaviour,
				PCMParametrization parametrization, Map<ResourceDemandingSEFF, String> seffToFQNMap) {
			this.name = name;
			this.parent = parent;
			this.behaviour = behaviour;
			this.parametrization = parametrization;
			this.seffToFQNMap = seffToFQNMap;

			// begin detection with Start action's successor
			expectedAction = PCMHelper.findStartAction(behaviour);
			proceedWithNextAction();
		}

		public void proceedWithNextAction() {
			expectNextAction(expectedAction.getSuccessor_AbstractAction());
		}

		private void expectNextAction(AbstractAction action) {
			logDebugInContext("Expecting next action " + PCMHelper.entityToString(action), this);
			this.expectedAction = action;
			if (SeffPackage.eINSTANCE.getExternalCallAction().isInstance(action)) {
				currentDetector = new DetectExternalCallAction((ExternalCallAction) action, this, seffToFQNMap);
			} else if (SeffPackage.eINSTANCE.getInternalAction().isInstance(action)) {
				currentDetector = new DetectInternalAction((InternalAction) action, this, seffToFQNMap);
			} else if (SeffPackage.eINSTANCE.getStopAction().isInstance(action)) {
				currentDetector = null;
				// currentDetector = new StopDetector(this);
			} else if (SeffPackage.eINSTANCE.getBranchAction().isInstance(action)) {
				currentDetector = new DetectBranchAction((BranchAction) action, this, seffToFQNMap);
			} else {
				logWarnInContext("Could not find extractor for AbstractActions of type " + action.getClass()
						+ ". Continuing with successor...", this);
				expectNextAction(action.getSuccessor_AbstractAction());
			}
		}

		public void leaveNestedContext() {
			logDebugInContext("Clearing nested context", this);
			nestedContext = null;
			proceedWithNextAction();
		}

		public void leaveContext() {
			parent.leaveNestedContext();
		}

		public boolean reachedStop() {
			return SeffPackage.eINSTANCE.getStopAction().isInstance(expectedAction);
		}

		public ScanningProgressListener getDetector() {
			if (nestedContext != null && nestedContext.getDetector() != null) {
				return nestedContext.getDetector();
			} else {
				return currentDetector;
			}
		}

		public AbstractAction getExpectedAction() {
			return expectedAction;
		}

		public PCMParametrization getParametrization() {
			return parametrization;
		}

		public String getName() {
			String parentNames = "";
			if (parent == null) {
				parentNames = "#";
			} else {
				parentNames = parent.getName();
			}
			return parentNames + " -> " + name;
		}

		public String getShortName() {
			String parentNames = "";
			if (parent == null) {
				parentNames = "#";
			} else {
				parentNames = parent.getShortName() + " -> ";
			}
			String[] nameSegments = name.split("\\.");
			String shortName = "";
			if (nameSegments.length <= 2) {
				shortName = name;
			} else {
				int n = nameSegments.length;
				shortName = nameSegments[n - 2] + "." + nameSegments[n - 1];
			}
			return parentNames + shortName;
		}

	}

	private static class DefaultDetector implements ScanningProgressListener {

		protected DetectionContext context;

		protected Map<ResourceDemandingSEFF, String> seffToFQNMap;

		public DefaultDetector(DetectionContext context, Map<ResourceDemandingSEFF, String> seffToFQNMap) {
			this.context = context;
			this.seffToFQNMap = seffToFQNMap;
		}

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			// nothing to do
		}

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			// nothing to do
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			logWarnInContext("Encountered unexpected begin of external call.", context);
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			logWarnInContext("Encountered unexpected end of external call.", context);
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			logWarnInContext("Encountered unexpected begin of internal action.", context);
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			logWarnInContext("Encountered unexpected end of internal action.", context);
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			logWarnInContext("Encountered unexpected SQL statement.", context);
		}

	}

	private static class DetectExternalCallAction extends DefaultDetector {

		private ExternalCallAction externalCall;

		public DetectExternalCallAction(ExternalCallAction externalCall, DetectionContext context,
				Map<ResourceDemandingSEFF, String> seffToFQNMap) {
			super(context, seffToFQNMap);
			this.externalCall = externalCall;
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			// TODO check if this is the expected call
			// super.externalCallBegin(callingService, calledService, time);

			boolean match = isSameService(externalCall, calledService);
			if (match) {
				logDebugInContext("Detected start of external call " + calledService.toFQN(), context);
				ResourceDemandingSEFF seff = seffFromFQN(seffToFQNMap, calledService.toFQN());
				// TODO improve following statement
				context.nestedContext = new DetectionContext(calledService.toFQN(), context, seff,
						context.getParametrization(), seffToFQNMap);
			} else {
				// TODO refine message
				String interfaceMethodFQN = externalCall.getCalledService_ExternalService()
						.getInterface__OperationSignature().getEntityName() + "."
						+ externalCall.getCalledService_ExternalService().getEntityName();
				logErrorInContext("Expected external call from " + callingService.toFQN() + " to interface method "
						+ interfaceMethodFQN + ", but got call to class " + calledService.toFQN(), context);
			}
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			// TODO check if this is the expected call
			boolean match = isSameService(externalCall, calledService);
			if (match) {
				logDebugInContext("Detected end of external call " + calledService.toFQN(), context);
				context.leaveNestedContext();
			} else {
				// TODO refine message
				String interfaceMethodFQN = externalCall.getCalledService_ExternalService()
						.getInterface__OperationSignature().getEntityName() + "."
						+ externalCall.getCalledService_ExternalService().getEntityName();
				logErrorInContext("Expected external call from " + callingService.toFQN() + " to interface method "
						+ interfaceMethodFQN + ", but got call to class " + calledService.toFQN(), context);
			}
		}

	}

	private static class DetectBranchAction extends DefaultDetector {

		private BranchAction branch;

		private List<DetectionContext> contextCandidates = new CopyOnWriteArrayList<>();

		private Map<DetectionContext, AbstractBranchTransition> transitionMap = new HashMap<>();

		public DetectBranchAction(BranchAction branch, DetectionContext context,
				Map<ResourceDemandingSEFF, String> seffToFQNMap) {
			super(context, seffToFQNMap);
			this.branch = branch;
			logDebugInContext("Trying to detect " + PCMHelper.entityToString(branch), context);

			// we don't know yet which branch transition will be taken, so
			// create a candidate mapper for each of them and later throw away
			// each matcher no longer appropriate in light of the advanced
			// scanning progress.
			int i = 0;
			for (AbstractBranchTransition t : branch.getBranches_Branch()) {
				ResourceDemandingBehaviour behaviour = t.getBranchBehaviour_BranchTransition();
				DetectionContext mapper = new DetectionContext("Transition_" + i, context, behaviour,
						(PCMParametrization) new PCMParametrization(), seffToFQNMap);
				contextCandidates.add(mapper);
				transitionMap.put(mapper, t);
				i++;
			}
		}

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			finalizeBranchDetection();
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			logDebugInContext("Dispatching external call to branch transition candidates: " + calledService.toFQN(),
					context);

			// delegate to all remaining candidates
			List<DetectionContext> removedCandidates = new ArrayList<>();
			for (DetectionContext ctx : contextCandidates) {
				if (!ctx.reachedStop()) {
					ctx.getDetector().externalCallBegin(callingService, calledService, time);
				} else {
					contextCandidates.remove(ctx);
					logDebugInContext("Removing context from candidates", ctx);
					removedCandidates.add(ctx);
				}
			}
			finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
		}

		private void finalizeBranchDetection() {
			if (contextCandidates.size() > 1) {
				logWarnInContext("Could not decide which branch transition to follow "
						+ "because more than one candidate is left. Choosing first candidate.", context);
			}
			finalizeBranchDetection(contextCandidates.get(0));
		}

		private void finalizeBranchDetection(DetectionContext chosenContext) {
			context.getParametrization().mergeFrom(chosenContext.parametrization);
			context.getParametrization().captureBranchTransition(transitionMap.get(chosenContext));
			context.proceedWithNextAction();
		}

		private boolean finalizeBranchDetectionIfNoRemainingCandidates(List<DetectionContext> removedCandidates) {
			if (contextCandidates.isEmpty()) {
				logDebugInContext("Finalizing branch detection.", context);
				if (removedCandidates.size() > 1) {
					logWarnInContext("Could not decide which branch transition to follow "
							+ "because more than one candidate is left. Choosing first candidate.", context);
				}

				// finalize branch detection
				DetectionContext chosenContext = removedCandidates.get(0);
				finalizeBranchDetection(chosenContext);

				return true; // finalized
			}
			return false; // not yet finalized
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			// delegate to all remaining candidates
			List<DetectionContext> removedCandidates = new ArrayList<>();
			for (DetectionContext ctx : contextCandidates) {
				if (!ctx.reachedStop()) {
					ctx.getDetector().externalCallEnd(callingService, calledService, time);
				} else {
					contextCandidates.remove(ctx);
					logDebugInContext("Removing context from candidates", ctx);
					removedCandidates.add(ctx);
				}
			}
			finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			// delegate to all remaining candidates
			List<DetectionContext> removedCandidates = new ArrayList<>();
			for (DetectionContext ctx : contextCandidates) {
				if (!ctx.reachedStop()) {
					ctx.getDetector().internalActionBegin(callingService, time);
				} else {
					contextCandidates.remove(ctx);
					logDebugInContext("Removing context from candidates", ctx);
					removedCandidates.add(ctx);
				}
			}
			finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			// delegate to all remaining candidates
			List<DetectionContext> removedCandidates = new ArrayList<>();
			for (DetectionContext ctx : contextCandidates) {
				if (!ctx.reachedStop()) {
					ctx.getDetector().internalActionEnd(callingService, time);
				} else {
					contextCandidates.remove(ctx);
					logDebugInContext("Removing context from candidates", ctx);
					removedCandidates.add(ctx);
				}
			}
			finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			// delegate to all remaining candidates
			List<DetectionContext> removedCandidates = new ArrayList<>();
			for (DetectionContext ctx : contextCandidates) {
				if (!ctx.reachedStop()) {
					ctx.getDetector().sqlStatement(callingService, statement);
				} else {
					contextCandidates.remove(ctx);
					logDebugInContext("Removing context from candidates", ctx);
					removedCandidates.add(ctx);
				}
			}
			finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
		}

	}

	private static class DetectInternalAction extends DefaultDetector {

		private double timeBegin;

		private SQLStatementSequence sqlStatements = new SQLStatementSequence();

		/** the action to be detected */
		private InternalAction action;

		public DetectInternalAction(InternalAction action, DetectionContext context,
				Map<ResourceDemandingSEFF, String> seffToFQNMap) {
			super(context, seffToFQNMap);
			this.action = action;
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			if (!isWithinSameService(action, callingService, context)) {
				context.leaveContext();
				// delegate to parent context
				context.parent.getDetector().internalActionBegin(callingService, time);
				return;
			}

			if (timeBegin != 0) {
				logWarnInContext(
						"Encountered consecutive begins of internal action without a corresponding end action.",
						context);
			}
			timeBegin = time;
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			if (!isWithinSameService(action, callingService, context)) {
				context.leaveContext();
				// delegate to parent context
				context.parent.getDetector().internalActionEnd(callingService, time);
				return;
			}

			if (timeBegin == 0) {
				logWarnInContext("Encountered end of internal action, but did not find corresponding begin action.",
						context);
			}
			double difference = time - timeBegin;
			logInfoInContext("Successfully detected internal action, execution time is " + difference, context);

			context.getParametrization().captureResourceDemand((InternalAction) action, difference);

			// capture SQL statements, if present
			if (sqlStatements.size() > 0) {
				context.getParametrization().captureSQLStatementSequence(action, sqlStatements);
				logInfoInContext("Detected " + sqlStatements.size() + " SQL statements within internal action.",
						context);
			}

			context.proceedWithNextAction();
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			logWarnInContext("Encountered unexpected begin of external call, thus skipping internal action detection "
					+ "and continuing with its successor (...that is hopefully an external action).", context);
			context.proceedWithNextAction();
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			sqlStatements.add(statement);
		}

	}

}
