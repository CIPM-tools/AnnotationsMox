package org.somox.ejbmox.inspectit2pcm.parametrization.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.parametrization.SQLStatementSequence;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.Equalizer;

public class SQLStatementsToPCM {

	private static final Logger LOG = Logger.getLogger(SQLStatementsToPCM.class);

	private NestableNodeChain<SQLStatement> graph = null;

	public void addStatements(SQLStatementSequence sequence) {
		Path<SQLStatement> path = pathFromSequence(sequence);
		LOG.debug("Adding " + path);
		if (graph == null) {
			LOG.debug("Using " + path + " as initial graph");
			graph = new NestableNodeChain<>(path.first());
		} else {
			Path<SQLStatement> closestPath = findPathClosestTo(path);
			LOG.debug("Using closest path " + closestPath);
			integrate(closestPath, path);
		}
	}

	/**
	 * Finds and returns the path closest to the specified path.
	 * 
	 * @param path
	 * @return
	 */
	public Path<SQLStatement> findPathClosestTo(Path<SQLStatement> path) {
		List<Path<SQLStatement>> paths = graph.collectAllPaths();
		LOG.debug("Collected paths: " + paths);
		int minCost = Integer.MAX_VALUE;
		Path<SQLStatement> minPath = null;
		for (Path<SQLStatement> p : paths) {
			Patch<ContentNode<SQLStatement>> patch = DiffUtils.diff(p.getNodes(), path.getNodes(), new NodeEqualiser());
			int cost = cost(patch);
			if (cost < minCost) {
				minCost = cost;
				minPath = p;
			}
		}
		return minPath;
	}

	private int cost(Patch<?> patch) {
		int cost = 0;
		for (Delta<?> d : patch.getDeltas()) {
			// System.out.println("Original: " + d.getOriginal());
			// System.out.println("Revised: " + d.getRevised());
			// System.out.println("--------");
			cost += Math.max(d.getOriginal().size(), d.getRevised().size());
		}
		return cost;
	}

	private static class NodeEqualiser implements Equalizer<ContentNode<SQLStatement>> {
		@Override
		public boolean equals(ContentNode<SQLStatement> original, ContentNode<SQLStatement> revised) {
			return (original.getContent().getSql().equals(revised.getContent().getSql()));
		}
	}

	private void integrate(Path<SQLStatement> closestPath, Path<SQLStatement> node) {
		LOG.debug("Integrating " + node + " into path " + closestPath);

		Patch<ContentNode<SQLStatement>> patch = DiffUtils.diff(closestPath.getNodes(), node.getNodes(),
				new NodeEqualiser());
		for (Delta<ContentNode<SQLStatement>> delta : patch.getDeltas()) {
			LOG.debug("Delta: " + delta);
			switch (delta.getType()) {
			case CHANGE:
				// TODO simplify
				BranchingNode<SQLStatement> branch = BranchingNode
						.convertToAndReplaceWithBranch(Path.fromNodeList(delta.getOriginal().getLines()));
				Path<SQLStatement> tmp = Path.fromNodeList(delta.getRevised().getLines());
				tmp.first().clearPredecessor(); // TODO improve
				tmp.last().clearSuccessor();
				branch.addTransition(tmp.first());
				// delta.getRevised().getLines().get(delta.getRevised().getLines().size()
				// - 1).setSuccessor(null);
				break;
			case DELETE:
				throw new UnsupportedOperationException();
				// break;
			case INSERT:
				// TODO simplify
				Node<SQLStatement> nodeBeforeInsert = closestPath.getNodes().get(delta.getOriginal().getPosition() - 1);
				Node<SQLStatement> lastNode = nodeBeforeInsert;
				for (Node<SQLStatement> insertNode : delta.getRevised().getLines()) {
					insertNode.setSuccessor(lastNode.getSuccessor());
					lastNode.setSuccessor(insertNode);
					lastNode = insertNode;
				}
			}
		}
		LOG.debug("Result: " + graph);
	}
	
	public static Path<SQLStatement> pathFromSequence(SQLStatementSequence sequence) {
		List<Node<SQLStatement>> nodeList = new ArrayList<>();
		Node<SQLStatement> last = null;
		for (SQLStatement stmt : sequence.getSequence()) {
			Node<SQLStatement> current = new ContentNode<>(stmt);
			if (last != null) {
				last.setSuccessor(current);
				current.setPredecessor(last);
			}
			nodeList.add(current);
			last = current;
		}
		return Path.fromNodeList(nodeList);
	}

	public static void main(String[] args) {
		// log4j basic setup
		BasicConfigurator.configure();

		SQLStatementSequence s1 = new SQLStatementSequence();
		s1.add(new SQLStatement("A"));
		s1.add(new SQLStatement("X"));
		s1.add(new SQLStatement("C"));

		SQLStatementSequence s2 = new SQLStatementSequence();
		s2.add(new SQLStatement("A"));
		s2.add(new SQLStatement("S"));
		s2.add(new SQLStatement("T"));
		s2.add(new SQLStatement("C"));

		SQLStatementSequence s3 = new SQLStatementSequence();
		s3.add(new SQLStatement("A"));
		s3.add(new SQLStatement("S"));
		s3.add(new SQLStatement("T"));
		s3.add(new SQLStatement("C"));

		SQLStatementSequence s4 = new SQLStatementSequence();
		s4.add(new SQLStatement("A"));
		s4.add(new SQLStatement("S"));
		s4.add(new SQLStatement("T"));
		// s4.add(new SQLStatement("C"));
		s4.add(new SQLStatement("D"));

		SQLStatementSequence s5 = new SQLStatementSequence();
		s5.add(new SQLStatement("A"));
		s5.add(new SQLStatement("S"));
		// s5.add(new SQLStatement("T"));
		s5.add(new SQLStatement("C"));
		// s5.add(new SQLStatement("D"));
		// s5.add(new SQLStatement("E"));

		SQLStatementsToPCM sql2pcm = new SQLStatementsToPCM();
		sql2pcm.addStatements(s1);
		sql2pcm.addStatements(s2);
		sql2pcm.addStatements(s3);
		sql2pcm.addStatements(s4);
		sql2pcm.addStatements(s5);

		sql2pcm.addStatements(s5);
	}

}
