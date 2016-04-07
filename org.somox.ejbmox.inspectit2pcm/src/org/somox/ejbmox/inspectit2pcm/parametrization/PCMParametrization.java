package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.InternalAction;
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
public class PCMParametrization {

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

	private void parametrizeSQLStatementsWithMean() {
		for (Entry<InternalAction, List<SQLStatementSequence>> e : sqlStatementMap.entrySet()) {
			InternalAction action = e.getKey();
			List<SQLStatementSequence> sequence = e.getValue();

			// TODO iterate over list
			if (!sequence.isEmpty()) {
				SQLStatementSequence stmts = sequence.get(sequence.size() - 1);
				for (SQLStatement stmt : stmts.getSequence()) {
					PCMHelper.insertSQLStatementAsResourceCall(action, stmt);
				}
			}

		}
	}

}
