/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Get and Put request class.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
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
     * Sends an HTTP POST request to the Emby server.
     *
     * @param urlAddress the endpoint path (appended to the configured hostIpPort) to send the request to
     * @param payload the JSON payload to include in the request body
     * @return the response body as a string
     * @throws IOException if an I/O error occurs during the request
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
     * Sends an HTTP POST request to the Emby server with retry logic.
     * 
     * Implements a simple linear backoff strategy between retries.
     *
     * @param urlAddress the endpoint path (appended to the configured hostIpPort) to send the request to
     * @param payload the JSON payload to include in the request body
     * @param retryCount the maximum number of retry attempts before failing
     * @return the response body as a string, or null if no response was received
     * @throws EmbyHttpRetryExceeded if the number of retries is exceeded or the thread is interrupted
     */
    public synchronized @Nullable String doPost(String urlAddress, String payload, int retryCount)
            throws EmbyHttpRetryExceeded {
        String response = null;
        int x = 0;

        while (true) {
            try {
                response = doPost(urlAddress, payload);
                break;
            } catch (IOException e) {
                x++;
                try {
                    Thread.sleep(1000 * x); // Simple linear backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // preserve interrupt status
                    logger.warn("Sleep interrupted during retry delay", ie);
                    throw new EmbyHttpRetryExceeded("Interrupted while retrying POST request", ie);
                }

                if (x > retryCount) {
                    logger.warn("Attempt {} failed to POST to {}: {}", x, urlAddress, e.getMessage());
                    throw new EmbyHttpRetryExceeded("The number of retry attempts was exceeded", e.getCause());
                }
            }
        }
        return response;
    }
}
