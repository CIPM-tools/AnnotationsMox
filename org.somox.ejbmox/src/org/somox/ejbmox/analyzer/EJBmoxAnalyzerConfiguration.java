package org.somox.ejbmox.analyzer;

import org.somox.ui.runconfig.ModelAnalyzerConfiguration;

public class EJBmoxAnalyzerConfiguration extends ModelAnalyzerConfiguration<EJBmoxConfiguration> {

    @Override
    public void setDefaults() {
        this.moxConfiguration = new EJBmoxConfiguration();
    }

}
