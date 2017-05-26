/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.solarlog.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Collection of methods to help retrieve HTTP data from a SolarLog Server
 * Based on SqueezeBox HttpUtils, adapted for SolarLog.
 *
 * @author Dan Cunningham
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 * @author Johann Richard - Adapted for SolarLog Binding
 */
public class HttpUtils {

    private static int TIMEOUT = 5000;
    private static HttpClient client = new HttpClient();
    /**
     * JSON request to get the Data from a SolarLog Device
     */
    private static final String JSON_REQ = "{\"801\":{\"170\":null}}";
    private static final String URL_POSTFIX = "/getjp";

    /**
     * Simple logic to perform a post request
     *
     * @param url
     * @param timeout
     * @return
     */
    public static String post(String url, String postData) throws Exception {
        if (!client.isStarted()) {
            client.start();
        }

        // @formatter:off
        ContentResponse response = client.newRequest(url)
                .method(HttpMethod.POST)
                .content(new StringContentProvider(postData))
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .send();

        int statusCode = response.getStatus();

        if (statusCode != HttpStatus.OK_200) {
            String statusLine = response.getStatus() + " " + response.getReason();
            throw new HttpResponseException("Method failed: " + statusLine, response);
        }

        return response.getContentAsString();
    }

    /**
     * Retrieves the command line port (cli) from a SqueezeServer
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static JsonElement getSolarLogData(String url) throws Exception {
        String json = HttpUtils.post(url + URL_POSTFIX, JSON_REQ);
        JsonElement resp = new JsonParser().parse(json);
        return resp;
    }
}
