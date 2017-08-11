package org.somox.ejbmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.parametrization.ParametrizationUtils;

public class OutlierFilter implements AggregationStrategy {

    private AggregationStrategy wrappedStrategy;

    public OutlierFilter(AggregationStrategy wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
    }

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        Collection<Double> cleansedValues = ParametrizationUtils.removeOutliers(values);
        return wrappedStrategy.aggregate(cleansedValues, description);
    }

}
