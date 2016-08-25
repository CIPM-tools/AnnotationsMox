package org.somox.ejbmox.inspectit2pcm;

import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;

/**
 * A listener that observes the progress of a {@link InvocationTreeScanner}.
 * 
 * @author Philipp Merkle
 *
 */
public interface ScanningProgressListener {

    void systemCallBegin(MethodIdent calledService, double time);

    void systemCallEnd(MethodIdent calledService, double time);

    void externalCallBegin(MethodIdent callingService, MethodIdent calledService, double time);

    void externalCallEnd(MethodIdent callingService, MethodIdent calledService, double time);

    void internalActionBegin(MethodIdent callingService, double time);

    void internalActionEnd(MethodIdent callingService, double time);

    void sqlStatement(MethodIdent callingService, SQLStatement statement);

}
