package org.somox.ejbmox.inspectit2pcm;

import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffPackage;
import org.palladiosimulator.pcm.seff.StartAction;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Relates the events produced by a {@link InvocationTreeScanner} to SEFF actions of a PCM model. This allows to collect, for instance, the execution times of InternalActions. These are collected in a {@link PCMParametrization} instance, and are finally used to parametrize the PCM model ({@link #parametrize(AggregationStrategy)}).    
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationTree2PCMMapper {

	private static final Logger logger = Logger.getLogger(InvocationTree2PCMMapper.class);

	private PCMParametrization parametrization;

	private ScannerListenerDelegator detector = new ScannerListenerDelegator();

	private ResourceDemandingSEFF seff;

	private AbstractAction expectedAction;

	private Map<ResourceDemandingSEFF, String> seffToFQNMap;

	private InvocationTree2PCMMapper(Map<ResourceDemandingSEFF, String> seffToFQNMap, ResourceDemandingSEFF seff,
			PCMParametrization parametrization) {
		this(seffToFQNMap);
		this.seff = seff;
		this.parametrization = parametrization;

		// begin detection with Start action's successor
		StartAction startAction = PCMHelper.findStartAction(seff);
		expectNextAction(startAction.getSuccessor_AbstractAction());
	}

	public InvocationTree2PCMMapper(Map<ResourceDemandingSEFF, String> seffToFQNMap) {
		this.seffToFQNMap = seffToFQNMap;
		this.detector.setDetectionType(new DetectSystemCall());
		this.parametrization = new PCMParametrization();
	}

	public void expectNextAction(AbstractAction action) {
		this.expectedAction = action;
		if (SeffPackage.eINSTANCE.getExternalCallAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectExternalCallAction());
		} else if (SeffPackage.eINSTANCE.getInternalAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectInternalAction());
		} else if (SeffPackage.eINSTANCE.getStopAction().isInstance(expectedAction)) {
			detector.setDetectionType(new DetectStopAction());
		} else {
			logger.warn("Could not find extractor for AbstractActions of type " + expectedAction.getClass());
		}
	}

	public void parametrize(AggregationStrategy aggregation) {
		parametrization.parametrize(aggregation);
	}

	public ScanningProgressListener getScannerListener() {
		return detector;
	}

	private ResourceDemandingSEFF seffFromFQN(String fqn) {
		return seffToFQNMap.entrySet().stream().filter(e -> e.getValue().equals(fqn)).findFirst().get().getKey();
	}

	private static class ScannerListenerDelegator implements ScanningProgressListener {

		private ScanningProgressListener delegatee;

		public ScannerListenerDelegator() {
			this.delegatee = new AbstractScannerListener() {
			};
		}

		@Override
		public void scanFinished() {
			delegatee.scanFinished();
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

		public void setDetectionType(ScanningProgressListener delegatee) {
			this.delegatee = delegatee;
		}

	}

	private static abstract class AbstractScannerListener implements ScanningProgressListener {

		@Override
		public void scanFinished() {
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

	}

	private class DetectSystemCall extends AbstractScannerListener {

		@Override
		public void systemCallBegin(MethodIdent calledService, double time) {
			seff = seffFromFQN(calledService.toFQN());

			// begin detection with Start action's successor
			StartAction startAction = PCMHelper.findStartAction(seff);
			expectNextAction(startAction.getSuccessor_AbstractAction());
		}

	}

	private class DetectExternalCallAction extends AbstractScannerListener {

		private int callDepth;

		private Stack<InvocationTree2PCMMapper> nestedMatcher = new Stack<>();

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
		public void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time) {
			callDepth++;
			ResourceDemandingSEFF seff = seffFromFQN(calledService.toFQN());
			nestedMatcher.push(new InvocationTree2PCMMapper(seffToFQNMap, seff, parametrization));
			// TODO delegate to nested matcher?
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
		public void scanFinished() {
			if (!nestedMatcher.isEmpty()) {
				nestedMatcher.peek().getScannerListener().scanFinished();
			} else {
				super.scanFinished();
			}
		}

	}

	private class DetectInternalAction extends AbstractScannerListener {

		private double timeBegin;

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

			expectNextAction(expectedAction.getSuccessor_AbstractAction());
		}

	}

	private class DetectStopAction extends AbstractScannerListener {

		@Override
		public void scanFinished() {
			logger.debug("Successfully detected end of invocation tree.");

			// reset detection
			detector.setDetectionType(new DetectSystemCall());
		}

	}

}
