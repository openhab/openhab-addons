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
package org.openhab.binding.konnected.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Get and Put reqeust class.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedHTTPUtils {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHTTPUtils.class);
    private int requestTimeout;
    private String logTest = "";

    public KonnectedHTTPUtils(int requestTimeout) {
        this.requestTimeout = (int) TimeUnit.SECONDS.toMillis(requestTimeout);
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = (int) TimeUnit.SECONDS.toMillis(requestTimeout);
    }

    /**
     * Sends a {@link doPut} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload to include with the request
     */
    private String doPut(String urlAddress, String payload) throws IOException {
        logger.debug("The String url we want to put is : {}", urlAddress);
        logger.debug("The payload we want to put is: {}", payload);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        String retVal = HttpUtil.executeUrl("PUT", urlAddress, getHttpHeaders(), input, "application/json",
                requestTimeout);
        logger.trace("return value: {}", retVal);
        return retVal;
    }

    protected Properties getHttpHeaders() {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    /**
     * Sends a {@link doGet} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     */

    private synchronized String doGet(String urlAddress) throws IOException {
        logger.debug("The String url we want to get is : {}", urlAddress);
        String retVal = HttpUtil.executeUrl("GET", urlAddress, requestTimeout);
        logger.trace("return value: {}", retVal);
        return retVal;
    }

    /**
     * Sends a {@link doGet} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload you want to send as part of the request
     */

    private synchronized String doGet(String urlAddress, String payload) throws IOException {
        logger.debug("The String url we want to get is : {}", urlAddress);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        String retVal = HttpUtil.executeUrl("GET", urlAddress, getHttpHeaders(), input, "application/json",
                requestTimeout);
        logger.trace("return value: {}", retVal);
        return retVal;
    }

    /**
     * Sends a {@link doGet} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload you want to send as part of the request, may be null.
     * @param retryCount the number of retries before throwing the IOexpcetion back to the handler
     */
    public synchronized String doGet(String urlAddress, String payload, int retryCount)
            throws KonnectedHttpRetryExceeded {
        String response = null;
        int x = 0;
        Boolean loop = true;
        while (loop) {
            try {
                if (payload == null) {
                    response = doGet(urlAddress, payload);
                } else {
                    response = doGet(urlAddress);
                }
                loop = false;
            } catch (IOException e) {
                x++;
                if (x > retryCount) {
                    throw new KonnectedHttpRetryExceeded("The number of retry attempts was exceeded", e.getCause());
                }
            }
        }
        return response;
    }

    /**
     * Sends a {@link doPut} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload the json payload you want to send as part of the request
     * @param retryCount the number of retries before throwing the IOexpcetion back to the handler
     */
    public synchronized String doPut(String urlAddress, String payload, int retryCount)
            throws KonnectedHttpRetryExceeded {
        String response = null;
        int x = 0;
        Boolean loop = true;
        while (loop) {
            try {
                response = doPut(urlAddress, payload);
                loop = false;
            } catch (IOException e) {
                x++;
                if (x > retryCount) {
                    throw new KonnectedHttpRetryExceeded("The number of retry attempts was exceeded", e.getCause());
                }
            }
        }
        return response;
    }
}
