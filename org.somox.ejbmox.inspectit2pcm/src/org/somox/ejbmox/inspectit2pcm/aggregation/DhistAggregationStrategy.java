package org.somox.ejbmox.inspectit2pcm.aggregation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

public class DhistAggregationStrategy implements AggregationStrategy {

    private RserveConnection rConnection;

    private int bins;

    /**
     * 
     * @param bins
     *            set to 0 to calculate bins using Sturges method
     */
    public DhistAggregationStrategy(int bins) {
        this.bins = bins;
        this.rConnection = lookupConnection();
        loadResource("dhist.r");
        loadResource("plot.r");
    }

    /**
     * 
     * @param bins
     *            set to 0 to calculate bins using Sturges method
     */
    public DhistAggregationStrategy(RserveConnection rConnection, int bins) {
        this.bins = bins;
        this.rConnection = rConnection;
        loadResource("dhist.r");
        loadResource("plot.r");
    }

    private void loadResource(String fileName) {
        InputStream is = DhistAggregationStrategy.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String dhistDefinition = br.lines().collect(Collectors.joining("\n"));

        try {
            EvaluationHelper.evaluateVoid(rConnection.getConnection(), dhistDefinition);
        } catch (EvaluationException e) {
            e.printStackTrace(); // TODO
        }
    }

    private RserveConnection lookupConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        if (values.size() < 100) {
            double sum = values.stream().collect(Collectors.summingDouble(Double::doubleValue));
            int count = values.size();
            return PCMHelper.createPCMRandomVariable(sum / count);
        }

        double[] valuesArray = values.stream().mapToDouble(Double::doubleValue).toArray();
        String stoEx = "";
        try {
            rConnection.getConnection().assign("values", valuesArray);
            String binsParameter = bins == 0 ? "" : ", nbins=" + bins;
            REXP evaluationResult = EvaluationHelper.evaluate(rConnection.getConnection(),
                    "doublePDF(values" + binsParameter + ")");
            stoEx = evaluationResult.asString();
        } catch (REngineException e) {
            throw new RuntimeException(e);
        } catch (REXPMismatchException e) {
            throw new RuntimeException(e);
        } catch (EvaluationException e) {
            throw new RuntimeException(e);
        }

        return PCMHelper.createPCMRandomVariable(stoEx);
    }

}
