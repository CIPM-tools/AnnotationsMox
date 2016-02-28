package org.somox.ejbmox.ejb.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.somox.configuration.SoMoXConfiguration;
import org.somox.ejbmox.util.EJBmoxUtil;
import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.IJob;
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
        return EJBmoxUtil.createEJBmoxWorkflowJobs(modelAnalyzerConfig);
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
