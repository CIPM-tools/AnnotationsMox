package org.somox.ejbmox.ejb.ui;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxAnalyzerConfiguration;
import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.workflow.EJBMoXJob;

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
        extends AbstractWorkflowBasedLaunchConfigurationDelegate<EJBmoxAnalyzerConfiguration, Workflow> {

    @Override
    protected IJob createWorkflowJob(final EJBmoxAnalyzerConfiguration modelAnalyzerConfig, final ILaunch mode)
            throws CoreException {
        return new EJBMoXJob(modelAnalyzerConfig);
    }

    @Override
    protected EJBmoxAnalyzerConfiguration deriveConfiguration(final ILaunchConfiguration launchconfiguration,
            final String mode) throws CoreException {
        final EJBmoxAnalyzerConfiguration modelAnalyzerConfig = new EJBmoxAnalyzerConfiguration();
        final Map<String, Object> attributeMap = launchconfiguration.getAttributes();
        final EJBmoxConfiguration ejbMoxConfiguration = new EJBmoxConfiguration(attributeMap);
        modelAnalyzerConfig.setMoxConfiguration(ejbMoxConfiguration);
        return modelAnalyzerConfig;
    }

}
