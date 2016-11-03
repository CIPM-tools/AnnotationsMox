package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

/**
 * Calculates the arithmetic mean of supplied values and builds a {@link PCMRandomVariable}
 * representing that mean.
 * 
 * @author Philipp Merkle
 *
 */
public class MeanAggregationStrategy implements AggregationStrategy {

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values) {
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();

        return PCMHelper.createPCMRandomVariable(mean);
    }

}
