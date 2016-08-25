package org.somox.ejbmox.inspectit2pcm.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * A simple client for requesting HTTP resources that follow the REST naming scheme. HTTP responses
 * are assumed to be JSON documents.
 * 
 * @author Philipp Merkle
 *
 */
public class RESTClient {

    private static final Logger LOG = Logger.getLogger(RESTClient.class);

    private String urlPrefix;

    public RESTClient(String prefix) {
        if (!prefix.endsWith("/")) {
            this.urlPrefix = prefix + "/";
        } else {
            this.urlPrefix = prefix;
        }
    }

    public String request(String relativeURL) {
        LOG.debug("Requesting " + urlPrefix + relativeURL);
        StringBuilder responseBuilder = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlPrefix + relativeURL);
            conn = (HttpURLConnection) url.openConnection();
            // conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // inner try allows auto close for BufferedReader
            try (BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())))) {
                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Request failed: " + conn.getResponseCode());
                }
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return responseBuilder.toString();
    }

}
