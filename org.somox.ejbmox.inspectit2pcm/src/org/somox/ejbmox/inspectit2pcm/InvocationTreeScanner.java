package org.somox.ejbmox.inspectit2pcm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.rest.IdentsServiceClient;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsServiceClient;

/**
 * Scans an invocation tree (@link {@link InvocationSequence}) recursively in
 * order of (ascending) invocation times.
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
		scanExternalServiceInvocation(invocation);
		listener.scanFinished();
	}

	private void scanExternalServiceInvocation(InvocationSequence invocation) {
		scanExternalServiceInvocation(invocation, new Stack<MethodIdent>());
	}

	private void scanExternalServiceInvocation(InvocationSequence invocation, Stack<MethodIdent> invocationStack) {
		MethodIdent calledService = methodIdToIdentMap.get(invocation.getMethodId());

		// process external service invocation
		if (!invocationStack.isEmpty()) {
			listener.internalActionEnd(invocationStack.peek(), invocation.getStart());
			listener.externalCallBegin(invocationStack.peek(), calledService, invocation.getStart());
		} else {
			listener.systemCallBegin(calledService, invocation.getStart());
		}
		invocationStack.push(calledService);
		listener.internalActionBegin(invocationStack.peek(), invocation.getStart());

		// recursively traverse child invocations
		scanNestedInvocations(invocation, invocationStack);

		// after traversing child invocations
		listener.internalActionEnd(invocationStack.peek(), invocation.getEnd());
		invocationStack.pop();

		if (invocationStack.size() >= 1) {
			listener.externalCallEnd(invocationStack.peek(), calledService, invocation.getEnd());
			listener.internalActionBegin(invocationStack.peek(), invocation.getEnd());
		}
	}

	private void scanNestedInvocations(InvocationSequence invocation, Stack<MethodIdent> invocationStack) {
		for (InvocationSequence nestedInvocation : invocation.getNestedSequences()) {
			MethodIdent calledService = methodIdToIdentMap.get(nestedInvocation.getMethodId());
			if (isExternalService(calledService)) {
				scanExternalServiceInvocation(nestedInvocation, invocationStack);
			} else {
				scanNestedInvocations(nestedInvocation, invocationStack);
			}
		}
	}

	private boolean isExternalService(MethodIdent ident) {
		return externalServicesFQN.contains(ident.toFQN());
	}

}
