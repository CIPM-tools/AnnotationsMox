package org.somox.ejbmox.ejb.ui;

import java.util.ArrayList;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.somox.ui.runconfig.ModelAnalyzerTabGroup;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.DebugEnabledCommonTab;

/**
 * This class represents the Tab group for the launch configuration
 * 
 * @author langhamm
 *
 */
public class EJBmoxModelAnalyzerTabGroup extends ModelAnalyzerTabGroup {

    @Override
    public void createTabs(final ILaunchConfigurationDialog dialog, final String mode) {
        final ArrayList<ILaunchConfigurationTab> tabList = this.getCoreAnalyzerTabs();

        tabList.add(new DebugEnabledCommonTab());
        this.setTabs(tabList.toArray(new ILaunchConfigurationTab[0]));
    }
}
