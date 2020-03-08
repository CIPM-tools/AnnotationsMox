package org.annotationsmox.inspectit2pcm.rest;

import java.lang.reflect.Type;
import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Base implementation for REST service clients.
 * 
 * @author Philipp Merkle
 *
 */
public abstract class RESTServiceClient {

    private RESTClient client;

    public RESTServiceClient(RESTClient client) {
        this.client = client;
    }

    protected String request(String relativeURL) {
        return client.request(relativeURL);
    }

    protected Gson buildGson() {
        /*
         * see http://stackoverflow.com/questions/5671373/unparseable-date-
         * 1302828677828-trying-to-deserialize-with-gson-a-millisecond
         */
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Timestamp.class, new JsonDeserializer<Timestamp>() {
            public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return new Timestamp(json.getAsJsonPrimitive().getAsLong());
            }
        });
        return builder.create();
    }

}
