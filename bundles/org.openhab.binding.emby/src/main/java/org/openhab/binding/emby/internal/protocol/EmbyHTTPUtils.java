/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Get and Put reqeust class.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class EmbyHTTPUtils {
    private final Logger logger = LoggerFactory.getLogger(EmbyHTTPUtils.class);
    private int requestTimeout;
    private String apiKey;
    private String hostIpPort;
    private String logTest = "";

    public EmbyHTTPUtils(int requestTimeout, String apiKey, String hostIpPort) {
        this.requestTimeout = (int) TimeUnit.SECONDS.toMillis(requestTimeout);
        this.apiKey = apiKey;
        this.hostIpPort = hostIpPort;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = (int) TimeUnit.SECONDS.toMillis(requestTimeout);
    }

    /**
     * Sends a {@link doPost} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload to include with the request
     */
    private String doPost(String urlAddress, String payload) throws IOException {
        urlAddress = "http://" + hostIpPort + urlAddress;
        logger.debug("The String url we want to post is : {}", urlAddress);
        logger.debug("The payload we want to post is: {}", payload);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        logTest = HttpUtil.executeUrl("POST", urlAddress, getHttpHeaders(), input, "application/json", requestTimeout);
        logger.debug("{}", logTest);
        return logTest;
    }

    protected Properties getHttpHeaders() {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Content-Type", "application/json");
        httpHeaders.put("X-Emby-Token", this.apiKey);
        return httpHeaders;
    }

    /**
     * Sends a {@link doPost} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload you want to send as part of the request
     * @param retry the number of retries before throwing the IOexpcetion back to the handler
     */
    public synchronized String doPost(String urlAddress, String payload, int retryCount) throws EmbyHttpRetryExceeded {
        String response = null;
        int x = 0;
        Boolean loop = true;
        while (loop) {
            try {
                response = doPost(urlAddress, payload);
                loop = false;
            } catch (IOException e) {
                x++;
                if (x > retryCount) {
                    throw new EmbyHttpRetryExceeded("The number of retry attempts was exceeded", e.getCause());
                }
            }
        }
        return response;
    }
}
