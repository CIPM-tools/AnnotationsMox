package org.annotationsmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.annotationsmox.inspectit2pcm.util.PCMHelper;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

/**
 * Calculates the arithmetic mean of supplied values and builds a {@link PCMRandomVariable}
 * representing that mean.
 * 
 * @author Philipp Merkle
 *
 */
public class MeanAggregationStrategy implements AggregationStrategy {

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();

        return PCMHelper.createPCMRandomVariable(mean);
    }

}
