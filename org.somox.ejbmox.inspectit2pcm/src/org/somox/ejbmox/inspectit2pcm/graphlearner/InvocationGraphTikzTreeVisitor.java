package org.somox.ejbmox.inspectit2pcm.graphlearner;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.somox.ejbmox.graphlearner.node.Node;
import org.somox.ejbmox.graphlearner.util.TikzTreeVisitorFactory;
import org.somox.ejbmox.graphlearner.visitor.TikZTreeVisitor;

public class InvocationGraphTikzTreeVisitor extends TikZTreeVisitor {

    private static NumberFormat PROBABILITY_FORMATTER = new DecimalFormat("#0.00");

    @Override
    protected String printNode(Node node, String name, Shape shape, boolean bold, String label) {
        if (label != null) {
            throw new RuntimeException("Not yet supported");
        }
        Integer invocations = (Integer) node.getAttribute(NodeAttribute.INVOCATION_COUNT);
        Double probability = (Double) node.getAttribute(NodeAttribute.INVOCATION_PROBABILITY);
        probability = probability != null ? probability : 0;
        String invocationsLabel = invocations != null ? invocations.toString() : "";
        return super.printNode(node, name, shape, bold, invocationsLabel + "/" + formatProbability(probability));
    }

    private String formatProbability(double probability) {
        return PROBABILITY_FORMATTER.format(probability).replace(",", ".").replace("0.", ".").replace("1.00", "1");
    }

    public static TikzTreeVisitorFactory getFactory() {
        return new TikzTreeVisitorFactory() {

            @Override
            public TikZTreeVisitor create() {
                return new InvocationGraphTikzTreeVisitor();
            }
        };
    }

}
