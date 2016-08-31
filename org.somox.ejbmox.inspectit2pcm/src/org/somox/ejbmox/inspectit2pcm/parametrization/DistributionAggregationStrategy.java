package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.Collection;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

public class DistributionAggregationStrategy implements AggregationStrategy {

    public static final int BIN_COUNT = 10;

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values) {
        double[] data = values.stream().mapToDouble(i -> i).toArray();

        EmpiricalDistribution distribution = new EmpiricalDistribution(BIN_COUNT);
        distribution.load(data);

        StringBuilder stoExBuilder = new StringBuilder().append("DoublePDF[");
        int observations = data.length;

        SummaryStatistics previousBin = null;
        double lastFrequency = 0;
        double previousValue = -1;
        for (SummaryStatistics stats : distribution.getBinStats()) {
            // is bin empty? Then, the bin's boundaries (min/max) are undefinied (Double.NaN) and
            // need to be treated differently
            double value;
            if (stats.getN() == 0) {
                if (previousBin.getN() != 0) {
                    value = ensureValueGreaterThanPreviousValue(previousBin.getMax(), previousValue);
                    stoExBuilder.append("(").append(value);
                    stoExBuilder.append(";").append(lastFrequency).append(")");
                } else {
                    value = previousValue;
                }
                lastFrequency = 0;
            } else {
                value = ensureValueGreaterThanPreviousValue(stats.getMin(), previousValue);
                stoExBuilder.append("(").append(value);
                stoExBuilder.append(";").append(lastFrequency).append(")");
                lastFrequency = stats.getN() / (double) observations;
            }
            previousBin = stats;
            previousValue = value;
        }
        stoExBuilder.append("(").append(ensureValueGreaterThanPreviousValue(previousBin.getMax(), previousValue));
        stoExBuilder.append(";").append(lastFrequency).append(")").append("]");

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
    private double ensureValueGreaterThanPreviousValue(double currentValue, double lastValue) {
        final double DELTA = 0.001;
        if (currentValue > lastValue) {
            return currentValue;
        } else {
            return lastValue + DELTA;
        }
    }

}
