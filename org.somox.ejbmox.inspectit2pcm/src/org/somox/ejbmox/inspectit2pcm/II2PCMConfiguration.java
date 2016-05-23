package org.somox.ejbmox.inspectit2pcm;

import java.util.Map;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;

/**
 * 
 * @author Philipp Merkle
 *
 */
public class II2PCMConfiguration extends AbstractExtensionJobConfiguration {
    
    public static final String CMR_REST_API_DEFAULT = "http://localhost:8182/rest/";

	public static final Integer WARMUP_MEASUREMENTS_DEFAULT = 10;
	
	private Map<String, Object> attributes;
	
	private String cmrUrl = CMR_REST_API_DEFAULT;
	
	/** the number of initial invocations (i.e. requests) to be discarded */
	private Integer warmupLength = WARMUP_MEASUREMENTS_DEFAULT;
	
	public II2PCMConfiguration(Map<String, Object> attributes) {
	    this.attributes = attributes;
    }
	
	public String getCmrUrl() { 
		return cmrUrl;
	}
	
	public void setCmrUrl(String cmrUrl) {
		this.cmrUrl = cmrUrl;
	}
	
	public Integer getWarmupLength() {
		return warmupLength;
	}
	
	public void setWarmupLength(Integer warmupLength) {
		this.warmupLength = warmupLength;
	}

    public Map<String, Object> getAttributes() {
        return attributes;
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
