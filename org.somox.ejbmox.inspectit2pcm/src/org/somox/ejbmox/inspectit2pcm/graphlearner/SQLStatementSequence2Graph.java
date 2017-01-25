package org.somox.ejbmox.inspectit2pcm.graphlearner;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
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

    private GraphLearner learner = new SQLInvocationGraphLearner();

    public void addStatementSequence(SQLStatementSequence sequence) {
        SQLStatementSequence filteredSequence = filter(sequence);
        Path path = convertToPath(filteredSequence);
        LOG.debug("Adding " + path);
        learner.integratePath(path);
    }

    public SPGraph getLearnedGraph() {
        return learner.getGraph();
    }

    private SQLStatementSequence filter(SQLStatementSequence sequence) {
        SQLStatementSequence filtered = new SQLStatementSequence();
        for (SQLStatement stmt : sequence.getSequence()) {
            // TODO make configurable
            // remove comments and metadata queries
            if (stmt.getSql().startsWith("/*") || stmt.getSql().toLowerCase().startsWith("select @@")) {
                continue;
            }
            filtered.add(stmt);
        }
        return filtered;
    }

    private static Path convertToPath(SQLStatementSequence sequence) {
        List<Node> nodeList = new ArrayList<>();
        for (SQLStatement stmt : sequence.getSequence()) {
            nodeList.add(new LeafNode(stmt));
        }
        return Path.fromNodes(nodeList);
    }

}
