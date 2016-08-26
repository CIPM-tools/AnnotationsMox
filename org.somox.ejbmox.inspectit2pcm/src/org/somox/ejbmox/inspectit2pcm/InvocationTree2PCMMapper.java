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
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.parametrization.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.parametrization.PCMParametrization;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Relates the events produced by a {@link InvocationTreeScanner} to SEFF actions of a PCM model.
 * This allows to collect, for instance, the execution times of InternalActions. These are collected
 * in a {@link PCMParametrization} instance, and are finally used to parametrize the PCM model (
 * {@link #parametrize(AggregationStrategy)}).
 *
 * @author Philipp Merkle
 *
 */
public class InvocationTree2PCMMapper {

    private static final Logger logger = Logger.getLogger(InvocationTree2PCMMapper.class);

    private final Map<ResourceDemandingSEFF, String> seffToFQNMap;

    private final ScanningProgressDispatcher dispatcher = new ScanningProgressDispatcher();

    private final PCMParametrization parametrization;

    static {
        // TODO use config file
        // Logger.getRootLogger().setLevel(Level.INFO);
    }

    /**
     *
     * @param seffToFQNMap
     * @param ensureInternalActionsBeforeSTOPAction
     *            is necessary only if pcm.tx extraction is used
     */
    public InvocationTree2PCMMapper(final Map<ResourceDemandingSEFF, String> seffToFQNMap,
            final boolean ensureInternalActionsBeforeSTOPAction) {
        this.seffToFQNMap = seffToFQNMap;
        this.parametrization = new PCMParametrization();

        if (ensureInternalActionsBeforeSTOPAction) {
            this.ensureInternalActionBeforeStopActions();
        }
    }

    /**
     * Insert one InternalAction before each SEFF's stop, if not present yet; serves as a
     * placeholder for SQL statements issued at the end of the service invocation
     */
    private void ensureInternalActionBeforeStopActions() {

        for (final Entry<ResourceDemandingSEFF, String> entry : this.seffToFQNMap.entrySet()) {
            final StopAction stop = PCMHelper.findStopAction(entry.getKey());
            final AbstractAction stopPredecessor = stop.getPredecessor_AbstractAction();
            if (!(stopPredecessor instanceof InternalAction)) {
                final InternalAction placeholder = PCMHelper.createInternalActionStub(
                        stop.getResourceDemandingBehaviour_AbstractAction(),
                        "Inserted by InspectIT2PCM processor @ " + entry.getValue());
                placeholder.setPredecessor_AbstractAction(stopPredecessor);
                stop.setPredecessor_AbstractAction(placeholder);
            }
        }
    }

    public ScanningProgressListener getScanningProgressDispatcher() {
        return this.dispatcher;
    }

    public PCMParametrization getParametrization() {
        return this.parametrization;
    }

    private static ResourceDemandingSEFF seffFromFQN(final Map<ResourceDemandingSEFF, String> seffToFQNMap,
            final String fqn) {
        try {
            return seffToFQNMap.entrySet().stream().filter(e -> e.getValue().equals(fqn)).findFirst().get().getKey();
        } catch (final NoSuchElementException e) {
            throw new RuntimeException("Could not find SEFF with FQN " + fqn, e);
        }
    }

    private static boolean isSameService(final ExternalCallAction firstExternalCall, final MethodIdent calledService) {
        if (firstExternalCall == null) {
            return false;
        }
        final OperationSignature signature = firstExternalCall.getCalledService_ExternalService();
        // TODO very hack solution right now!! check if interface/bean do
        // match
        if (calledService.getMethodName().equals(signature.getEntityName())) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isWithinSameService(final InternalAction action, final MethodIdent callingService,
            final DetectionContext context) {
        final ResourceDemandingSEFF seff = PCMHelper.findSeffForInternalAction(action);
        final String seffFQN = context.seffToFQNMap.get(seff);

        final boolean isSame = callingService.toFQN().equals(seffFQN);
        if (!isSame) {
            logErrorInContext("Expected internal action within SEFF " + seffFQN
                    + ", but encountered internal action in scope of " + callingService.toFQN(), context);
        }

        return isSame;
    }

    private static String message(final String message, final DetectionContext ctx) {
        return ctx.getShortName() + ": " + message;
    }

    private static void logErrorInContext(final String message, final DetectionContext ctx) {
        logger.error(message(message, ctx));
    }

    private static void logWarnInContext(final String message, final DetectionContext ctx) {
        logger.warn(message(message, ctx));
    }

    private static void logInfoInContext(final String message, final DetectionContext ctx) {
        logger.info(message(message, ctx));
    }

    private static void logDebugInContext(final String message, final DetectionContext ctx) {
        logger.debug(message(message, ctx));
    }

    private class ScanningProgressDispatcher implements ScanningProgressListener {

        private DetectionContext rootContext;

        @Override
        public void systemCallBegin(final MethodIdent calledService, final double time) {
            logger.debug("Dispatching begin of system call " + calledService.toFQN());

            if (this.rootContext != null) {
                throw new IllegalStateException(
                        "Encountered begin of system call" + " although another system call is still being processed.");
            }
            final ResourceDemandingBehaviour behaviour = seffFromFQN(InvocationTree2PCMMapper.this.seffToFQNMap,
                    calledService.toFQN());
            final DetectionContext parent = null;
            this.rootContext = new DetectionContext(calledService.toFQN(), parent, behaviour,
                    InvocationTree2PCMMapper.this.parametrization, InvocationTree2PCMMapper.this.seffToFQNMap);

            // delegate to allow for setup operations
            this.rootContext.getDetector().systemCallBegin(calledService, time);
        }

        @Override
        public void systemCallEnd(final MethodIdent calledService, final double time) {
            logger.debug("Dispatching end of system call " + calledService.toFQN());

            if (this.rootContext == null) {
                throw new IllegalStateException(
                        "Encountered end of system call, but there is no system call being processed.");
            }

            // delegate to allow for clean up operations
            if (this.rootContext.getDetector() != null) {
                this.rootContext.getDetector().systemCallEnd(calledService, time);
            }

            this.rootContext = null;
        }

        @Override
        public void externalCallBegin(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logger.debug("Dispatching begin of external call " + calledService.toFQN() + ", called by "
                    + callingService.toFQN());
            this.rootContext.getDetector().externalCallBegin(callingService, calledService, time);
        }

        @Override
        public void externalCallEnd(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logger.debug("Dispatching end of external call " + calledService.toFQN() + ", called by "
                    + callingService.toFQN());
            this.rootContext.getDetector().externalCallEnd(callingService, calledService, time);
        }

        @Override
        public void internalActionBegin(final MethodIdent callingService, final double time) {
            logger.debug("Dispatching begin of internal action, called by " + callingService.toFQN());
            this.rootContext.getDetector().internalActionBegin(callingService, time);
        }

        @Override
        public void internalActionEnd(final MethodIdent callingService, final double time) {
            logger.debug("Dispatching end of internal action, called by " + callingService.toFQN());
            this.rootContext.getDetector().internalActionEnd(callingService, time);
        }

        @Override
        public void sqlStatement(final MethodIdent callingService, final SQLStatement statement) {
            logger.debug("Dispatching SQL statement, called by " + callingService.toFQN());
            this.rootContext.getDetector().sqlStatement(callingService, statement);
        }

    }

    private static class DetectionContext {

        private final ResourceDemandingBehaviour behaviour;

        private final DetectionContext parent;

        private AbstractAction expectedAction;

        private final PCMParametrization parametrization;

        private ScanningProgressListener currentDetector;

        private DetectionContext nestedContext;

        private final Map<ResourceDemandingSEFF, String> seffToFQNMap;

        private final String name;

        public DetectionContext(final String name, final DetectionContext parent,
                final ResourceDemandingBehaviour behaviour, final PCMParametrization parametrization,
                final Map<ResourceDemandingSEFF, String> seffToFQNMap) {
            this.name = name;
            this.parent = parent;
            this.behaviour = behaviour;
            this.parametrization = parametrization;
            this.seffToFQNMap = seffToFQNMap;

            // begin detection with Start action's successor
            this.expectedAction = PCMHelper.findStartAction(behaviour);
            this.proceedWithNextAction();
        }

        public void proceedWithNextAction() {
            this.expectNextAction(this.expectedAction.getSuccessor_AbstractAction());
        }

        private void expectNextAction(final AbstractAction action) {
            logDebugInContext("Expecting next action " + PCMHelper.entityToString(action), this);
            this.expectedAction = action;
            if (isExternalCallAction(action)) {
                this.currentDetector = new DetectExternalCallAction((ExternalCallAction) action, this,
                        this.seffToFQNMap);
            } else if (isInternalAction(action)) {
                this.currentDetector = new DetectInternalAction((InternalAction) action, this, this.seffToFQNMap);
            } else if (isStopAction(action)) {
                this.currentDetector = null;
                // currentDetector = new StopDetector(this);
            } else if (isBranchAction(action)) {
                this.currentDetector = new DetectBranchAction((BranchAction) action, this, this.seffToFQNMap);
            } else {
                logWarnInContext("Could not find extractor for AbstractActions of type " + action.getClass()
                        + ". Continuing with successor...", this);
                this.expectNextAction(action.getSuccessor_AbstractAction());
            }
        }

        private boolean isBranchAction(final AbstractAction action) {
            return SeffPackage.eINSTANCE.getBranchAction().isInstance(action);
        }

        private boolean isStopAction(final AbstractAction action) {
            return SeffPackage.eINSTANCE.getStopAction().isInstance(action);
        }

        private boolean isInternalAction(final AbstractAction action) {
            return SeffPackage.eINSTANCE.getInternalAction().isInstance(action);
        }

        private boolean isExternalCallAction(final AbstractAction action) {
            return SeffPackage.eINSTANCE.getExternalCallAction().isInstance(action);
        }

        public void leaveNestedContext() {
            logDebugInContext("Clearing nested context", this);
            this.nestedContext = null;
            this.proceedWithNextAction();
        }

        public void leaveContext() {
            this.parent.leaveNestedContext();
        }

        public boolean reachedStop() {
            return SeffPackage.eINSTANCE.getStopAction().isInstance(this.expectedAction);
        }

        public ScanningProgressListener getDetector() {
            if (this.nestedContext != null && this.nestedContext.getDetector() != null) {
                return this.nestedContext.getDetector();
            } else {
                return this.currentDetector;
            }
        }

        public AbstractAction getExpectedAction() {
            return this.expectedAction;
        }

        public PCMParametrization getParametrization() {
            return this.parametrization;
        }

        public String getName() {
            String parentNames = "";
            if (this.parent == null) {
                parentNames = "#";
            } else {
                parentNames = this.parent.getName();
            }
            return parentNames + " -> " + this.name;
        }

        public String getShortName() {
            String parentNames = "";
            if (this.parent == null) {
                parentNames = "#";
            } else {
                parentNames = this.parent.getShortName() + " -> ";
            }
            final String[] nameSegments = this.name.split("\\.");
            String shortName = "";
            if (nameSegments.length <= 2) {
                shortName = this.name;
            } else {
                final int n = nameSegments.length;
                shortName = nameSegments[n - 2] + "." + nameSegments[n - 1];
            }
            return parentNames + shortName;
        }

    }

    private static class DefaultDetector implements ScanningProgressListener {

        protected DetectionContext context;

        protected Map<ResourceDemandingSEFF, String> seffToFQNMap;

        public DefaultDetector(final DetectionContext context, final Map<ResourceDemandingSEFF, String> seffToFQNMap) {
            this.context = context;
            this.seffToFQNMap = seffToFQNMap;
        }

        @Override
        public void systemCallBegin(final MethodIdent calledService, final double time) {
            // nothing to do
        }

        @Override
        public void systemCallEnd(final MethodIdent calledService, final double time) {
            // nothing to do
        }

        @Override
        public void externalCallBegin(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logWarnInContext("Encountered unexpected begin of external call.", this.context);
        }

        @Override
        public void externalCallEnd(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logWarnInContext("Encountered unexpected end of external call.", this.context);
        }

        @Override
        public void internalActionBegin(final MethodIdent callingService, final double time) {
            logWarnInContext("Encountered unexpected begin of internal action.", this.context);
        }

        @Override
        public void internalActionEnd(final MethodIdent callingService, final double time) {
            logWarnInContext("Encountered unexpected end of internal action.", this.context);
        }

        @Override
        public void sqlStatement(final MethodIdent callingService, final SQLStatement statement) {
            logWarnInContext("Encountered unexpected SQL statement.", this.context);
        }

    }

    private static class DetectExternalCallAction extends DefaultDetector {

        private final ExternalCallAction externalCall;

        public DetectExternalCallAction(final ExternalCallAction externalCall, final DetectionContext context,
                final Map<ResourceDemandingSEFF, String> seffToFQNMap) {
            super(context, seffToFQNMap);
            this.externalCall = externalCall;
        }

        @Override
        public void externalCallBegin(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            // TODO check if this is the expected call
            // super.externalCallBegin(callingService, calledService, time);

            final boolean match = isSameService(this.externalCall, calledService);
            if (match) {
                logDebugInContext("Detected start of external call " + calledService.toFQN(), this.context);
                final ResourceDemandingSEFF seff = seffFromFQN(this.seffToFQNMap, calledService.toFQN());
                // TODO improve following statement
                this.context.nestedContext = new DetectionContext(calledService.toFQN(), this.context, seff,
                        this.context.getParametrization(), this.seffToFQNMap);
            } else {
                // TODO refine message
                final String interfaceMethodFQN = this.externalCall.getCalledService_ExternalService()
                        .getInterface__OperationSignature().getEntityName() + "."
                        + this.externalCall.getCalledService_ExternalService().getEntityName();
                logErrorInContext(
                        "Expected external call from " + callingService.toFQN() + " to interface method "
                                + interfaceMethodFQN + ", but got call to class " + calledService.toFQN(),
                        this.context);
            }
        }

        @Override
        public void externalCallEnd(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            // TODO check if this is the expected call
            final boolean match = isSameService(this.externalCall, calledService);
            if (match) {
                logDebugInContext("Detected end of external call " + calledService.toFQN(), this.context);
                this.context.leaveNestedContext();
            } else {
                // TODO refine message
                final String interfaceMethodFQN = this.externalCall.getCalledService_ExternalService()
                        .getInterface__OperationSignature().getEntityName() + "."
                        + this.externalCall.getCalledService_ExternalService().getEntityName();
                logErrorInContext(
                        "Expected external call from " + callingService.toFQN() + " to interface method "
                                + interfaceMethodFQN + ", but got call to class " + calledService.toFQN(),
                        this.context);
            }
        }

    }

    private static class DetectBranchAction extends DefaultDetector {

        private final BranchAction branch;

        private final List<DetectionContext> contextCandidates = new CopyOnWriteArrayList<>();

        private final Map<DetectionContext, AbstractBranchTransition> transitionMap = new HashMap<>();

        public DetectBranchAction(final BranchAction branch, final DetectionContext context,
                final Map<ResourceDemandingSEFF, String> seffToFQNMap) {
            super(context, seffToFQNMap);
            this.branch = branch;
            logDebugInContext("Trying to detect " + PCMHelper.entityToString(branch), context);

            // we don't know yet which branch transition will be taken, so
            // create a candidate mapper for each of them and later throw away
            // each matcher no longer appropriate in light of the advanced
            // scanning progress.
            int i = 0;
            for (final AbstractBranchTransition t : branch.getBranches_Branch()) {
                final ResourceDemandingBehaviour behaviour = t.getBranchBehaviour_BranchTransition();
                final DetectionContext mapper = new DetectionContext("Transition_" + i, context, behaviour,
                        new PCMParametrization(), seffToFQNMap);
                this.contextCandidates.add(mapper);
                this.transitionMap.put(mapper, t);
                i++;
            }
        }

        @Override
        public void systemCallEnd(final MethodIdent calledService, final double time) {
            this.finalizeBranchDetection();
        }

        @Override
        public void externalCallBegin(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logDebugInContext("Dispatching external call to branch transition candidates: " + calledService.toFQN(),
                    this.context);

            // delegate to all remaining candidates
            final List<DetectionContext> removedCandidates = new ArrayList<>();
            for (final DetectionContext ctx : this.contextCandidates) {
                if (!ctx.reachedStop()) {
                    ctx.getDetector().externalCallBegin(callingService, calledService, time);
                } else {
                    this.contextCandidates.remove(ctx);
                    logDebugInContext("Removing context from candidates", ctx);
                    removedCandidates.add(ctx);
                }
            }
            this.finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
        }

        private void finalizeBranchDetection() {
            if (this.contextCandidates.size() > 1) {
                logWarnInContext("Could not decide which branch transition to follow "
                        + "because more than one candidate is left. Choosing first candidate.", this.context);
            }
            this.finalizeBranchDetection(this.contextCandidates.get(0));
        }

        private void finalizeBranchDetection(final DetectionContext chosenContext) {
            this.context.getParametrization().mergeFrom(chosenContext.parametrization);
            this.context.getParametrization().captureBranchTransition(this.transitionMap.get(chosenContext));
            this.context.proceedWithNextAction();
        }

        private boolean finalizeBranchDetectionIfNoRemainingCandidates(final List<DetectionContext> removedCandidates) {
            if (this.contextCandidates.isEmpty()) {
                logDebugInContext("Finalizing branch detection.", this.context);
                if (removedCandidates.size() > 1) {
                    logWarnInContext(
                            "Could not decide which branch transition to follow "
                                    + "because more than one candidate is left. Choosing first candidate.",
                            this.context);
                }

                // finalize branch detection
                final DetectionContext chosenContext = removedCandidates.get(0);
                this.finalizeBranchDetection(chosenContext);

                return true; // finalized
            }
            return false; // not yet finalized
        }

        @Override
        public void externalCallEnd(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            // delegate to all remaining candidates
            final List<DetectionContext> removedCandidates = new ArrayList<>();
            for (final DetectionContext ctx : this.contextCandidates) {
                if (!ctx.reachedStop()) {
                    ctx.getDetector().externalCallEnd(callingService, calledService, time);
                } else {
                    this.contextCandidates.remove(ctx);
                    logDebugInContext("Removing context from candidates", ctx);
                    removedCandidates.add(ctx);
                }
            }
            this.finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
        }

        @Override
        public void internalActionBegin(final MethodIdent callingService, final double time) {
            // delegate to all remaining candidates
            final List<DetectionContext> removedCandidates = new ArrayList<>();
            for (final DetectionContext ctx : this.contextCandidates) {
                if (!ctx.reachedStop()) {
                    ctx.getDetector().internalActionBegin(callingService, time);
                } else {
                    this.contextCandidates.remove(ctx);
                    logDebugInContext("Removing context from candidates", ctx);
                    removedCandidates.add(ctx);
                }
            }
            this.finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
        }

        @Override
        public void internalActionEnd(final MethodIdent callingService, final double time) {
            // delegate to all remaining candidates
            final List<DetectionContext> removedCandidates = new ArrayList<>();
            for (final DetectionContext ctx : this.contextCandidates) {
                if (!ctx.reachedStop()) {
                    ctx.getDetector().internalActionEnd(callingService, time);
                } else {
                    this.contextCandidates.remove(ctx);
                    logDebugInContext("Removing context from candidates", ctx);
                    removedCandidates.add(ctx);
                }
            }
            this.finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
        }

        @Override
        public void sqlStatement(final MethodIdent callingService, final SQLStatement statement) {
            // delegate to all remaining candidates
            final List<DetectionContext> removedCandidates = new ArrayList<>();
            for (final DetectionContext ctx : this.contextCandidates) {
                if (!ctx.reachedStop()) {
                    ctx.getDetector().sqlStatement(callingService, statement);
                } else {
                    this.contextCandidates.remove(ctx);
                    logDebugInContext("Removing context from candidates", ctx);
                    removedCandidates.add(ctx);
                }
            }
            this.finalizeBranchDetectionIfNoRemainingCandidates(removedCandidates);
        }

    }

    private static class DetectInternalAction extends DefaultDetector {

        private double timeBegin;

        private final SQLStatementSequence sqlStatements = new SQLStatementSequence();

        /** the action to be detected */
        private final InternalAction action;

        public DetectInternalAction(final InternalAction action, final DetectionContext context,
                final Map<ResourceDemandingSEFF, String> seffToFQNMap) {
            super(context, seffToFQNMap);
            this.action = action;
        }

        @Override
        public void internalActionBegin(final MethodIdent callingService, final double time) {
            if (!isWithinSameService(this.action, callingService, this.context)) {
                this.context.leaveContext();
                // delegate to parent context
                this.context.parent.getDetector().internalActionBegin(callingService, time);
                return;
            }

            if (this.timeBegin != 0) {
                logWarnInContext(
                        "Encountered consecutive begins of internal action without a corresponding end action.",
                        this.context);
            }
            this.timeBegin = time;
        }

        @Override
        public void internalActionEnd(final MethodIdent callingService, final double time) {
            if (!isWithinSameService(this.action, callingService, this.context)) {
                this.context.leaveContext();
                // delegate to parent context
                this.context.parent.getDetector().internalActionEnd(callingService, time);
                return;
            }

            if (this.timeBegin == 0) {
                logWarnInContext("Encountered end of internal action, but did not find corresponding begin action.",
                        this.context);
            }
            final double difference = time - this.timeBegin;
            logInfoInContext("Successfully detected internal action, execution time is " + difference, this.context);

            this.context.getParametrization().captureResourceDemand(this.action, difference);

            // capture SQL statements, if present
            if (this.sqlStatements.size() > 0) {
                this.context.getParametrization().captureSQLStatementSequence(this.action, this.sqlStatements);
                logInfoInContext("Detected " + this.sqlStatements.size() + " SQL statements within internal action.",
                        this.context);
            }

            this.context.proceedWithNextAction();
        }

        @Override
        public void externalCallBegin(final MethodIdent callingService, final MethodIdent calledService,
                final double time) {
            logWarnInContext(
                    "Encountered unexpected begin of external call, thus skipping internal action detection "
                            + "and continuing with its successor (...that is hopefully an external action).",
                    this.context);
            this.context.proceedWithNextAction();
        }

        @Override
        public void sqlStatement(final MethodIdent callingService, final SQLStatement statement) {
            this.sqlStatements.add(statement);
        }

    }

}
