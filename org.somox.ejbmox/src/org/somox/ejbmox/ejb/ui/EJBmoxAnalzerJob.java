package org.somox.ejbmox.ejb.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.somox.analyzer.AnalysisResult;
import org.somox.analyzer.ModelAnalyzerException;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.configuration.SoMoXConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzer;
import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class EJBmoxAnalzerJob extends AbstractBlackboardInteractingJob<SoMoXBlackboard> {

    private final SoMoXConfiguration somoxConfiguration;

    public EJBmoxAnalzerJob(final ModelAnalyzerConfiguration config) {
        this.somoxConfiguration = config.getSomoxConfiguration();
    }

    @Override
    public void execute(final IProgressMonitor arg0) throws JobFailedException, UserCanceledException {
        final EJBmoxAnalyzer ejbMoxAnalzer = new EJBmoxAnalyzer();
        try {
            final AnalysisResult analysisResult = ejbMoxAnalzer.analyze(this.somoxConfiguration, null,
                    new NullProgressMonitor());
            this.myBlackboard.setAnalysisResult(analysisResult);
        } catch (final ModelAnalyzerException e) {
            throw new JobFailedException("SoMoX Failed", e);
        }
    }

    @Override
    public String getName() {
        return "EJBmox Analyzer Job";
    }

    @Override
    public void cleanup(final IProgressMonitor arg0) throws CleanupFailedException {
        // TODO Auto-generated method stub

    }

}
