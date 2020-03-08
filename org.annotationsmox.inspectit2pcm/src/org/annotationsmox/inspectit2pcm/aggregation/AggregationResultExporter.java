package org.annotationsmox.inspectit2pcm.aggregation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.stream.Collectors;

import org.annotationsmox.inspectit2pcm.aggregation.Evaluator.EvaluationResult;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;

public class AggregationResultExporter implements AggregationStrategy {

    private AggregationStrategy wrappedStrategy;

    private RserveConnection rConnection;

    public AggregationResultExporter(AggregationStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
        this.rConnection = lookupConnection();
        loadResource("plot.r");
    }

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        PCMRandomVariable rv = wrappedStrategy.aggregate(values, description);
        String stoEx = rv.getSpecification();

        export(values, stoEx, description);

        return rv;
    }

    private void export(Collection<Double> values, String stoEx, String description) {

        String fileEncoded = URLEncoder.encode(description.replace(" ", "_").replace("(", "").replace(")", "")
                .replace("<", "").replace(">", "").replace("@", "")).replace("%", "");
        fileEncoded = fileEncoded.length() > 100 ? fileEncoded.substring(0, 100) : fileEncoded;
        String rdsPath = "C:/tmp/result_" + fileEncoded + ".rds";
        String pdfPath = "C:/tmp/result_" + fileEncoded + ".pdf";

        Evaluator evaluator = new Evaluator();
        EvaluationResult result = evaluator.validate(values, stoEx, values.size(), new Metadata("description", description));

        MeasurementStorage measurementStore = Evaluator.createMeasurementStorage(rConnection, rdsPath);
        Evaluator.store(measurementStore, result);
        measurementStore.finish();

        try {
            EvaluationHelper.evaluateVoid(rConnection.getConnection(),
                    "plot.eval('" + rdsPath + "','" + pdfPath + "')");
        } catch (EvaluationException e) {
            throw new RuntimeException(e);
        }
    }

    private RserveConnection lookupConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    private void loadResource(String fileName) {
        InputStream is = DhistAggregationStrategy.class.getResourceAsStream(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String fileContent = br.lines().collect(Collectors.joining("\n"));

        try {
            EvaluationHelper.evaluateVoid(rConnection.getConnection(), fileContent);
        } catch (EvaluationException e) {
            e.printStackTrace(); // TODO
        }
    }

}
