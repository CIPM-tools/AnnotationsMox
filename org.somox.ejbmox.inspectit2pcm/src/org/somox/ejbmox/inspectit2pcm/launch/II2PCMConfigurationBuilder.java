package org.somox.ejbmox.inspectit2pcm.launch;

import java.util.Map;

import org.somox.ejbmox.inspectit2pcm.II2PCMConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionConfigurationBuilder;

public class II2PCMConfigurationBuilder extends AbstractWorkflowExtensionConfigurationBuilder {

	@Override
	public II2PCMConfiguration buildConfiguration(Map<String, Object> attributes) {
		II2PCMConfiguration config = new II2PCMConfiguration();
		config.setCmrUrl((String) attributes.get(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL));
		config.setWarmupLength(Integer.valueOf((String) attributes.get(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS)));
		return config;
	}

}
