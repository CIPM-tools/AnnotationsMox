package org.annotationsmox.pcmtx;

import java.util.Map;

import org.annotationsmox.analyzer.AnnotationsMoxConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;

public class PCMTXConfiguration extends AbstractExtensionJobConfiguration {

    private AnnotationsMoxConfiguration ejbMoXConfiguration;

    private Map<String, Object> attributes;

    public PCMTXConfiguration(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public AnnotationsMoxConfiguration getEjbMoXConfiguration() {
        return ejbMoXConfiguration;
    }

    public void setEjbMoXConfiguration(AnnotationsMoxConfiguration ejbMoXConfiguration) {
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
