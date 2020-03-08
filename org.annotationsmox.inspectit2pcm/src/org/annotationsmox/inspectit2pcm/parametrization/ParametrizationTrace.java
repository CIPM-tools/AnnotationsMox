package org.annotationsmox.inspectit2pcm.parametrization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.palladiosimulator.pcm.seff.InternalAction;

public class ParametrizationTrace {

    private Map<InternalAction, SQLStatement> internalActionToStatementLinks;

    public ParametrizationTrace() {
        internalActionToStatementLinks = new HashMap<>();
    }

    public void addInternalActionToSQLStatementLink(InternalAction action, SQLStatement stmt) {
        internalActionToStatementLinks.put(action, stmt);
    }

    public Map<InternalAction, SQLStatement> getInternalActionToStatementLinks() {
        return Collections.unmodifiableMap(internalActionToStatementLinks);
    }

}
