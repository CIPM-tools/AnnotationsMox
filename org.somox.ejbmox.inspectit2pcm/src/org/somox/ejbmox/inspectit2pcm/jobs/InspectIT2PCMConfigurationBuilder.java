package org.somox.ejbmox.inspectit2pcm.jobs;

import java.util.Map;

import org.somox.ejbmox.inspectit2pcm.InspectIT2PCMConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionConfigurationBuilder;

public class InspectIT2PCMConfigurationBuilder extends AbstractWorkflowExtensionConfigurationBuilder {

	@Override
	public InspectIT2PCMConfiguration buildConfiguration(Map<String, Object> attributes) {
		InspectIT2PCMConfiguration config = new InspectIT2PCMConfiguration();
		config.setCmrUrl((String) attributes.get(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL));
		config.setWarmupLength(Integer.valueOf((String) attributes.get(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS)));
		return config;
	}

}
