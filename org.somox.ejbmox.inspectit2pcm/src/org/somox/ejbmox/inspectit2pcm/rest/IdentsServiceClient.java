package org.somox.ejbmox.inspectit2pcm.rest;

import java.lang.reflect.Type;
import java.util.Set;

import org.somox.ejbmox.inspectit2pcm.model.MethodIdent;

import com.google.gson.reflect.TypeToken;

/**
 * REST service client to retrieve metadata on methods ("method idents") from a running InspectIT
 * CMR.
 * <p>
 * Method idents, for instance, map method ids to their corresponding method name and vice versa.
 * 
 * @author Philipp Merkle
 *
 */
public class IdentsServiceClient extends RESTServiceClient {

    // TODO this assumes, there is only one agent with platformId "1"
    private static final String SERVICE_URL_PREFIX = "agents/1/idents";

    public IdentsServiceClient(RESTClient client) {
        super(client);
    }

    public Set<MethodIdent> listMethodIdents() {
        String response = request(SERVICE_URL_PREFIX + "/methods");
        Type collectionType = new TypeToken<Set<MethodIdent>>() {
        }.getType();
        Set<MethodIdent> methods = buildGson().fromJson(response, collectionType);
        return methods;
    }

}
