package org.somox.ejbmox.ejb.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.somox.analyzer.simplemodelanalyzer.jobs.SaveSoMoXModelsJob;
import org.somox.analyzer.simplemodelanalyzer.jobs.SimpleModelAnalyzerJob;
import org.somox.analyzer.simplemodelanalyzer.jobs.SoMoXBlackboard;
import org.somox.configuration.SoMoXConfiguration;
import org.somox.ejbmox.ejb.functionclassification.EJBmoxFunctionClassificationStrategyFactory;
import org.somox.gast2seff.jobs.GAST2SEFFJob;
import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.launchconfig.AbstractWorkflowBasedLaunchConfigurationDelegate;

/**
 * Class that creates the EJBmox workflow.
 *
 * @author langhamm
 *
 */
public class EJBmoxModelAnalyzerConfigurationDelegate
        extends AbstractWorkflowBasedLaunchConfigurationDelegate<ModelAnalyzerConfiguration, Workflow> {

    @Override
    protected IJob createWorkflowJob(final ModelAnalyzerConfiguration modelAnalyzerConfig, final ILaunch mode)
            throws CoreException {
        final SequentialBlackboardInteractingJob<SoMoXBlackboard> ejbMoxJob = new SequentialBlackboardInteractingJob<SoMoXBlackboard>();
        final SoMoXBlackboard soMoXBlackboard = new SoMoXBlackboard();
        ejbMoxJob.setBlackboard(soMoXBlackboard);

        ejbMoxJob.add(new SimpleModelAnalyzerJob(modelAnalyzerConfig));
        final boolean reverseEngineerResourceDemandingInternalBehaviour = modelAnalyzerConfig.getSomoxConfiguration()
                .isReverseEngineerInternalMethodsAsResourceDemandingInternalBehaviour();
        ejbMoxJob.add(new GAST2SEFFJob(reverseEngineerResourceDemandingInternalBehaviour,
                new EJBmoxFunctionClassificationStrategyFactory()));
        ejbMoxJob.add(new SaveSoMoXModelsJob(modelAnalyzerConfig.getSomoxConfiguration()));

        return ejbMoxJob;
    }

    @Override
    protected ModelAnalyzerConfiguration deriveConfiguration(final ILaunchConfiguration launchconfiguration,
            final String mode) throws CoreException {
        final ModelAnalyzerConfiguration modelAnalyzerConfig = new ModelAnalyzerConfiguration();
        final Map<String, Object> attributeMap = launchconfiguration.getAttributes();
        final SoMoXConfiguration somoxConfiguration = new SoMoXConfiguration(attributeMap);
        modelAnalyzerConfig.setSomoxConfiguration(somoxConfiguration);
        return modelAnalyzerConfig;
    }

}
