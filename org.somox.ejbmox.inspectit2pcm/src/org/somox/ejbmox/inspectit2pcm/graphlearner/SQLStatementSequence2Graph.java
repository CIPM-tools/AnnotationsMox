package org.somox.ejbmox.inspectit2pcm.graphlearner;

import org.apache.log4j.Logger;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.graphlearner.Sequence;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;

/**
 * Learns {@link SPGraph}s from {@link SQLStatementSequence}s. Wraps a {@link GraphLearner}.
 * 
 * @author Philipp Merkle
 *
 */
public class SQLStatementSequence2Graph {

    private static final Logger LOG = Logger.getLogger(SQLStatementSequence2Graph.class);

    private GraphLearner<SQLStatement> learner = new SQLInvocationGraphLearner();

    public void addStatementSequence(SQLStatementSequence s) {
        SQLStatementSequence filteredSequence = filter(s);
        Sequence<SQLStatement> sequence = convertToSequence(filteredSequence);
        LOG.debug("Adding " + sequence);
        learner.integrateSequence(sequence);
    }

    public SPGraph getLearnedGraph() {
        return learner.getGraph();
    }

    private SQLStatementSequence filter(SQLStatementSequence sequence) {
        SQLStatementSequence filtered = new SQLStatementSequence();
        for (SQLStatement stmt : sequence.getSequence()) {
            // TODO make configurable
            // ignore comments and metadata queries
            if (!SQLHelper.isActualStatement(stmt.getSql())) {
                continue; // ignore this statement
            }
            filtered.add(stmt);
        }
        return filtered;
    }

    // TODO get rid of SQLStatementSequence
    private static Sequence<SQLStatement> convertToSequence(SQLStatementSequence s) {
        Sequence<SQLStatement> sequence = new Sequence<>();
        for (SQLStatement stmt : s.getSequence()) {
            sequence.add(stmt);
        }
        return sequence;
    }

}
