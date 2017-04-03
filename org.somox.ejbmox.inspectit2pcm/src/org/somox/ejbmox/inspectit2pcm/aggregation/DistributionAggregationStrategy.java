package org.somox.ejbmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.parametrization.ParametrizationUtils;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Calculates a probability distribution of supplied values and builds a {@link PCMRandomVariable}
 * representing that distribution.
 * 
 * @author Philipp Merkle
 *
 */
public class DistributionAggregationStrategy implements AggregationStrategy {

    // actual bin size may be lower, this value is no guarantee
    public static final int DEFAULT_BIN_COUNT = 30;

    private int binCount;

    /** whether outliers should be removed */
    private boolean removeOutliers;

    public DistributionAggregationStrategy() {
        this(DEFAULT_BIN_COUNT, true);
    }

    public DistributionAggregationStrategy(int binCount, boolean removeOutliers) {
        this.binCount = binCount;
        this.removeOutliers = removeOutliers;
    }

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values) {
        // 1) remove outliers
        double[] data;
        if (removeOutliers) {
            Collection<Double> valuesAfterOutlierRemoval = ParametrizationUtils.removeOutliers(values);
            data = valuesAfterOutlierRemoval.stream().mapToDouble(i -> i).toArray();
        } else {
            data = values.stream().mapToDouble(i -> i).toArray();
        }

        // 2) create histogram
        EmpiricalDistribution distribution = new EmpiricalDistribution(binCount);
        distribution.load(data);

        // 3) build StoEx from histogram
        StringBuilder stoExBuilder = new StringBuilder().append("DoublePDF[");
        int observations = data.length;

        SummaryStatistics previousBin = null;
        boolean contiguous = false;
        for (SummaryStatistics stats : distribution.getBinStats()) {
            // is bin empty?
            if (stats.getN() == 0) {
                contiguous = false;
                continue;
            }

            final double min, max;
            if (previousBin == null) {
                min = stats.getMin();
            } else {
                min = ensureGreaterThan(stats.getMin(), previousBin.getMax());
            }
            max = ensureGreaterThan(stats.getMax(), min);

            // the empirical probability
            double probability = stats.getN() / (double) observations;

            if (!contiguous) {
                // insert bin with zero probability
                stoExBuilder.append("(").append(min);
                stoExBuilder.append(";").append(0);
                stoExBuilder.append(")");
            }

            // second bin
            stoExBuilder.append("(").append(max);
            stoExBuilder.append(";").append(probability);
            stoExBuilder.append(")");

            previousBin = stats;
            contiguous = true;
        }
        stoExBuilder.append("]");

        return PCMHelper.createPCMRandomVariable(stoExBuilder.toString());
    }

    /**
     * Returns {@code currentValue}, if strictly greater than {@code lastValue}; else, returns
     * {@code lastValue + delta} with a small delta.
     * 
     * @param currentValue
     * @param lastValue
     * @return
     */
    private double ensureGreaterThan(double currentValue, double lastValue) {
        final double DELTA = 0.001;
        if (currentValue > lastValue) {
            return currentValue;
        } else {
            return lastValue + DELTA;
        }
    }

}
