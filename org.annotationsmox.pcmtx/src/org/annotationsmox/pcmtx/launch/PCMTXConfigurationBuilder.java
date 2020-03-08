package org.annotationsmox.pcmtx.launch;

import java.util.Map;

import org.annotationsmox.analyzer.AnnotationsMoxConfiguration;
import org.annotationsmox.pcmtx.PCMTXConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;
import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionConfigurationBuilder;

public class PCMTXConfigurationBuilder extends AbstractWorkflowExtensionConfigurationBuilder {

    @Override
    public AbstractExtensionJobConfiguration buildConfiguration(Map<String, Object> attributes) {
        PCMTXConfiguration config = new PCMTXConfiguration(attributes);
        config.setEjbMoXConfiguration(new AnnotationsMoxConfiguration(attributes));
        return config;
    }

}
