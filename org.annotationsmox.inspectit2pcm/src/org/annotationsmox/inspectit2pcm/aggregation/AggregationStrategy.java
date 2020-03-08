package org.annotationsmox.inspectit2pcm.aggregation;

import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.entity.Entity;

/**
 * Aggregates input values according to a certain aggregation strategy (e.g., mean, median,
 * distribution). Builds a {@link PCMRandomVariable} representing the aggregation result.
 * 
 * @author Philipp Merkle
 *
 */
public interface AggregationStrategy {

    /**
     * Aggregates the given values and returns the aggregation result as a random variable.
     * 
     * @param values
     *            the values to be aggregated
     * @return the random variable resembling the aggregation result
     */
    public PCMRandomVariable aggregate(Collection<Double> values, String description);

    public default PCMRandomVariable aggregate(Collection<Double> values) {
        return aggregate(values, "default");
    }

    public default PCMRandomVariable aggregate(Collection<Double> values, Entity action) {
        return aggregate(values, action.getEntityName());
    }

}
