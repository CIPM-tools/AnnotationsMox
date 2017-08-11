package org.somox.ejbmox.inspectit2pcm.aggregation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.somox.ejbmox.inspectit2pcm.util.PCMHelper;

public class BayesianBlocksAggregationStrategy implements AggregationStrategy {

    private static final String PYTHON_PATH = "C:/Python36/python.exe";

    private static final String SCRIPT_PATH = "C:/EJBMoX-mRUBiS/Eclipse/workspace/org.somox.ejbmox.inspectit2pcm/resources/org/somox/ejbmox/inspectit2pcm/aggregation/stoexbb.py";

    private static final String CSV_PATH = "C:/tmp/tmp.csv";

    @Override
    public PCMRandomVariable aggregate(Collection<Double> values, String description) {
        exportMeasurementsToCSV(values);

        String[] cmd = { PYTHON_PATH, SCRIPT_PATH, CSV_PATH };
        ProcessBuilder builder = new ProcessBuilder(cmd);
        // builder.inheritIO();

        String stoex = "";
        if (values.size() == 1) {
            stoex = Double.toString(values.iterator().next());
        } else {
            Process p;
            try {
                p = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                p.waitFor();
                stoex = output.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return PCMHelper.createPCMRandomVariable(stoex);
    }

    private void exportMeasurementsToCSV(Collection<Double> values) {
        try (FileWriter writer = new FileWriter(new File(CSV_PATH))) {
            for (double value : values) {
                writer.write(value + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
