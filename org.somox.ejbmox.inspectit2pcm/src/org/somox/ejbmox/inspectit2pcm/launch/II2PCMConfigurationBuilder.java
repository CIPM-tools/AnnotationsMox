package org.somox.ejbmox.inspectit2pcm.launch;

import java.util.Map;

import org.somox.ejbmox.inspectit2pcm.II2PCMConfiguration;

import de.uka.ipd.sdq.workflow.extension.AbstractWorkflowExtensionConfigurationBuilder;

public class II2PCMConfigurationBuilder extends AbstractWorkflowExtensionConfigurationBuilder {

    @Override
    public II2PCMConfiguration buildConfiguration(final Map<String, Object> attributes) {
        final II2PCMConfiguration config = new II2PCMConfiguration(attributes);
        config.setCmrUrl((String) attributes.get(InspectIT2PCMConfigurationAttributes.CMR_REST_API_URL));
        final String warmUpAsString = attributes.get(InspectIT2PCMConfigurationAttributes.WARMUP_MEASUREMENTS)
                .toString();
        config.setWarmupLength(Integer.valueOf(warmUpAsString));
        config.setEnsureInternalActionsBeforeSTOPAction((boolean) attributes
                .get(InspectIT2PCMConfigurationAttributes.ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION));
        return config;
    }

}
