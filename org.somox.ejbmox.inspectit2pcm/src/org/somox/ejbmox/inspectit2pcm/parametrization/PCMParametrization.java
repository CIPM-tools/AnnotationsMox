package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.somox.ejbmox.graphlearner.SPGraph;
import org.somox.ejbmox.inspectit2pcm.graphlearner.Graph2SEFFVisitor;
import org.somox.ejbmox.inspectit2pcm.graphlearner.InvocationProbabilityVisitor;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Container that allows to collect various information required to parametrize
 * a PCM model. In a final step, the collected information is aggregated and
 * written to the PCM model to be parametrized.
 * 
 * @author Philipp Merkle
 *
 */
public class PCMParametrization implements Cloneable {

	private Map<InternalAction, List<Double>> resourceDemandMap;

	private Map<InternalAction, List<SQLStatementSequence>> sqlStatementMap;

	// private Map<String, List<Integer>> loopIterationMap;

	public PCMParametrization() {
		resourceDemandMap = new HashMap<>();
		sqlStatementMap = new HashMap<>();
	}

	public void captureSQLStatementSequence(InternalAction action, SQLStatementSequence statements) {
		if (action == null) {
			throw new IllegalArgumentException("Action may not be null");
		}
		if (statements == null) {
			throw new IllegalArgumentException("SQL Statements may not be null.");
		}
		if (!sqlStatementMap.containsKey(action)) {
			sqlStatementMap.put(action, new ArrayList<>());
		}
		sqlStatementMap.get(action).add(statements);
	}

	public void captureResourceDemand(InternalAction action, double demand) {
		if (action == null) {
			throw new IllegalArgumentException("Action may not be null");
		}
		if (demand < 0) {
			throw new IllegalArgumentException("Demand may not be negative.");
		}
		if (!resourceDemandMap.containsKey(action)) {
			resourceDemandMap.put(action, new ArrayList<>());
		}
		resourceDemandMap.get(action).add(demand);
	}

	public void mergeFrom(PCMParametrization other) {
		// merge resource demands
		for (Entry<InternalAction, List<Double>> entry : other.resourceDemandMap.entrySet()) {
			for (Double demand : entry.getValue()) {
				captureResourceDemand(entry.getKey(), demand);
			}
		}

		// merge SQL statements
		for (Entry<InternalAction, List<SQLStatementSequence>> entry : other.sqlStatementMap.entrySet()) {
			for (SQLStatementSequence stmtSequence : entry.getValue()) {
				captureSQLStatementSequence(entry.getKey(), stmtSequence);
			}
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		PCMParametrization clone = new PCMParametrization();
		clone.mergeFrom(this);
		return clone;
	}

	public void parametrize(AggregationStrategy aggregation) {
		switch (aggregation) {
		case HISTOGRAM:
			throw new UnsupportedOperationException();
			// break;
		case MEAN:
			parametrizeResourceDemandsWithMean();
			parametrizeSQLStatementsWithMean();
			break;
		case MEDIAN:
			throw new UnsupportedOperationException();
			// break;
		default:
			throw new RuntimeException("Unknown aggregation strategy: " + aggregation);
		}
	}

	private void parametrizeResourceDemandsWithMean() {
		for (Entry<InternalAction, List<Double>> e : resourceDemandMap.entrySet()) {
			InternalAction action = e.getKey();
			List<Double> demands = e.getValue();

			// calculate mean
			double sum = demands.stream().mapToDouble(Double::doubleValue).sum();
			double mean = sum / demands.size();

			// parametrize action
			PCMRandomVariable rv = PCMHelper.createPCMRandomVariable(mean);
			action.getResourceDemand_Action().get(0).setSpecification_ParametericResourceDemand(rv);
		}
	}

	// TODO simplify whole method
	private void parametrizeSQLStatementsWithMean() {

		for (Entry<InternalAction, List<SQLStatementSequence>> e : sqlStatementMap.entrySet()) {
			SQLStatementsToPCM sql2pcm = new SQLStatementsToPCM();

			// learn graph from paths (SQL statement sequences)
			InternalAction action = e.getKey();
			List<SQLStatementSequence> sequences = e.getValue();
			for (SQLStatementSequence s : sequences) {
				sql2pcm.addStatementSequence(s);
			}
			SPGraph g = sql2pcm.getLearner().getGraph();

			// create SEFF from graph (assumes "verbose" representation)
			ResourceDemandingBehaviour rdb = SeffFactory.eINSTANCE.createResourceDemandingBehaviour();
			g.toVerboseRepresentation();
			g.traverse(new InvocationProbabilityVisitor());
			g.traverse(new Graph2SEFFVisitor(), rdb); // stores SEFF in rdb
														// variable

			replaceAction(action, rdb);
		}
	}

	private void replaceAction(AbstractAction replaceAction, ResourceDemandingBehaviour behaviour) {
		// first collect all actions in a new ArrayList to avoid
		// ConcurrentModificationException thrown by EMF
		List<AbstractAction> insertActions = new ArrayList<>(behaviour.getSteps_Behaviour());
		for (AbstractAction insertAction : insertActions) {
			// ignore Start and Stop actions
			if (insertAction instanceof StartAction || insertAction instanceof StopAction) {
				continue;
			}
			insertAction.setResourceDemandingBehaviour_AbstractAction(
					replaceAction.getResourceDemandingBehaviour_AbstractAction());
		}

		AbstractAction predecessor = replaceAction.getPredecessor_AbstractAction();
		predecessor.setSuccessor_AbstractAction(PCMHelper.findStartAction(behaviour).getSuccessor_AbstractAction());

		AbstractAction successor = replaceAction.getSuccessor_AbstractAction();
		successor.setPredecessor_AbstractAction(PCMHelper.findStopAction(behaviour).getPredecessor_AbstractAction());

		// remove action that has been replaced
		replaceAction.setResourceDemandingBehaviour_AbstractAction(null);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("==== Resource Demands =============\n");
		for (Entry<InternalAction, List<Double>> e : resourceDemandMap.entrySet()) {
			builder.append(e.getKey().getEntityName() + ": " + e.getValue().toString() + "\n");
		}
		builder.append("==== SQL Statements ===============\n");
		for (Entry<InternalAction, List<SQLStatementSequence>> e : sqlStatementMap.entrySet()) {
			builder.append(e.getKey().getEntityName() + ": \n");
			for (SQLStatementSequence s : e.getValue()) {
				for (SQLStatement stmt : s.getSequence()) {
					builder.append("    " + stmt + " \n");
				}
				builder.append("--\n");
			}
			builder.append("--------\n");
		}

		return builder.toString();
	}

}
