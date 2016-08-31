package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

public class MeanAggregationStrategy implements AggregationStrategy {

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values) {
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();

        return PCMHelper.createPCMRandomVariable(mean);
    }

}
