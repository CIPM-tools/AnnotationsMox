package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ParametrizationUtils {

    public static Collection<Double> removeOutliers(Collection<Double> values) {
        Collection<Double> cleansedValues = new ArrayList<>();
        Collection<Double> outliers = new ArrayList<>();

        double[] data = values.stream().mapToDouble(i -> i).toArray();

        DescriptiveStatistics statistics = new DescriptiveStatistics(data);
        double sd = statistics.getStandardDeviation();
        double mean = statistics.getMean();

        for (double v : data) {
            // TODO this rule could be improved / fine-tuned
            if (Math.abs(v - mean) > 2 * sd) { // outlier
                outliers.add(v);
                continue;
            }
            cleansedValues.add(v);
        }

        // TODO log outliers for later inspection

        return cleansedValues;
    }

}
