package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.BasicConfigurator;
import org.somox.ejbmox.inspectit2pcm.aggregation.AggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.aggregation.BayesianBlocksAggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.aggregation.DhistAggregationStrategy;
import org.somox.ejbmox.inspectit2pcm.aggregation.Evaluator;
import org.somox.ejbmox.inspectit2pcm.aggregation.Evaluator.EvaluationResult;
import org.somox.ejbmox.inspectit2pcm.aggregation.StoExUtil;
import org.somox.ejbmox.inspectit2pcm.aggregation.UniformBinWidthAggregationStrategy;

import de.uka.ipd.sdq.probfunction.math.exception.StringNotPDFException;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorageStartException;
import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.MetadataHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.RMeasurementStore;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;

public class EvaluateAggregationStrategy {

    private static final int EXPERIMENT_REPETITIONS = 1;

    /** the number of samples used to learn a StoEx */
    private static final int TRAINING_SAMPLE_SIZE = 10_000;

    /** the number of samples used to validate the StoEx's accuracy */
    private static final int REFERENCE_SAMPLE_SIZE = 100_000;

    private AggregationStrategy strategy;

    private Metadata[] globalMetadata;

    public EvaluateAggregationStrategy(AggregationStrategy strategy, Metadata... metadata) {
        this.strategy = strategy;
        this.globalMetadata = metadata;
    }

    public static Collection<Double> drawSamplesFromStoExFromHeikosDiss(int samples)
            throws StringNotPDFException, RecognitionException {
        String stoExDissHeiko = "DoublePDF[(10.0;0.0)(73.0;0.6)(79.0;0.2)(84.0;0.13)(93.0;0.07)]";
        return StoExUtil.drawSamples(stoExDissHeiko, samples);
    }

    private static Collection<Double> drawSamplesFromBetaDistribution(double alpha, double beta, int factor,
            int samples) {
        return drawSamples(new BetaDistribution(2, 5), samples).stream().map(v -> v * factor)
                .collect(Collectors.toList());
    }

    private static Collection<Double> drawSamplesFromCombinedDistribution(AbstractRealDistribution distributionA,
            AbstractRealDistribution distributionB, double[] weights, int samples) throws RecognitionException {
        // calculate expected statistics
        Collection<Double> expectedSamples = drawSamples(distributionA, (int) (samples * weights[0]));
        expectedSamples.addAll(drawSamples(distributionB, (int) (samples * weights[1])));
        // expectedSamples.addAll(drawSamples(distributionC, (int) (samples * weights[2])));

        // remove negative values
        expectedSamples = expectedSamples.stream().filter(d -> d >= 0).collect(Collectors.toList());

        return expectedSamples;
    }

    private EvaluationResult learnStoExAndEvaluate(Collection<Double> trainingSample,
            Collection<Double> referenceSample, boolean addOutliers, String distributionName) {
        if (addOutliers) {
            trainingSample.addAll(createOutliers(trainingSample));
            referenceSample.addAll(createOutliers(referenceSample));
        }

        // learn stoEx via aggregation strategy
        String stoEx = strategy.aggregate(trainingSample).getSpecification();

        // evaluate learned stoEx
        Metadata[] metadata = MetadataHelper.mergeMetadata(new Metadata[] {
                new Metadata("distribution", distributionName), new Metadata("outliers", addOutliers, false) },
                globalMetadata);
        Evaluator eval = new Evaluator();
        return eval.validate(referenceSample, stoEx, REFERENCE_SAMPLE_SIZE, metadata);
    }

    private static Collection<Double> drawSamples(AbstractRealDistribution pdf, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sample = pdf.sample();
            values.add(sample);
        }
        return values;
    }

    public static void main(String[] args)
            throws MeasurementStorageStartException, StringNotPDFException, RecognitionException, IOException {
        initializeLogging();

        RserveConnection rConnection = new RserveConnection();
        rConnection.connect();

        for (boolean addOutliers : new boolean[] { true, false }) {
            int iterations = EXPERIMENT_REPETITIONS;
            for (int i = 1; i <= iterations; i++) {
                System.out.println("Starting iteration " + i);
                System.out.println("Generation of outliers is " + (addOutliers ? "enabled" : "disabled"));

                String rdsFile = File.createTempFile("results", ".rds", new File("F:/")).getAbsolutePath();

                List<AggregationStrategyAndMetadata> strategies = new LinkedList<>();
                strategies.add(new AggregationStrategyAndMetadata(new DhistAggregationStrategy(rConnection, 0),
                        new Metadata("strategy", "dhist"), new Metadata("bins", "sturges")));
                // strategies.add(new AggregationStrategyAndMetadata(new
                // DhistAggregationStrategy(rConnection, 10),
                // new Metadata("strategy", "dhist(bins=10)"), new Metadata("bins", 10)));
                // strategies.add(new AggregationStrategyAndMetadata(new
                // DhistAggregationStrategy(rConnection, 30),
                // new Metadata("strategy", "dhist(bins=30)"), new Metadata("bins", 30)));
                strategies.add(new AggregationStrategyAndMetadata(new BayesianBlocksAggregationStrategy(),
                        new Metadata("strategy", "Bayesian Blocks")));
                // strategies.add(new AggregationStrategyAndMetadata(new
                // UniformBinWidthAggregationStrategy(10, false),
                // new Metadata("strategy", "Uniform Width 10")));
                strategies.add(new AggregationStrategyAndMetadata(new UniformBinWidthAggregationStrategy(20, false),
                        new Metadata("strategy", "Uniform Width")));

                List<EvaluationResult> results = new LinkedList<>();
                for (AggregationStrategyAndMetadata strategy : strategies) {
                    EvaluateAggregationStrategy es = new EvaluateAggregationStrategy(strategy.getStrategy(),
                            strategy.getMetadata());

                    // exponential
                    AbstractRealDistribution distributionA = new ExponentialDistribution(10);
                    Collection<Double> trainingSetA = drawSamples(distributionA, TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetA = drawSamples(distributionA, REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetA, validationSetA, addOutliers, "Exponential"));

                    // boxed
                    Collection<Double> trainingSetB = drawSamplesFromStoExFromHeikosDiss(TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetB = drawSamplesFromStoExFromHeikosDiss(REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetB, validationSetB, addOutliers, "Boxed"));

                    // normal
                    AbstractRealDistribution distributionC = new NormalDistribution(50, 10);
                    Collection<Double> trainingSetC = drawSamples(distributionC, TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetC = drawSamples(distributionC, REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetC, validationSetC, addOutliers, "Normal"));

                    // uniform
                    AbstractRealDistribution distributionD = new UniformRealDistribution(10, 90);
                    Collection<Double> trainingSetD = drawSamples(distributionD, TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetD = drawSamples(distributionD, REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetD, validationSetD, addOutliers, "Uniform"));

                    // beta
                    Collection<Double> trainingSetE = drawSamplesFromBetaDistribution(2, 5, 100, TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetE = drawSamplesFromBetaDistribution(2, 5, 100, REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetE, validationSetE, addOutliers, "Beta"));

                    // new NormalDistribution(80, 30)
                    // combined
                    Collection<Double> trainingSetF = drawSamplesFromCombinedDistribution(
                            new NormalDistribution(20, 10), new NormalDistribution(50, 5), new double[] { 0.7, 0.3 },
                            TRAINING_SAMPLE_SIZE);
                    Collection<Double> validationSetF = drawSamplesFromCombinedDistribution(
                            new NormalDistribution(20, 10), new NormalDistribution(50, 5), new double[] { 0.7, 0.3 },
                            REFERENCE_SAMPLE_SIZE);
                    results.add(es.learnStoExAndEvaluate(trainingSetF, validationSetF, addOutliers, "Combined"));
                }

                MeasurementStorage measurementStore = new RMeasurementStore(rConnection, rdsFile);
                measurementStore.addIdExtractor(String.class, s -> s.toString());
                measurementStore.addNameExtractor(String.class, s -> s.toString());
                try {
                    measurementStore.start();
                } catch (MeasurementStorageStartException e) {
                    throw new RuntimeException(e);
                }

                for (EvaluationResult evaluationResult : results) {
                    Evaluator.store(measurementStore, evaluationResult);
                }

                measurementStore.finish();
            }
        }

        rConnection.disconnect();
    }

    private Collection<Double> createOutliers(Collection<Double> samples) {
        double[] samplesPrimitve = samples.stream().mapToDouble(Double::doubleValue).toArray();
        DescriptiveStatistics statistics = new DescriptiveStatistics(samplesPrimitve);
        int count = samples.size() / 1000 * 2;
        double min = statistics.getMax(); // + statistics.getStandardDeviation() * 5;
        double max = statistics.getMax() + statistics.getStandardDeviation() * 10;
        return drawSamples(new UniformRealDistribution(min, max), count);
    }

    private static void initializeLogging() {
        BasicConfigurator.configure();
    }

    private static class AggregationStrategyAndMetadata {

        private AggregationStrategy strategy;

        private Metadata[] metadata;

        public AggregationStrategyAndMetadata(AggregationStrategy strategy, Metadata... metadata) {
            this.strategy = strategy;
            this.metadata = metadata;
        }

        public AggregationStrategy getStrategy() {
            return strategy;
        }

        public Metadata[] getMetadata() {
            return metadata;
        }

    }

}
