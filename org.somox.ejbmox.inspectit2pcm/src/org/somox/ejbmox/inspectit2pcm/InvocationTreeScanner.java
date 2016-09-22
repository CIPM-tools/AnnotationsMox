package org.somox.ejbmox.inspectit2pcm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.model.WrapperMethodIdent;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;

/**
 * Scans an invocation tree ({@link InvocationSequence}) recursively in order of (ascending)
 * invocation times of nested invocation sequences.
 * <p>
 * The scanning progress is reported to a {@link ScanningProgressListener}, which allows to react to
 * certain scanning events, including, for instance, the begin/end of an external service call.
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationTreeScanner {

    private static final Logger LOG = Logger.getLogger(InvocationTreeScanner.class);

    /** a listener interested in the progress of this scanner */
    private ScanningProgressListener listener;

    /**
     * set of external services identified by their fully qualified name (FQN); used to
     * differentiate between external calls and component-internal calls.
     */
    private Set<String> externalServicesFQN;

    private Set<String> interfacesFQN;

    private Map<Long, MethodIdent> methodIdToIdentMap;

    private Map<String, MethodIdent> methodFQNToIdentMap;

    public InvocationTreeScanner(ScanningProgressListener listener, Set<String> externalServicesFQN,
            Set<String> interfacesFQN, IdentsServiceClient identService, InvocationsServiceClient invocationsService) {
        this.listener = listener;
        this.externalServicesFQN = externalServicesFQN;
        this.interfacesFQN = interfacesFQN;

        methodIdToIdentMap = new HashMap<>();
        methodFQNToIdentMap = new HashMap<>();
        for (MethodIdent m : identService.listMethodIdents()) {
            methodIdToIdentMap.put(m.getId(), m);
            methodFQNToIdentMap.put(m.toFQN(), m);
        }
    }

    public void scanInvocationTree(InvocationSequence invocation) {
        MethodIdent calledService = methodIdent(invocation);
        listener.systemCallBegin(calledService, invocation.getStart());
        listener.internalActionBegin(calledService, invocation.getStart());
        scanInvocationTreeRecursive(invocation);
        listener.internalActionEnd(calledService, invocation.getEnd());
        listener.systemCallEnd(calledService, invocation.getEnd());
    }

    private void scanInvocationTreeRecursive(InvocationSequence invocation) {
        MethodIdent calledService = methodIdent(invocation);
        Stack<MethodIdent> externalServicesStack = new Stack<>();
        externalServicesStack.push(calledService);
        scanInvocationTreeRecursive(invocation, externalServicesStack);
    }

    private void scanInvocationTreeRecursive(InvocationSequence invocation, Stack<MethodIdent> externalServicesStack) {
        MethodIdent callingService = externalServicesStack.peek();
        scanSQLStatements(callingService, invocation);
        for (InvocationSequence nestedInvocation : invocation.getNestedSequences()) {
            MethodIdent calledService = methodIdent(nestedInvocation);
            if (isExternalServiceWrapper(calledService)) {
                calledService = new WrapperMethodIdent(calledService, findWrappedService(nestedInvocation));
            }
            /*
             * following condition ensures that only one external call (the outer one, i.e. the
             * wrapper) will be reported for a wrapped service call (which would otherwise be
             * reported as two separate successive calls)
             */
            if ((isExternalService(calledService) && !callingService.isWrapperFor(calledService))
                    || calledService.isWrapper()) {
                // before external call, close existing internal action
                listener.internalActionEnd(callingService, nestedInvocation.getStart());

                // external call
                listener.externalCallBegin(callingService, calledService, nestedInvocation.getStart());
                listener.internalActionBegin(calledService, nestedInvocation.getStart());
                externalServicesStack.push(calledService);
                scanInvocationTreeRecursive(nestedInvocation, externalServicesStack);
                externalServicesStack.pop();
                listener.internalActionEnd(calledService, nestedInvocation.getEnd());
                listener.externalCallEnd(callingService, calledService, nestedInvocation.getEnd());

                // after external call, open new internal action
                listener.internalActionBegin(callingService, nestedInvocation.getEnd());
            } else { // internal call
                scanInvocationTreeRecursive(nestedInvocation, externalServicesStack);
            }
        }
    }

    private MethodIdent findWrappedService(InvocationSequence wrapperInvocation) {
        return methodIdent(wrapperInvocation.getNestedSequences().stream()
                .filter(i -> isExternalService(methodIdent(i))).findFirst().get());
    }

    private MethodIdent methodIdent(InvocationSequence s) {
        return methodIdToIdentMap.get(s.getMethodId());
    }

    private void scanSQLStatements(MethodIdent callingService, InvocationSequence invocation) {
        SQLStatement stmt = invocation.getSqlStatement();
        if (stmt != null) {
            listener.sqlStatement(callingService, stmt);
        }
    }

    private boolean isExternalService(MethodIdent ident) {
        return externalServicesFQN.contains(ident.toFQN());
    }

    private boolean isExternalServiceWrapper(MethodIdent calledService) {
        boolean excludeMethodName = true;
        boolean isWrapper = calledService.toFQN(excludeMethodName).matches(".*_Wrapper$");
        if (isWrapper && isRepositoryInterface(wrappedInterfaceFQN(calledService))) {
            return true;
        }
        return false;
    }

    private String wrappedInterfaceFQN(MethodIdent wrapperService) {
        boolean excludeMethodName = true;
        String[] segments = wrapperService.toFQN(excludeMethodName).split("\\.");
        segments[segments.length - 1] = segments[segments.length - 1].replaceAll("^_", "").replaceAll("_Wrapper$", "");
        return String.join(".", segments);
    }

    private boolean isRepositoryInterface(String interfaceFQN) {
        return interfaceFQN.contains(interfaceFQN);
    }

}
