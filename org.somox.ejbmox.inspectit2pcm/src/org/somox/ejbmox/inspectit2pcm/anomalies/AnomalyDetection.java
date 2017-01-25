package org.somox.ejbmox.inspectit2pcm.anomalies;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.somox.ejbmox.inspectit2pcm.model.InvocationSequence;
import org.somox.ejbmox.inspectit2pcm.rest.InvocationsProvider;

/**
 * Detects anomalies in time-series measurements. Works only for measurements that exhibit no trend.
 * <p>
 * Note that anomalies and outliers are not the same. This class targets anomalies only.
 * 
 * @author Philipp Merkle
 *
 */
public class AnomalyDetection {

    private static final Logger LOG = Logger.getLogger(AnomalyDetection.class);

    private static enum DistanceMeasure {
        MEAN, MEDIAN
    }

    private static final double MAX_DIFFERENCE_BEFORE_JOIN_FACTOR = 1.2;

    private static final double MERGE_AT_LEAST_PERCENTAGE = 0.1;

    private static final int REF_WND_SLIDING_WND_SIZE = 1;

    private static final double KEEP_BEST_PERCENTAGE = 0.7; // lower values increase data quality

    private static final int DESIRED_WINDOW_SIZE = 20; // in measurements

    private static final DistanceMeasure DISTANCE_MEASURE = DistanceMeasure.MEDIAN;

    private InvocationsProvider invocations;

    private Map<Long, List<Window>> methodToWindowsMap;

    public AnomalyDetection(InvocationsProvider invocations) {
        this.invocations = invocations;
        methodToWindowsMap = new HashMap<>();
    }

    public Set<Long> detect() {
        Set<Long> allAnomalies = new HashSet<>();

        buildWindows(DESIRED_WINDOW_SIZE);

        Set<Long> methodIds = methodToWindowsMap.keySet();
        for (Long methodId : methodIds) {
            List<Window> windowList = methodToWindowsMap.get(methodId);

            mergeSimilarContiguousWindows(windowList);

            // determine window with lowest number of anomalies, which is the largest window
            Window bestWindow = findLargestWindow(windowList);

            // sort windows from best to worst, depending on difference to best window
            sortDistanceAscending(windowList, bestWindow);

            identifyAndMarkAnomalies(windowList);

            // debug output
            dumpToFile(windowList, methodId);

            Set<Long> anomalies = buildAnomaliesSet(windowList);
            allAnomalies.addAll(anomalies);
        }

        return allAnomalies;
    }

    private Set<Long> buildAnomaliesSet(List<Window> windowList) {
        Set<Long> anomalies = new HashSet<>();
        for (Window wnd : windowList) {
            if (wnd.isAnomaly()) {
                for (Measurement m : wnd.getMeasurements()) {
                    anomalies.add(m.getId());
                }
            }
        }
        return anomalies;
    }

    public void buildWindows(int windowSize) {
        for (InvocationSequence invocation : invocations) {
            long methodId = invocation.getMethodId();

            // retrieve current window for methodId; create new window, if there is no window yet,
            // or if the current window is full
            methodToWindowsMap.putIfAbsent(methodId, new ArrayList<>());
            List<Window> windowList = methodToWindowsMap.get(methodId);
            if (windowList.isEmpty()) {
                windowList.add(new Window());
            } else if (windowList.get(windowList.size() - 1).size() == windowSize) {
                System.out.println("Built window: " + windowList.get(windowList.size() - 1));
                windowList.add(new Window());
            }
            Window currentWindow = methodToWindowsMap.get(methodId).get(windowList.size() - 1);

            // adjust current window
            currentWindow.add(invocation);
        }
    }

    private Window findLargestWindow(List<Window> windowList) {
        sortSizeDescending(windowList);
        Window reference = windowList.get(0); // largest window (with most measurements)
        return reference;
    }

    private void mergeSimilarContiguousWindows(List<Window> windowList) {
        DescriptiveStatistics slidingDifferences = new DescriptiveStatistics(REF_WND_SLIDING_WND_SIZE);
        int initialWindowsCount = windowList.size();
        int mergedWindows = 0;

        double maxDifference = Double.MAX_VALUE;
        while (windowList.size() >= 1) {
            double currentDifference = mergeContiguousWindows(windowList, maxDifference);
            slidingDifferences.addValue(currentDifference);
            mergedWindows++;
            if (mergedWindows > initialWindowsCount * MERGE_AT_LEAST_PERCENTAGE) {
                if (currentDifference > maxDifference) {
                    System.out.println(String.format("Current difference %s exceeds max difference %s",
                            currentDifference, maxDifference));
                    break; // while
                }
                maxDifference = slidingDifferences.getMean() * MAX_DIFFERENCE_BEFORE_JOIN_FACTOR;
            }
            // System.out.println(currentDifference);
        }
    }

    private void dumpToFile(List<Window> windowList, Long methodId) {
        try (FileWriter fw = new FileWriter("D:/test." + methodId)) {
            for (Window wnd : windowList) {
                for (Measurement m : wnd.getMeasurements()) {
                    fw.append(String.format("%f;%f;%b\n", m.getTimestamp(), m.getValue(), wnd.isAnomaly()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void identifyAndMarkAnomalies(List<Window> windowList) {
        int totalMeasurements = windowList.stream().mapToInt(m -> m.size()).sum();
        int measurements = 0;
        List<Window> regularWindows = new ArrayList<>();
        for (Window wnd : windowList) {
            regularWindows.add(wnd);
            measurements += wnd.size();
            if (measurements > totalMeasurements * KEEP_BEST_PERCENTAGE) {
                break;
            }
        }
        List<Window> anomalousWindows = new ArrayList<>(windowList);
        anomalousWindows.removeAll(regularWindows);
        anomalousWindows.forEach(w -> w.setAnomaly(true));
    }

    private double distance(Window w1, Window w2) {
        switch (DISTANCE_MEASURE) {
        case MEAN:
            return Math.abs(w1.getMean() - w2.getMean());
        case MEDIAN:
            return Math.abs(w1.getMedian() - w2.getMedian());
        default:
            throw new RuntimeException("Unkown distance measure:" + DISTANCE_MEASURE);
        }
    }

    private void sortSizeDescending(List<Window> windowList) {
        windowList.sort(new Comparator<Window>() {
            @Override
            public int compare(Window w1, Window w2) {
                return Double.compare(w2.size(), w1.size());
            }
        });

    }

    private void sortDistanceAscending(List<Window> windowList, Window reference) {
        windowList.sort(new Comparator<Window>() {
            @Override
            public int compare(Window w1, Window w2) {
                return Double.compare(distance(w1, reference), distance(w2, reference));
            }
        });
    }

    public double mergeContiguousWindows(List<Window> windowList, double maxDifference) {
        Window candidate1 = null;
        Window candidate2 = null;
        double smallestDifference = Double.MAX_VALUE;

        // find contiguous window pair with the smallest difference
        Window lastWindow = null;
        for (Window currentWindow : windowList) {
            if (lastWindow == null) {
                lastWindow = currentWindow;
                continue;
            }

            double difference = distance(currentWindow, lastWindow);
            if (difference < smallestDifference) {
                smallestDifference = difference;
                candidate1 = lastWindow;
                candidate2 = currentWindow;
            }

            lastWindow = currentWindow;
        }

        if (smallestDifference < maxDifference) {
            // join identified window pair
            candidate1.addAll(candidate2);
            windowList.remove(candidate2);
        }

        return smallestDifference;
    }

}
