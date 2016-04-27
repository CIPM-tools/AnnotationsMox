package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.node.LeafNode;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationGraphLearner;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;

public class SQLStatementsToPCM {

	private static final Logger LOG = Logger.getLogger(SQLStatementsToPCM.class);

	private GraphLearner learner = new InvocationGraphLearner();

	public void addStatementSequence(SQLStatementSequence sequence) {
		Path path = pathFromStatementSequence(filter(sequence));
		LOG.debug("Adding " + path);
		learner.integratePath(path);
	}

	private SQLStatementSequence filter(SQLStatementSequence sequence) {
		SQLStatementSequence filtered = new SQLStatementSequence();
		for (SQLStatement stmt : sequence.getSequence()) {
			// TODO make configurable
			if (stmt.getSql().startsWith("/*") || stmt.getSql().startsWith("SELECT @@")) {
				continue;
			}
			filtered.add(stmt);
		}
		return filtered;
	}

	public GraphLearner getLearner() {
		return learner;
	}

	private static Path pathFromStatementSequence(SQLStatementSequence sequence) {
		List<Node> nodeList = new ArrayList<>();
		for (SQLStatement stmt : sequence.getSequence()) {
			nodeList.add(new LeafNode(stmt));
		}
		return Path.fromNodes(nodeList);
	}

}
