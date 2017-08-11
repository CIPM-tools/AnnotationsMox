package org.somox.ejbmox.inspectit2pcm.aggregation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.RecognitionException;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

import de.uka.ipd.sdq.probfunction.math.IProbabilityDensityFunction;
import de.uka.ipd.sdq.probfunction.math.ManagedPDF;
import de.uka.ipd.sdq.probfunction.math.exception.StringNotPDFException;

public class StoExUtil {

    private static IProbabilityDensityFunction createDistributionOf(PCMRandomVariable rv) {
        String stoEx = rv.getSpecification();

        // create PDF from StoEx
        IProbabilityDensityFunction pdf;
        try {
            pdf = ManagedPDF.createFromString(stoEx).getPdfTimeDomain();
        } catch (StringNotPDFException e) {
            throw new RuntimeException(e);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(stoEx, e);
        }
        return pdf;
    }

    public static List<Double> drawSamples(String stoEx, int count) {
        return drawSamples(PCMHelper.createPCMRandomVariable(stoEx), count);
    }

    public static List<Double> drawSamples(PCMRandomVariable rv, int count) {
        return drawSamples(createDistributionOf(rv), count);
    }

    public static List<Double> drawSamples(IProbabilityDensityFunction pdf, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sample = pdf.drawSample();
            values.add(sample);
        }
        return values;
    }

    public static List<Interval> extractBucketsFromStoEx(String stoEx) {
        String regex = "\\(([0-9.eE+-]+);([0-9.eE+-]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stoEx);

        List<Interval> buckets = new LinkedList<>();
        double lowerBoundary = 0;
        while (matcher.find()) {
            double upperBoundary = Double.parseDouble(matcher.group(1));
            double probability = Double.parseDouble(matcher.group(2));
            buckets.add(new Interval(lowerBoundary, upperBoundary, probability));
            lowerBoundary = upperBoundary;
        }
        return buckets;
    }

    public static boolean isDoublePDF(String stoEx) {
        return stoEx.toLowerCase().startsWith("doublepdf");
    }

    public static class Interval {

        private double lowerBoundary;

        private double upperBoundary;

        private double probability;

        public Interval(double lowerBoundary, double upperBoundary, double probability) {
            this.lowerBoundary = lowerBoundary;
            this.upperBoundary = upperBoundary;
            this.probability = probability;
        }

        public double getLowerBoundary() {
            return lowerBoundary;
        }

        public double getUpperBoundary() {
            return upperBoundary;
        }

        public double getProbability() {
            return probability;
        }

    }

}
