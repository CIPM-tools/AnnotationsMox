package org.somox.ejbmox.pcmtx;

import java.util.Map;

import org.somox.ejbmox.analyzer.EJBmoxConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;

public class PCMTXConfiguration extends AbstractExtensionJobConfiguration {

    private EJBmoxConfiguration ejbMoXConfiguration;

    private Map<String, Object> attributes;

    public PCMTXConfiguration(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public EJBmoxConfiguration getEjbMoXConfiguration() {
        return ejbMoXConfiguration;
    }

    public void setEjbMoXConfiguration(EJBmoxConfiguration ejbMoXConfiguration) {
        this.ejbMoXConfiguration = ejbMoXConfiguration;
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaults() {
        // TODO Auto-generated method stub
    }

}
