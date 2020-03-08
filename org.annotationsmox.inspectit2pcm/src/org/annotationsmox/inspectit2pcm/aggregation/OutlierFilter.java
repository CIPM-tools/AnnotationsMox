package org.annotationsmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.annotationsmox.inspectit2pcm.parametrization.ParametrizationUtils;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

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
