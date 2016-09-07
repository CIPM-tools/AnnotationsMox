package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatement;
import org.somox.ejbmox.inspectit2pcm.model.SQLStatementSequence;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Container that allows to collect various information required to parametrize a PCM model. In a
 * final step, the collected information is aggregated and written to the PCM model to be
 * parametrized.
 * 
 * @author Philipp Merkle
 *
 */
public class PCMParametrization {

    private Map<InternalAction, List<InternalActionInvocation>> internalActionMap;

    // private Map<InternalAction, List<SQLStatementSequence>> sqlStatementMap;

    private Map<AbstractBranchTransition, Integer> branchTransitionMap;

    // private Map<String, List<Integer>> loopIterationMap;

    public PCMParametrization() {
        internalActionMap = new HashMap<>();
        branchTransitionMap = new HashMap<>();
    }

    public void captureInternalAction(InternalAction action, double duration, SQLStatementSequence statements) {
        Objects.requireNonNull(action);
        if (duration < 0) {
            throw new IllegalArgumentException("Duration may not be negative.");
        }

        InternalActionInvocation invocation = new InternalActionInvocation(duration, statements);
        internalActionMap.putIfAbsent(action, new ArrayList<>());
        internalActionMap.get(action).add(invocation);
    }

    public void captureBranchTransition(AbstractBranchTransition transition) {
        if (transition == null) {
            throw new IllegalArgumentException("Branch transition may not be null");
        }
        Integer count = branchTransitionMap.getOrDefault(transition, 0);
        branchTransitionMap.put(transition, ++count);
    }

    public void mergeFrom(PCMParametrization other) {
        // merge internal action invocations
        for (Entry<InternalAction, List<InternalActionInvocation>> entry : other.getInternalActionMap().entrySet()) {
            InternalAction internalAction = entry.getKey();
            List<InternalActionInvocation> invocations = entry.getValue();
            internalActionMap.putIfAbsent(internalAction, new ArrayList<>());
            internalActionMap.get(internalAction).addAll(invocations);
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

    public Map<InternalAction, List<InternalActionInvocation>> getInternalActionMap() {
        return internalActionMap;
    }

    public boolean isEmpty() {
        return internalActionMap.isEmpty() && branchTransitionMap.isEmpty();
    }

    public String toDebugString() {
        StringBuilder builder = new StringBuilder();

        for (Entry<InternalAction, List<InternalActionInvocation>> e : internalActionMap.entrySet()) {
            builder.append("=== " + PCMHelper.entityToString(e.getKey()) + " ===");

            List<Double> durations = new ArrayList<>();
            List<SQLStatementSequence> statementSequences = new ArrayList<>();
            for (InternalActionInvocation invocation : e.getValue()) {
                durations.add(invocation.getDuration());
                statementSequences.add(invocation.getSqlSequence());
            }

            builder.append("--- Resource Demands --------------\n");
            builder.append(durations.toString() + "\n");

            builder.append("--- SQL Statements -----------------\n");
            for (SQLStatementSequence s : statementSequences) {
                for (SQLStatement stmt : s.getSequence()) {
                    builder.append("    " + stmt + " \n");
                }
                builder.append("***\n");
            }
        }
        return builder.toString();
    }

    public void saveToFile(File file) {
        Writer fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(toDebugString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}
