package org.annotationsmox.inspectit2pcm.rest;

import java.lang.reflect.Type;
import java.util.List;

import org.annotationsmox.inspectit2pcm.model.InvocationSequence;

import com.google.gson.reflect.TypeToken;

/**
 * REST service client to retrieve invocation sequences from a running InspectIT CMR.
 * 
 * @author Philipp Merkle
 *
 */
public class InvocationsServiceClient extends RESTServiceClient {

    // TODO this assumes, there is only one agent with platformId "1"
    private static final String SERVICE_URL_PREFIX = "agents/{agentid}/invocations";

    private int agentId;
    
    public InvocationsServiceClient(RESTClient client, int agentId) {
        super(client);
        this.agentId = agentId;
    }

    public List<Long> getInvocationSequencesId() {
        return getInvocationSequencesId(-1, -1);
    }

    public List<Long> getInvocationsId(long methodId) {
        return getInvocationSequencesId(methodId, -1);
    }

    public List<Long> getInvocationSequencesId(long methodId, long sensorTypeId) {
        // build query string
        StringBuilder parametersBuilder = new StringBuilder().append("?");
        if (methodId != -1) {
            parametersBuilder.append("methodId=").append(methodId).append("&");
        }
        if (sensorTypeId != -1) {
            parametersBuilder.append("sensorTypeId=").append(sensorTypeId).append("&");
        }
        // delete last char ("?" or "&")
        parametersBuilder.deleteCharAt(parametersBuilder.length() - 1);

        String response = request(getServiceUrlPrefix() + "/" + parametersBuilder.toString());
        Type collectionType = new TypeToken<List<Long>>() {
        }.getType();
        List<Long> methodIds = buildGson().fromJson(response, collectionType);
        return methodIds;
    }

    public InvocationSequence getInvocationSequence(long invocationSequenceId, boolean detailed) {
        String response = request(getServiceUrlPrefix() + "/" + invocationSequenceId + "?detailed=" + detailed);
        InvocationSequence is = buildGson().fromJson(response, InvocationSequence.class);
        return is;
    }

    private String getServiceUrlPrefix() {
        return SERVICE_URL_PREFIX.replace("{agentid}", Integer.toString(agentId));
    }

}
