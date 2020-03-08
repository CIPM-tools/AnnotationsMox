package org.annotationsmox.inspectit2pcm.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * An invocation tree, formed by a parent-child-relationship via nested {@link InvocationSequence}s.
 * 
 * @author Patrice Bouillet (parts of this class have been copied from InspectIT)
 * @author Philipp Merkle
 *
 */
public class InvocationSequence {

    private long id;
    
    /**
     * The unique identifier of the method.
     */
    private long methodIdent;

    /**
     * The nested invocation traces are stored in this list.
     */
    private List<InvocationSequence> nestedSequences;

    /**
     * The parent sequence of this sequence if there is any.
     */
    private InvocationSequence parentSequence;

    /**
     * The associated sql statement data object. Can be <code>null</code>.
     */
    private SQLStatement sqlStatementData;

    /**
     * The position if parent sequence is not <code>null</code>.
     */
    private long position;

    /**
     * The duration of this invocation sequence.
     */
    private double duration;

    /**
     * The start time of this invocation sequence.
     */
    private double start;

    /**
     * The end time of this invocation sequence.
     */
    private double end;

    /**
     * The timestamp which shows when this information was created on the Agent.
     */
    private Timestamp timeStamp;

    public long getId() {
        return id;
    }
    
    public long getMethodId() {
        return methodIdent;
    }

    public List<InvocationSequence> getNestedSequences() {
        return nestedSequences;
    }

    public InvocationSequence getParentSequence() {
        return parentSequence;
    }

    public SQLStatement getSqlStatement() {
        return sqlStatementData;
    }

    public long getPosition() {
        return position;
    }

    public double getDuration() {
        return duration;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

}
