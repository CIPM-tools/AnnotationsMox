package org.annotationsmox.inspectit2pcm.launch;

import java.util.List;
import java.util.Map;

import de.uka.ipd.sdq.workflow.extension.AbstractExtensionJobConfiguration;

/**
 *
 * @author Philipp Merkle
 *
 */
public class II2PCMConfiguration extends AbstractExtensionJobConfiguration {

    public static final String CMR_REST_API_DEFAULT = "http://localhost:8182/rest/";
    
    public static final Integer AGENT_ID_DEFAULT = 1;

    public static final Integer WARMUP_MEASUREMENTS_DEFAULT = 10;

    public static final Boolean ENSURE_INTERNAL_ACTIONS_BEFORE_STOP_ACTION_DEFAULT = true;
    
    public static final Boolean REFINE_INTERNAL_ACTIONS_TO_SQL_STATEMENTS_DEFAULT = true;
    
    public static final Boolean REMOVE_ANOMALIES_DEFAULT = true;

    public static final Boolean REMOVE_OUTLIERS_DEFAULT = false;

    private final Map<String, Object> attributes;

    private String cmrUrl = CMR_REST_API_DEFAULT;

    /** the number of initial invocations (i.e. requests) to be discarded */
    private Integer warmupLength = WARMUP_MEASUREMENTS_DEFAULT;

    private boolean ensureInternalActionsBeforeSTOPAction;
    
    private boolean refineSQLStatements;
    
    private Integer agentId = AGENT_ID_DEFAULT;
    
    private Boolean removeAnomalies = REMOVE_ANOMALIES_DEFAULT;
    
    private Boolean removeOutliers = REMOVE_OUTLIERS_DEFAULT;

    public II2PCMConfiguration(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getCmrUrl() {
        return this.cmrUrl;
    }

    public void setCmrUrl(final String cmrUrl) {
        this.cmrUrl = cmrUrl;
    }

    public Integer getWarmupLength() {
        return this.warmupLength;
    }

    public void setWarmupLength(final Integer warmupLength) {
        this.warmupLength = warmupLength;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setEnsureInternalActionsBeforeSTOPAction(final boolean ensureInternalActionsBeforeSTOPAction) {
        this.ensureInternalActionsBeforeSTOPAction = ensureInternalActionsBeforeSTOPAction;
    }

    public boolean isEnsureInternalActionsBeforeSTOPAction() {
        return this.ensureInternalActionsBeforeSTOPAction;
    }
    
    public boolean isRefineSQLStatements() {
        return refineSQLStatements;
    }

    public void setRefineSQLStatements(boolean refineSQLStatements) {
        this.refineSQLStatements = refineSQLStatements;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public Boolean isRemoveAnomalies() {
        return removeAnomalies;
    }

    public void setRemoveAnomalies(Boolean removeAnomalies) {
        this.removeAnomalies = removeAnomalies;
    }    

    public Boolean getRemoveOutliers() {
        return removeOutliers;
    }

    public void setRemoveOutliers(Boolean removeOutliers) {
        this.removeOutliers = removeOutliers;
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
