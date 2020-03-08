package org.annotationsmox.inspectit2pcm.anomalies;

public class Measurement {

    private long id;
    
    private double value;
    
    private double timestamp;
    
    public Measurement(long id, double value, double timestamp) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }
    
    public double getValue() {
        return value;
    }

    public double getTimestamp() {
        return timestamp;
    }
    
}
