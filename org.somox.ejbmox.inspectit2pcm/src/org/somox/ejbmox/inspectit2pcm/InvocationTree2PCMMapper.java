package org.somox.ejbmox.inspectit2pcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
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
import org.palladiosimulator.pcm.seff.StartAction;
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

	private PCMParametrization parametrization;

	private ScannerListenerDelegator detector = new ScannerListenerDelegator();

	private ResourceDemandingBehaviour behaviour;

	private AbstractAction expectedAction;

	private Map<ResourceDemandingSEFF, String> seffToFQNMap;

	static {
		// TODO use config file
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	private InvocationTree2PCMMapper(Map<ResourceDemandingSEFF, String> seffToFQNMap,
			ResourceDemandingBehaviour behaviour, PCMParametrization parametrization) {
		this(seffToFQNMap);
		this.behaviour = behaviour;
		this.parametrization = parametrization;

		// insert one InternalAction before each SEFF's stop, if not present
		// yet; serves as a placeholder for SQL statements issued at the end of
		// the service invocation
		for (ResourceDemandingSEFF seff : seffToFQNMap.keySet()) {
			StopAction stop = PCMHelper.findStopAction(seff);
			AbstractAction stopPredecessor = stop.getPredecessor_AbstractAction();
			if (!(stopPredecessor instanceof InternalAction)) {
				InternalAction placeholder = PCMHelper.createInternalActionStub(
						stop.getResourceDemandingBehaviour_AbstractAction(),
						"Inserted by InspectIT2PCM processor");
				placeholder.setPredecessor_AbstractAction(stopPredecessor);
				stop.setPredecessor_AbstractAction(placeholder);
			}
		}

		// begin detection with Start action's successor
		StartAction startAction = PCMHelper.findStartAction(behaviour);
		expectNextAction(startAction.getSuccessor_AbstractAction());
	}

	public InvocationTree2PCMMapper(Map<ResourceDemandingSEFF, String> seffToFQNMap) {
		this.seffToFQNMap = seffToFQNMap;
		this.detector.setDetectionType(new DetectSystemCall());
		this.parametrization = new PCMParametrization();
	}

	public void expectNextAction(AbstractAction action) {
		// logger.debug("Expecting action of type " + action.eClass());
		this.expectedAction = action;
		if (SeffPackage.eINSTANCE.getExternalCallAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectExternalCallAction());
		} else if (SeffPackage.eINSTANCE.getInternalAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectInternalAction());
		} else if (SeffPackage.eINSTANCE.getStopAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectStopAction());
		} else if (SeffPackage.eINSTANCE.getBranchAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectBranchAction());
		} else {
			logger.warn("Could not find extractor for AbstractActions of type " + expectedAction.getClass()
					+ ". Continuing with successor...");
			expectNextAction(action.getSuccessor_AbstractAction());
		}
	}

	public void parametrize(AggregationStrategy aggregation) {
		parametrization.parametrize(aggregation);
	}

	public ScanningProgressListener getScannerListener() {
		return detector;
	}

	public PCMParametrization getParametrization() {
		return parametrization;
	}

	public ResourceDemandingBehaviour getBehaviour() {
		return behaviour;
	}

	public AbstractAction getExpectedAction() {
		return expectedAction;
	}

	private ResourceDemandingSEFF seffFromFQN(String fqn) {
		try {
			return seffToFQNMap.entrySet().stream().filter(e -> e.getValue().equals(fqn)).findFirst().get().getKey();
		} catch (NoSuchElementException e) {
			throw new RuntimeException("Could not find SEFF with FQN " + fqn, e);
		}
	}

	private static class ScannerListenerDelegator implements ScanningProgressListener {

		private ScanningProgressListener delegatee;

		public ScannerListenerDelegator() {
			this.delegatee = new AbstractScannerListener() {
			};
		}

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			delegatee.systemCallEnd(calledService, time);
		}

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			delegatee.systemCallBegin(calledService, time);
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			delegatee.externalCallEnd(callingService, calledService, time);
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			delegatee.externalCallBegin(callingService, calledService, time);
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			delegatee.internalActionBegin(callingService, time);
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			delegatee.internalActionEnd(callingService, time);
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			delegatee.sqlStatement(callingService, statement);
		}

		public void setDetectionType(ScanningProgressListener delegatee) {
			this.delegatee = delegatee;
		}

	}

	private static abstract class AbstractScannerListener implements ScanningProgressListener {

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			logger.warn("Encountered unexpected end of traversal.");
		}

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			logger.warn("Encountered unexpected begin of system level service call.");
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			logger.warn("Encountered unexpected end of external service call.");
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			logger.warn("Encountered unexpected begin of external service call.");
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			logger.warn("Encountered unexpected begin of internal action.");
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			logger.warn("Encountered unexpected end of internal action.");
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			logger.warn("Encountered unexpected SQL statement.");
		}

	}

	private class DetectSystemCall extends AbstractScannerListener {

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			behaviour = seffFromFQN(calledService.toFQN());

			// begin detection with Start action's successor
			StartAction startAction = PCMHelper.findStartAction(behaviour);
			expectNextAction(startAction.getSuccessor_AbstractAction());
		}

	}

	private class DetectExternalCallAction extends AbstractScannerListener {

		private int callDepth;

		// TODO stack really needed?
		private Stack<InvocationTree2PCMMapper> nestedMatcher = new Stack<>();

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			callDepth++;
			ResourceDemandingSEFF seff = seffFromFQN(calledService.toFQN());
			nestedMatcher.push(new InvocationTree2PCMMapper(seffToFQNMap, seff, parametrization));
			// TODO delegate to nested matcher?
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			callDepth--;
			nestedMatcher.pop();
			// TODO delegate to nested matcher?
			if (callDepth == 0) {
				// TODO check if this is the expected external call
				logger.debug("Successfully detected external call " + calledService.toFQN());

				// finished ExternalCall detection
				expectNextAction(expectedAction.getSuccessor_AbstractAction());
			}
		}

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			// ignore internal actions not present in the model
			if (!nestedMatcher.isEmpty()) {
				nestedMatcher.peek().getScannerListener().internalActionBegin(callingService, time);
			}
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			// ignore internal actions not present in the model
			if (!nestedMatcher.isEmpty()) {
				nestedMatcher.peek().getScannerListener().internalActionEnd(callingService, time);
			}
		}

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			if (!nestedMatcher.isEmpty()) {
				nestedMatcher.peek().getScannerListener().systemCallEnd(calledService, time);
			} else {
				super.systemCallEnd(calledService, time);
			}
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			if (!nestedMatcher.isEmpty()) {
				nestedMatcher.peek().getScannerListener().sqlStatement(callingService, statement);
			} else {
				super.sqlStatement(callingService, statement);
			}
		}

	}

	private class DetectBranchAction extends AbstractScannerListener {

		private BranchAction branch;

		private List<InvocationTree2PCMMapper> candidatesMatcher = new CopyOnWriteArrayList<>();

		public DetectBranchAction() {
			branch = (BranchAction) expectedAction;
			// we don't know yet which branch transition will be taken, so
			// create a candidate mapper for each of them and later throw away
			// each matcher no longer appropriate in light of the advanced
			// scanning progress.
			for (AbstractBranchTransition t : branch.getBranches_Branch()) {
				ResourceDemandingBehaviour behaviour = t.getBranchBehaviour_BranchTransition();
				try {
					candidatesMatcher.add(new InvocationTree2PCMMapper(seffToFQNMap, behaviour,
							(PCMParametrization) parametrization.clone()));
				} catch (CloneNotSupportedException e) {
					// should not happen actually
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			List<InvocationTree2PCMMapper> removedCandidates = new ArrayList<>();
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				ExternalCallAction nextExternalCall = PCMHelper.findNextExternalCall(m.getExpectedAction());
				boolean match = isSameService(nextExternalCall, calledService);
				if (!match) {
					// no longer a candidate
					candidatesMatcher.remove(m);
					removedCandidates.add(m);
				}
			}

			if (candidatesMatcher.isEmpty()) {
				if (removedCandidates.size() > 1) {
					logger.warn("Could not decide which branch transition to follow "
							+ "because more than one candidate is left. Choosing first candidate.");
				}

				// finalize branch detection
				InvocationTree2PCMMapper chosenMapper = removedCandidates.get(0);
				parametrization.mergeFrom(chosenMapper.getParametrization());
				expectNextAction(branch.getSuccessor_AbstractAction());
			}

			// delegate to all candidates
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				m.getScannerListener().externalCallBegin(callingService, calledService, time);
			}
		}

		@Override
		public void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time) {
			// delegate to all candidates
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				m.getScannerListener().externalCallEnd(callingService, calledService, time);
			}
		}

		private boolean isSameService(ExternalCallAction firstExternalCall, MethodIdent calledService) {
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

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			// delegate to all candidates
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				m.getScannerListener().internalActionBegin(callingService, time);
			}
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			// delegate to all candidates
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				m.getScannerListener().internalActionEnd(callingService, time);
			}
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			// delegate to all candidates
			for (InvocationTree2PCMMapper m : candidatesMatcher) {
				m.getScannerListener().sqlStatement(callingService, statement);
			}
		}

	}

	private class DetectInternalAction extends AbstractScannerListener {

		private double timeBegin;

		private SQLStatementSequence sqlStatements = new SQLStatementSequence();

		@Override
		public void internalActionBegin(MethodIdent callingService, double time) {
			if (timeBegin != 0) {
				logger.warn("Encountered consecutive begins of internal action without a corresponding end action.");
			}
			timeBegin = time;
		}

		@Override
		public void internalActionEnd(MethodIdent callingService, double time) {
			if (timeBegin == 0) {
				logger.warn("Encountered end of internal action, but did not find corresponding begin action.");
			}
			double difference = time - timeBegin;
			logger.info("Successfully detected internal action, execution time is " + difference);

			parametrization.captureResourceDemand((InternalAction) expectedAction, difference);

			// capture SQL statements, if present
			if (sqlStatements.size() > 0) {
				parametrization.captureSQLStatementSequence((InternalAction) expectedAction, sqlStatements);
				logger.info("Detected " + sqlStatements.size() + " SQL statements within internal action.");
			}

			expectNextAction(expectedAction.getSuccessor_AbstractAction());
		}

		@Override
		public void sqlStatement(MethodIdent callingService, SQLStatement statement) {
			sqlStatements.add(statement);
		}

	}

	private class DetectStopAction extends AbstractScannerListener {

		@Override
		public void systemCallEnd(MethodIdent calledService, double time) {
			logger.debug("Successfully detected end of invocation tree.");

			// reset detection
			detector.setDetectionType(new DetectSystemCall());
		}

	}

}
