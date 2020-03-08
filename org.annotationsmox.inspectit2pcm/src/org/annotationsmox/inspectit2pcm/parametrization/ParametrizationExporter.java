package org.annotationsmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.palladiosimulator.pcm.seff.InternalAction;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorageStartException;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class ParametrizationExporter {

    // TODO make configurable
    private static final String RDS_FILE = "C:\\tmp\\parametrization.rds";

    private MeasurementStorage measurementStore;

    private PCMParametrization parametrization;

    public ParametrizationExporter(PCMParametrization parametrization) {
        this.parametrization = parametrization;
    }

    public void init() {
        RserveConnection rConnection = lookupConnection();
        measurementStore = new RMeasurementStore(rConnection, RDS_FILE);
        measurementStore.addIdExtractor(String.class, s -> s.toString());
        measurementStore.addNameExtractor(String.class, s -> s.toString());
        try {
            measurementStore.start();
        } catch (MeasurementStorageStartException e) {
            throw new RuntimeException(e);
        }
    }

    public void finish() {
        measurementStore.finish();
    }

    private RserveConnection lookupConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    public void exportToR() {
        init();
        for (Entry<InternalAction, List<InternalActionInvocation>> e : parametrization.getInternalActionMap()
                .entrySet()) {
            InternalAction action = e.getKey();
            List<Double> durations = new ArrayList<>();
            // List<SQLStatementSequence> statementSequences = new ArrayList<>();
            for (InternalActionInvocation invocation : e.getValue()) {
                durations.add(invocation.getDuration());
                double duration = invocation.getDuration();
                store(action, duration);
                // statementSequences.add(invocation.getSqlSequence());
            }
        }
        finish();
    }

    public void store(InternalAction action, double duration) {
        Measurement<Object> m = new Measurement<>("executiontime", new MeasuringPoint<Object>(""), "", duration, 0,
                new Metadata("action", action.getEntityName()));
        measurementStore.put(m);
    }

}
