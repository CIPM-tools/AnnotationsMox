package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.List;

import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;

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

        // sum up durations of all statements in the sequence
        double sqlDuration = sqlSequence.getSequence().stream().mapToDouble(s -> s.getDuration()).sum();

        return duration - sqlDuration;
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
