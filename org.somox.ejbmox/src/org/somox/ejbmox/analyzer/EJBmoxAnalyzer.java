package org.somox.ejbmox.analyzer;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzer;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.analyzer.SimpleAnalysisResult;
import org.somox.analyzer.simplemodelanalyzer.builder.ComponentBuilder;
import org.somox.analyzer.simplemodelanalyzer.builder.PCMSystemBuilder;
import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.extractor.ExtractionResult;
import org.somox.kdmhelper.KDMReader;

public class EJBmoxAnalyzer implements ModelAnalyzer<EJBmoxConfiguration> {

    private ModelAnalyzer.Status status = ModelAnalyzer.Status.READY;

    @Override
    public void init() {
        this.status = Status.READY;
    }

    @Override
    public AnalysisResult analyze(final EJBmoxConfiguration somoxConfiguration,
            final HashMap<String, ExtractionResult> extractionResultMap, final IProgressMonitor progressMonitor)
                    throws ModelAnalyzerException {
        this.status = Status.RUNNING;

        final SimpleAnalysisResult analysisResult = this.initializeAnalysisResult();

        this.analyzeProjectWithJaMoPP(somoxConfiguration, analysisResult);

        final EJBmoxPCMRepositoryModelCreator eJBmoxPCMRepositoryModelCreator = new EJBmoxPCMRepositoryModelCreator(
                analysisResult.getRoot().getCompilationUnits(), analysisResult);
        eJBmoxPCMRepositoryModelCreator.createStaticArchitectureModel();

        this.createPCMSystem(somoxConfiguration, analysisResult);

        this.status = Status.FINISHED;
        return analysisResult;
    }

    private void createPCMSystem(final EJBmoxConfiguration ejbmoxConfiguration,
            final SimpleAnalysisResult analysisResult) {
        final ComponentBuilder dummyComponentBuilder = new ComponentBuilder(analysisResult.getRoot(),
                ejbmoxConfiguration.convertToSoMoXConfiguration(), analysisResult);
        final PCMSystemBuilder pcmSystemBuilder = new PCMSystemBuilder(analysisResult.getRoot(), null, analysisResult,
                dummyComponentBuilder);
        pcmSystemBuilder.buildSystemModel();
    }

    private void analyzeProjectWithJaMoPP(final AbstractMoxConfiguration moxConfiguration,
            final AnalysisResult analysisResult) throws ModelAnalyzerException {
        final KDMReader jaMoPPReader = new KDMReader();
        final String projectName = moxConfiguration.getFileLocations().getProjectName();
        try {
            jaMoPPReader.loadProject(projectName);
        } catch (final IOException e) {
            throw new ModelAnalyzerException("Error: Could not load project " + projectName, e);
        }
        analysisResult.setRoot(jaMoPPReader.getRoot());
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

}
