package org.annotationsmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.annotationsmox.inspectit2pcm.aggregation.StoExUtil.Interval;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorageStartException;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.MetadataHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class Evaluator {

    /** TODO move */
    public static MeasurementStorage createMeasurementStorage(RserveConnection rConnection, String rdsFilePath) {
        MeasurementStorage measurementStore = new RMeasurementStore(rConnection, rdsFilePath);
        measurementStore.addIdExtractor(String.class, s -> s.toString());
        measurementStore.addNameExtractor(String.class, s -> s.toString());
        try {
            measurementStore.start();
        } catch (MeasurementStorageStartException e) {
            throw new RuntimeException(e);
        }
        return measurementStore;
    }

    public EvaluationResult validate(Collection<Double> referenceSample, String stoEx, int extractionSampleSize, Metadata... metadata) {
        EvaluationResult result = new EvaluationResult(referenceSample, stoEx,
                StoExUtil.drawSamples(stoEx, extractionSampleSize), metadata);
        return result;
    }

    // public void export(Collection<Double> measurements, List<Double> samples, String stoEx) {
    // storeMeasurements(measurements);
    // storeSamples(samples);
    // storeStoExIntervals(stoEx);
    // }

    public static void store(MeasurementStorage measurementStore, EvaluationResult result) {
        storeValidationSet(measurementStore, result.getValidationSet(), result.getMetadata());
        storePredictionSet(measurementStore, result.getPredictionSet(), result.getMetadata());
        storeStoExIntervals(measurementStore, result.getLearnedStoEx(), result.getMetadata());
    }

    private static void storePredictionSet(MeasurementStorage measurementStore, Collection<Double> predictionSet,
            Metadata... metadata) {
        for (double sample : predictionSet) {
            Measurement<?> m = createMeasurement("prediction", sample, metadata);
            measurementStore.put(m);
        }
    }

    private static void storeValidationSet(MeasurementStorage measurementStore, Collection<Double> validationSet,
            Metadata... metadata) {
        for (double sample : validationSet) {
            Measurement<?> m = createMeasurement("validation", sample, metadata);
            measurementStore.put(m);
        }
    }

    private static void storeStoExIntervals(MeasurementStorage measurementStore, String stoEx, Metadata... metadata) {
        Collection<Interval> intervals = StoExUtil.extractBucketsFromStoEx(stoEx);
        for (Interval interval : intervals) {
            Metadata[] localMetadata = new Metadata[] {
                    new Metadata("boundary.lower", interval.getLowerBoundary(), false),
                    new Metadata("boundary.upper", interval.getUpperBoundary(), false) };
            Metadata[] mergedMetadata = MetadataHelper.mergeMetadata(localMetadata, metadata);
            Measurement<?> m = createMeasurement("bucket.probability", interval.getProbability(), mergedMetadata);
            measurementStore.put(m);
        }

        // store the same data once again, now optimized to be used with ggplot's geom_step()
        int i = 0;
        Interval lastInterval = null;
        for (Interval interval : intervals) {
            Metadata[] localMetadata = new Metadata[] { new Metadata("boundary.lower",
                    i > 0 ? interval.getLowerBoundary() : interval.getUpperBoundary(), false),
                    new Metadata("boundary.upper", interval.getUpperBoundary(), false) };
            Metadata[] mergedMetadata = MetadataHelper.mergeMetadata(localMetadata, metadata);
            Measurement<?> m = createMeasurement("bucket.probability.opt", interval.getProbability(), mergedMetadata);
            measurementStore.put(m);
            i++;
            lastInterval = interval;
        }
        Metadata[] localMetadata = new Metadata[] {
                new Metadata("boundary.lower", lastInterval.getUpperBoundary(), false),
                new Metadata("boundary.upper", lastInterval.getUpperBoundary(), false) };
        Metadata[] mergedMetadata = MetadataHelper.mergeMetadata(localMetadata, metadata);
        Measurement<?> m = createMeasurement("bucket.probability.opt", 0, mergedMetadata);
        measurementStore.put(m);
    }

    private static Measurement<Object> createMeasurement(String metric, double value, Metadata... metadata) {
        return new Measurement<>(metric, new MeasuringPoint<Object>(""), "", value, 0, metadata);
    }

    public static class EvaluationResult {

        /**
         * actual measurements or a larger set of samples drawn from a reference distribution, e.g.
         * described by a stochastic expression
         */
        private Collection<Double> validationSet;

        /** the stochastic expression learned from the population */
        private String learnedStoEx;

        /**
         * samples drawn from the learned stochastic expression; ideally there is no significant
         * difference compared to the population
         */
        private Collection<Double> predictionSet;

        /** describes the evaluation */
        private Metadata[] metadata;

        public EvaluationResult(Collection<Double> validationSet, String learnedStoEx, Collection<Double> predictionSet,
                Metadata... metadata) {
            this.validationSet = validationSet;
            this.learnedStoEx = learnedStoEx;
            this.predictionSet = predictionSet;
            this.metadata = metadata;
        }

        public Collection<Double> getValidationSet() {
            return validationSet;
        }

        public String getLearnedStoEx() {
            return learnedStoEx;
        }

        public Collection<Double> getPredictionSet() {
            return predictionSet;
        }

        public Metadata[] getMetadata() {
            return metadata;
        }

    }

}
