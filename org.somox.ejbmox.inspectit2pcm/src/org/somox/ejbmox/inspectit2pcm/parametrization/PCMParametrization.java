package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;

/**
 * Container that allows to collect various information required to parametrize a PCM model. In a
 * final step, the collected information is aggregated and written to the PCM model to be
 * parametrized.
 * 
 * @author Philipp Merkle
 *
 */
public class PCMParametrization {

    private Map<InternalAction, List<Double>> resourceDemandMap;

    private Map<InternalAction, List<SQLStatementSequence>> sqlStatementMap;

    private Map<AbstractBranchTransition, Integer> branchTransitionMap;

    // private Map<String, List<Integer>> loopIterationMap;

    public PCMParametrization() {
        resourceDemandMap = new HashMap<>();
        sqlStatementMap = new HashMap<>();
        branchTransitionMap = new HashMap<>();
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

    public void captureBranchTransition(AbstractBranchTransition transition) {
        if (transition == null) {
            throw new IllegalArgumentException("Branch transition may not be null");
        }
        Integer count = branchTransitionMap.getOrDefault(transition, 0);
        branchTransitionMap.put(transition, ++count);
    }

    public void mergeFrom(PCMParametrization other) {
        // merge resource demands
        for (Entry<InternalAction, List<Double>> entry : other.resourceDemandMap.entrySet()) {
            InternalAction internalAction = entry.getKey();
            List<Double> resourceDemands = entry.getValue();
            resourceDemandMap.putIfAbsent(internalAction, new ArrayList<>());
            resourceDemandMap.get(internalAction).addAll(resourceDemands);
        }

        // merge SQL statements
        for (Entry<InternalAction, List<SQLStatementSequence>> entry : other.sqlStatementMap.entrySet()) {
            for (SQLStatementSequence stmtSequence : entry.getValue()) {
                captureSQLStatementSequence(entry.getKey(), stmtSequence);
            }
        }

        // merge branch transitions
        for (Entry<AbstractBranchTransition, Integer> entry : other.branchTransitionMap.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                captureBranchTransition(entry.getKey());
            }
        }
    }

    public Map<AbstractBranchTransition, Integer> getBranchTransitionMap() {
        return branchTransitionMap;
    }

    public Map<InternalAction, List<Double>> getResourceDemandMap() {
        return resourceDemandMap;
    }

    public Map<InternalAction, List<SQLStatementSequence>> getSqlStatementMap() {
        return sqlStatementMap;
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

//    public void saveToFile() {
//        Writer fw = null;
//        try {
//            fw = new FileWriter("D:/debug.txt");
//            fw.write(toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (fw != null)
//                try {
//                    fw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//        }
//    }

}
