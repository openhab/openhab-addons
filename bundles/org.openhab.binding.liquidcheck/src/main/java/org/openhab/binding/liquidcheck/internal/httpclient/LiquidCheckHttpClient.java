/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.liquidcheck.internal.httpclient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.liquidcheck.internal.LiquidCheckConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LiquidCheckHttpClient} sets up the jetty client for the connection to the device.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LiquidCheckHttpClient {
    private final Logger logger = LoggerFactory.getLogger(LiquidCheckHttpClient.class);
    private final HttpClient client;
    private final LiquidCheckConfiguration config;

    public boolean isClosed = false;

    /**
     * The Constructor of the LiquidCheckHttpClient class will set up a jetty client
     * 
     * @param config
     */
    public LiquidCheckHttpClient(LiquidCheckConfiguration config, HttpClient client) {
        this.config = config;
        this.client = client;
    }

    /**
     * The pollData method will poll the data from device
     * 
     * @return String with the response of the request
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public String pollData() throws InterruptedException, TimeoutException, ExecutionException {
        String uri = "http://" + config.hostname + "/infos.json";
        Request request = client.newRequest(uri).method(HttpMethod.GET)
                .timeout(config.connectionTimeout, TimeUnit.SECONDS).followRedirects(false);
        logger.debug("Polling for data");
        ContentResponse response = request.send();
        return response.getContentAsString();
    }

    /**
     * The measureCommand method will start a measurement
     * 
     * @return String with response of the request
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public String measureCommand() throws InterruptedException, TimeoutException, ExecutionException {
        String uri = "http://" + config.hostname + "/command";
        Request request = client.newRequest(uri).timeout(config.connectionTimeout, TimeUnit.SECONDS);
        request.method(HttpMethod.POST);
        request.header(HttpHeader.CONTENT_TYPE, "applicaton/json");
        request.content(new StringContentProvider(
                "{\"header\":{\"namespace\":\"Device.Control\",\"name\":\"StartMeasure\",\"messageId\":\"1\",\"payloadVersion\":\"1\"},\"payload\":null}"));
        ContentResponse response = request.send();
        return response.getContentAsString();
    }

    /**
     * The isConnected method will return the state of the http client
     * 
     * @return
     */
    public boolean isConnected() {
        String state = this.client.getState();
        return "STARTED".equals(state);
    }
}
