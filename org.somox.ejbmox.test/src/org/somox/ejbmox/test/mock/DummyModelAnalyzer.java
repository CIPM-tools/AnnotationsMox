package org.somox.ejbmox.test.mock;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzer;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.configuration.SoMoXConfiguration;
import org.somox.extractor.ExtractionResult;

public class DummyModelAnalyzer implements ModelAnalyzer {

    @Override
    public void init() {

    }

    @Override
    public AnalysisResult analyze(final SoMoXConfiguration somoxConfiguration,
            final HashMap<String, ExtractionResult> extractionResultMap, final IProgressMonitor progressMonitor)
                    throws ModelAnalyzerException {
        return null;
    }

    @Override
    public Status getStatus() {
        return null;
    }

}
