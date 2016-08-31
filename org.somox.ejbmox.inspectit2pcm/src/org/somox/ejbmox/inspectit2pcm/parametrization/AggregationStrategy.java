package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;

public interface AggregationStrategy {

    public PCMRandomVariable aggregate(Collection<Double> values);
    
}
