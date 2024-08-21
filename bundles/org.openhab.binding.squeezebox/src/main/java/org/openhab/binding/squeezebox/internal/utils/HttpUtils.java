/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.squeezebox.internal.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Collection of methods to help retrieve HTTP data from a SqueezeServer
 *
 * @author Dan Cunningham - Initial contribution
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 * @author Mark Hilbush - Add support for LMS authentication
 * @author Mark Hilbush - Rework exception handling
 */
public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static final int TIMEOUT = 5000;
    private static HttpClient client = new HttpClient();
    /**
     * JSON request to get the CLI port from a Squeeze Server
     */
    private static final String JSON_REQ = "{\"params\": [\"\", [\"pref\" ,\"plugin.cli:cliport\",\"?\"]], \"id\": 1, \"method\": \"slim.request\"}";

    /**
     * Simple logic to perform a post request
     *
     * @param url URL to be sent to LMS server
     * @param postData Data to be sent to LMS server
     * @return Content received from LMS
     * @throws SqueezeBoxCommunicationException
     * @throws SqueezeBoxNotAuthorizedException
     */
    public static String post(String url, String postData)
            throws SqueezeBoxNotAuthorizedException, SqueezeBoxCommunicationException {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                throw new SqueezeBoxCommunicationException("Jetty http client exception: " + e.getMessage());
            }
        }

        ContentResponse response;
        try {
            response = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postData))
                    .timeout(TIMEOUT, TimeUnit.MILLISECONDS).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SqueezeBoxCommunicationException("Jetty http client exception: " + e.getMessage());
        }

        int statusCode = response.getStatus();

        if (statusCode == HttpStatus.UNAUTHORIZED_401) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.error("Received '{}' from squeeze server", statusLine);
            throw new SqueezeBoxNotAuthorizedException("Unauthorized: " + statusLine);
        }

        if (statusCode != HttpStatus.OK_200) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.error("HTTP POST method failed: {}", statusLine);
            throw new SqueezeBoxCommunicationException("Http post to server failed: " + statusLine);
        }

        return response.getContentAsString();
    }

    /**
     * Retrieves the command line port (cli) from a SqueezeServer
     *
     * @param ip
     * @param webPort
     * @return Command Line Interpreter (CLI) port number
     * @throws SqueezeBoxNotAuthorizedException
     * @throws SqueezeBoxCommunicationException
     * @throws NumberFormatException
     */
    public static int getCliPort(String ip, int webPort)
            throws SqueezeBoxNotAuthorizedException, SqueezeBoxCommunicationException {
        String url = "http://" + ip + ":" + webPort + "/jsonrpc.js";
        String json = HttpUtils.post(url, JSON_REQ);
        logger.trace("Recieved json from server {}", json);
        JsonElement resp = JsonParser.parseString(json);
        String cliPort = resp.getAsJsonObject().get("result").getAsJsonObject().get("_p2").getAsString();
        return Integer.parseInt(cliPort);
    }
}
