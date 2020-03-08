package org.annotationsmox.inspectit2pcm.model;

/**
 * 
 * @author Patrice Bouillet (parts of this class have been copied from InspectIT)
 * @author Philipp Merkle
 *
 */
public class TimerData {

    /**
     * The count.
     */
    protected long count = 0;

    /**
     * The complete duration.
     */
    protected double duration = 0;

    /**
     * The cpu complete duration.
     */
    protected double cpuDuration = 0;

    /**
     * Exclusive duration.
     */
    protected double exclusiveDuration;

    public long getCount() {
        return count;
    }

    public double getDuration() {
        return duration;
    }

    public double getCpuDuration() {
        return cpuDuration;
    }

    public double getExclusiveDuration() {
        return exclusiveDuration;
    }

    @Override
    public String toString() {
        return "TimerData [count=" + count + ", duration=" + duration + ", cpuDuration=" + cpuDuration
                + ", exclusiveDuration=" + exclusiveDuration + "]";
    }

}
