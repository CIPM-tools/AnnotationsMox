package org.somox.ejbmox.graphlearner.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.somox.ejbmox.graphlearner.DiffListener;
import org.somox.ejbmox.graphlearner.GraphLearner;
import org.somox.ejbmox.graphlearner.Path;
import org.somox.ejbmox.graphlearner.PathIntegrationListener;
import org.somox.ejbmox.graphlearner.ReorganizationListener;
import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.visitor.TikZTreeForestVisitor;

/**
 * Use this tracer to track the actions of a {@link GraphLearner} as more and more paths are
 * integrated. The tracer result is TikZ code.
 * 
 * @author Philipp Merkle
 *
 */
public class TikzTracer {

    private GraphLearner learner;

    private DiffTracer diffTracer = new DiffTracer();

    private RecorganizationTracer reorganizationTracer = new RecorganizationTracer();

    private IntegationTracer integrationTracer = new IntegationTracer();

    private StringBuilder builder = new StringBuilder();

    private int[] levels = { 0, 0, 0 };

    private TikzTracer(GraphLearner learner) {
        this.learner = learner;
    }

    public DiffTracer getDiffTracer() {
        return diffTracer;
    }

    public RecorganizationTracer getReorganizationTracer() {
        return reorganizationTracer;
    }

    public IntegationTracer getIntegrationTracer() {
        return integrationTracer;
    }

    public void announceIntegration(Path p) {
        printNumbering();
        builder.append("Integrate " + printPath(p) + "\\\\\n");
    }

    private void printNumbering() {
        builder.append("\n\\ex. ");
        levels[1] = 0;
        levels[2] = 0;
    }

    private static String indent(int length) {
        String indentation = "";
        for (int i = 0; i < length; i++) {
            indentation += "\t";
        }
        return indentation;
    }

    public void forcePlot() {
        generateTikZ(Collections.emptyList());
    }

    public static TikzTracer trace(GraphLearner learner) {
        TikzTracer tracer = new TikzTracer(learner);
        learner.addDiffListener(tracer.getDiffTracer());
        learner.addReorganizationListener(tracer.getReorganizationTracer());
        learner.addIntegrationListener(tracer.getIntegrationTracer());
        return tracer;
    }

    public String getResult() {
        return builder.toString();
    }

    private class DiffTracer implements DiffListener {

        @Override
        public void insert(Path insertPath, Node nodeBeforeInsert) {
            printNumbering();
            String explanation = "Insert " + printPath(insertPath) + " after " + printNode(nodeBeforeInsert);
            builder.append(explanation + "\n");
        }

        @Override
        public void delete(Path deletePath) {
            printNumbering();
            String explanation = "Delete " + printPath(deletePath);
            builder.append(explanation + "\n");
        }

        @Override
        public void change(Path originalPath, Path revisedPath) {
            printNumbering();
            String explanation = "Change " + printPath(originalPath) + " to " + printPath(revisedPath);
            builder.append(explanation + "\n");
        }

        private void printNumbering() {
            builder.append(indent(1));
            if (levels[1] == 0) {
                builder.append("\\a. ");
            } else {
                builder.append("\\b. ");
            }
            levels[1]++;
            levels[2] = 0;
        }

    }

    private class RecorganizationTracer implements ReorganizationListener {

        private List<Node> boldNodes = new ArrayList<>();

        @Override
        public void insertSeriesSuccessor(Node reference, Node inserted) {
            printNumbering();
            String explanation = "Insert " + printNode(inserted) + " as series successor of " + printNode(reference);
            boldNodes.add(inserted);
            builder.append(explanation + ":\\\\\n");
            generateTikZ(boldNodes);
            boldNodes.clear();
        }

        @Override
        public void insertParallel(Node reference, Node inserted) {
            printNumbering();
            String explanation = "Insert " + printNode(inserted) + " parallel to " + printNode(reference);
            boldNodes.add(inserted);
            builder.append(explanation + ":\\\\\n");
            generateTikZ(boldNodes);
            boldNodes.clear();
        }

        @Override
        public void insertSeriesPredecessor(Node reference, Node inserted) {
            printNumbering();
            String explanation = "Insert " + printNode(inserted) + " as series predecessor of " + printNode(reference);
            boldNodes.add(inserted);
            builder.append(explanation + ":\\\\\n");
            generateTikZ(boldNodes);
            boldNodes.clear();
        }

        private void printNumbering() {
            builder.append(indent(2));
            if (levels[2] == 0) {
                builder.append("\\a. ");
            } else {
                builder.append("\\b. ");
            }
            levels[2]++;
        }

    }

    private class IntegationTracer implements PathIntegrationListener {

        @Override
        public void notifyIntegration(Path originalPath, Path addPath, Path combinedPath) {
            // do nothing
        }

        @Override
        public void notifyClosestPath(Path path) {
            builder.append(" into closest path " + printPath(path) + "\n");
        }

    }

    private void generateTikZ(List<Node> boldNodes) {
        TikZTreeForestVisitor v = new TikZTreeForestVisitor(boldNodes);
        learner.getGraph().traverse(v, 0);

        builder.append(v.asString());
        builder.append("\n");
    }

    private String printPath(Path path) {
        return "\\texttt{" + path.excludeEpsilon().toString().replaceAll(" ", "") + "}";
    }

    private String printNode(Node node) {
        return "\\texttt{" + node.toString().replace("e", "$\\epsilon$") + "}";
    }

}
