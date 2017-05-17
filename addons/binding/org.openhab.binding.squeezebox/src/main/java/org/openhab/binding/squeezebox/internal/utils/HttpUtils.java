/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.utils;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
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
 * @author Dan Cunningham
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 */
public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static int TIMEOUT = 5000;
    private static HttpClient client = new HttpClient();
    /**
     * JSON request to get the CLI port from a Squeeze Server
     */
    private static final String JSON_REQ = "{\"params\": [\"\", [\"pref\" ,\"plugin.cli:cliport\",\"?\"]], \"id\": 1, \"method\": \"slim.request\"}";

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
            logger.error("Method failed: {}", statusLine);
            throw new Exception("Method failed: " + statusLine);
        }

        return response.getContentAsString();
    }

    /**
     * Returns a byte array from a URL string
     *
     * @param urlString
     * @return byte array of data
     */
    public static byte[] getData(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return IOUtils.toByteArray(connection.getInputStream());
    }

    /**
     * Retrieves the command line port (cli) from a SqueezeServer
     *
     * @param ip
     * @param webPort
     * @return
     * @throws Exception
     */
    public static int getCliPort(String ip, int webPort) throws Exception {
        String url = "http://" + ip + ":" + webPort + "/jsonrpc.js";
        String json = HttpUtils.post(url, JSON_REQ);
        logger.trace("Recieved json from server {}", json);
        JsonElement resp = new JsonParser().parse(json);
        String cliPort = resp.getAsJsonObject().get("result").getAsJsonObject().get("_p2").getAsString();
        return Integer.parseInt(cliPort);
    }

}
