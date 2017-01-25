package org.somox.ejbmox.inspectit2pcm.anomalies;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;

/**
 * A collection of contiguous measurements.
 * 
 * @author Philipp Merkle
 *
 */
public class Window {

    private double startTime = Double.MAX_VALUE;

    private double endTime = Double.MIN_VALUE;

    private Collection<Measurement> measurements = new ArrayList<>();

    private boolean anomaly;

    public void add(InvocationSequence invocation) {
        // adjust statistics
        Measurement m = new Measurement(invocation.getId(), invocation.getDuration(), invocation.getEnd());
        measurements.add(m);

        // adjust start/end time
        startTime = Math.min(startTime, invocation.getStart());
        endTime = Math.max(endTime, invocation.getEnd());
    }

    public double getMedian() {
        return median(measurements);
    }

    public double getMean() {
        return mean(measurements);
    }

    protected static double mean(Collection<Measurement> values) {
        double sum = values.stream().mapToDouble(m -> m.getValue()).sum();
        double count = values.size();
        return sum / count;
    }

    protected static double median(Collection<Measurement> values) {
        Median median = new Median();
        double[] data = values.stream().mapToDouble(m -> m.getValue()).toArray();
        median.setData(data);
        return median.evaluate();
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public Collection<Measurement> getMeasurements() {
        return measurements;
    }

    /**
     * Returns the size of this window.
     * 
     * @return the number of invocation sequences represented by this window
     */
    public int size() {
        return measurements.size();
    }

    public void addAll(Window other) {
        for (Measurement m : other.getMeasurements()) {
            measurements.add(m);
            startTime = Math.min(startTime, other.getStartTime());
            endTime = Math.max(endTime, other.getEndTime());
        }
    }

    @Override
    public String toString() {
        return "Window [startTime=" + startTime + ", endTime=" + endTime + ", median=" + getMedian() + "]";
    }

    public boolean isAnomaly() {
        return anomaly;
    }

    /**
     * Marks this window as a window containing anomalies.
     * 
     * @param anomaly
     *            true, if the window contains anomalies; false, else
     */
    public void setAnomaly(boolean anomaly) {
        this.anomaly = true;
    }

}
