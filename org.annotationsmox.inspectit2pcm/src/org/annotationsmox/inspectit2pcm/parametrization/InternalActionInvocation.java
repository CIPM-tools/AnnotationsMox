package org.annotationsmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.List;

import org.annotationsmox.inspectit2pcm.graphlearner.SQLHelper;
import org.annotationsmox.inspectit2pcm.model.SQLStatement;
import org.annotationsmox.inspectit2pcm.model.SQLStatementSequence;

/**
 * Represents a distinct invocation of an internal action.
 * 
 * @author Philipp Merkle
 *
 */
public class InternalActionInvocation {

    private double duration;

    private SQLStatementSequence sqlSequence;

    public InternalActionInvocation(double duration, SQLStatementSequence sqlSequence) {
        this.duration = duration;
        this.sqlSequence = sqlSequence;
    }

    public double getDuration() {
        return duration;
    }

    public double getDurationWithoutSQL() {
        if (sqlSequence == null || sqlSequence.size() == 0) {
            return duration;
        }

        // sum up durations of all (actual) statements in the sequence
        double sqlDuration = 0;
        for (SQLStatement stmt : sqlSequence.getSequence()) {
            if (SQLHelper.isActualStatement(stmt.getSql())) {
                sqlDuration += stmt.getDuration();
            }
        }

        double exclusiveDuration = duration - sqlDuration;

        // fail fast in case there is a programming error
        if (exclusiveDuration < 0) {
            throw new RuntimeException("Exclusive duration cannot be smaller than 0.");
        }

        return exclusiveDuration;
    }

    public SQLStatementSequence getSqlSequence() {
        return sqlSequence;
    }

    public static List<Double> selectDurations(List<InternalActionInvocation> invocations) {
        List<Double> durations = new ArrayList<>();
        for (InternalActionInvocation invocation : invocations) {
            durations.add(invocation.getDuration());
        }
        return durations;
    }

    public static List<Double> selectDurationsWithoutSQL(List<InternalActionInvocation> invocations) {
        List<Double> durations = new ArrayList<>();
        for (InternalActionInvocation invocation : invocations) {
            durations.add(invocation.getDurationWithoutSQL());
        }
        return durations;
    }

    public static List<SQLStatementSequence> selectNonEmptySQLSequences(List<InternalActionInvocation> invocations) {
        List<SQLStatementSequence> sequences = new ArrayList<>();
        for (InternalActionInvocation invocation : invocations) {
            if (invocation.getSqlSequence().size() > 0) {
                sequences.add(invocation.getSqlSequence());
            }
        }
        return sequences;
    }

}
