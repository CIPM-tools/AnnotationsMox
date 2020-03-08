package org.annotationsmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.annotationsmox.inspectit2pcm.util.PCMHelper;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

public class AggregationStrategyValidator implements AggregationStrategy {

    private static final double ACCEPTABLE_ERROR = 0.20; // 20% (fraction between 0...1)

    private AggregationStrategy wrappedStrategy;

    public AggregationStrategyValidator(AggregationStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        PCMRandomVariable rv = wrappedStrategy.aggregate(values, description);

        boolean acceptable = validate(rv, values);
        if (!acceptable) {
            System.out.println("Deviation not acceptable. Falling back to mean.");
            double median = statistics(values).getPercentile(50);
            return PCMHelper.createPCMRandomVariable(median);
        }

        return rv;
    }

    private boolean validate(PCMRandomVariable rv, Collection<Double> values) {
        if (StoExUtil.isDoublePDF(rv.getSpecification())) {
            DescriptiveStatistics expectedStatistics = statistics(values);
            DescriptiveStatistics sampleStatistics = statistics(StoExUtil.drawSamples(rv, 1_000_000));

            System.out.println("-------------------");
            System.out.println("Expected Mean: " + expectedStatistics.getMean());
            System.out.println("Actual Mean: " + sampleStatistics.getMean());

            double acceptableDelta = calculateTotalAcceptableError(expectedStatistics.getMean());
            double actualDelta = Math.abs(expectedStatistics.getMean() - sampleStatistics.getMean());

            if (actualDelta > acceptableDelta) {
                return false;
            }
        }
        return true;
    }

    private double calculateTotalAcceptableError(double expectedMean) {
        return expectedMean * ACCEPTABLE_ERROR;
    }

    private DescriptiveStatistics statistics(Collection<Double> values) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (Double v : values) {
            statistics.addValue(v);
        }
        return statistics;
    }

}
