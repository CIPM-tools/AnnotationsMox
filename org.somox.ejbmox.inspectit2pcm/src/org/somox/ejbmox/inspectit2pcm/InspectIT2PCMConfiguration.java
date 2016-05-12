package org.somox.ejbmox.inspectit2pcm;

/**
 * 
 * @author Philipp Merkle
 *
 */
public class InspectIT2PCMConfiguration {

	private String restUrl = "http://localhost:8182/rest/";

	/** the number of initial invocations (i.e. requests) to be discarded */
	private Integer warmupLength = 10;

	public String getRestUrl() {
		return restUrl;
	}
	
	public Integer getWarmupLength() {
		return warmupLength;
	}

}
