package org.somox.ejbmox.util;

import org.eclipse.core.runtime.CoreException;
import org.somox.analyzer.simplemodelanalyzer.jobs.SaveSoMoXModelsJob;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.ejbmox.ejb.functionclassification.EJBmoxFunctionClassificationStrategyFactory;
import org.somox.ejbmox.ejb.ui.EJBmoxAnalzerJob;
import org.somox.gast2seff.jobs.GAST2SEFFJob;
import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;

public class EJBmoxUtil {

    private EJBmoxUtil() {
    }

    /**
     * Creates the workflow jobs for EJBmox execution.
     *
     * @param modelAnalyzerConfig
     * @return the jobs for EJBmox
     * @throws CoreException
     */
    public static SequentialBlackboardInteractingJob<SoMoXBlackboard> createEJBmoxWorkflowJobs(
            final ModelAnalyzerConfiguration modelAnalyzerConfig) throws CoreException {
        final SequentialBlackboardInteractingJob<SoMoXBlackboard> ejbMoxJob = new SequentialBlackboardInteractingJob<SoMoXBlackboard>();

        final SoMoXBlackboard soMoXBlackboard = new SoMoXBlackboard();
        ejbMoxJob.setBlackboard(soMoXBlackboard);

        ejbMoxJob.add(new EJBmoxAnalzerJob(modelAnalyzerConfig));

        final boolean reverseEngineerResourceDemandingInternalBehaviour = modelAnalyzerConfig.getSomoxConfiguration()
                .isReverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour();
        ejbMoxJob.add(new GAST2SEFFJob(reverseEngineerResourceDemandingInternalBehaviour,
                new EJBmoxFunctionClassificationStrategyFactory()));

        ejbMoxJob.add(new SaveSoMoXModelsJob(modelAnalyzerConfig.getSomoxConfiguration()));

        return ejbMoxJob;
    }

}
