package org.somox.ejbmox.pcmtx.launch;

import java.util.Map;

import org.somox.ejbmox.analyzer.EJBmoxConfiguration;
import org.somox.ejbmox.pcmtx.PCMTXConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;
import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionConfigurationBuilder;

public class PCMTXConfigurationBuilder extends AbstractWorkflowExtensionConfigurationBuilder {

    @Override
    public AbstractExtensionJobConfiguration buildConfiguration(Map<String, Object> attributes) {
        PCMTXConfiguration config = new PCMTXConfiguration(attributes);
        config.setEjbMoXConfiguration(new EJBmoxConfiguration(attributes));
        return config;
    }

}
