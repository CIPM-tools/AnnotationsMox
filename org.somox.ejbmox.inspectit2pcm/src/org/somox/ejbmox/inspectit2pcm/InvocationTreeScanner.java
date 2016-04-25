package org.somox.ejbmox.inspectit2pcm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;

/**
 * Scans an invocation tree ({@link InvocationSequence}) recursively in order of
 * (ascending) invocation times of nested invocation sequences.
 * <p>
 * The scanning progress is reported to a {@link ScanningProgressListener},
 * which allows to react to certain scanning events, including, for instance,
 * the begin/end of an external service call.
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationTreeScanner {

	private static final Logger LOG = Logger.getLogger(InvocationTreeScanner.class);

	/** a listener interested in the progress of this scanner */
	private ScanningProgressListener listener;

	/**
	 * set of external services identified by their fully qualified name (FQN);
	 * used to differentiate between external calls and component-internal
	 * calls.
	 */
	private Set<String> externalServicesFQN;

	private Map<Long, MethodIdent> methodIdToIdentMap;

	private Map<String, MethodIdent> methodFQNToIdentMap;

	public InvocationTreeScanner(ScanningProgressListener listener, Set<String> externalServicesFQN,
			IdentsServiceClient identService, InvocationsServiceClient invocationsService) {
		this.listener = listener;
		this.externalServicesFQN = externalServicesFQN;

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
		MethodIdent callingService = methodIdent(invocation);
		for (InvocationSequence nestedInvocation : invocation.getNestedSequences()) {
			MethodIdent calledService = methodIdent(nestedInvocation);
			if (isExternalService(calledService)) {
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
				scanSQLStatements(externalServicesStack.peek(), nestedInvocation);
				scanInvocationTreeRecursive(nestedInvocation, externalServicesStack);
			}
		}
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

}