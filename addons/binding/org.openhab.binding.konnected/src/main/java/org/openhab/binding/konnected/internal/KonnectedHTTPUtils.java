/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

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
public class KonnectedHTTPUtils {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHTTPUtils.class);
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
    private String logTest = "";

    /**
     * Sends a {@link doPut} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload    the json payload to include with the request
     */
    public String doPut(String urlAddress, String payload) throws IOException {
        logger.debug("The String url we want to put is : {}", urlAddress);
        logger.debug("The payload we want to put is: {}", payload);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        logTest = HttpUtil.executeUrl("PUT", urlAddress, getHttpHeaders(), input, "application/json",
                KonnectedHTTPUtils.REQUEST_TIMEOUT);
        logger.debug(logTest);
        return logTest;
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

    public synchronized String doGet(String urlAddress) throws IOException {
        logger.debug("The String url we want to get is : {}", urlAddress);
        logTest = HttpUtil.executeUrl("GET", urlAddress, KonnectedHTTPUtils.REQUEST_TIMEOUT);
        logger.debug(logTest);
        return logTest;
    }

    /**
     * Sends a {@link doGet} request with a timeout of 30 seconds
     *
     * @param urlAddress the address to send the request
     * @param payload    the json payload you want to send as part of the request
     */

    public synchronized String doGet(String urlAddress, String payload) throws IOException {
        logger.debug("The String url we want to get is : {}", urlAddress);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        logTest = HttpUtil.executeUrl("GET", urlAddress, getHttpHeaders(), input, "application/json",
                KonnectedHTTPUtils.REQUEST_TIMEOUT);
        logger.debug(logTest);
        return logTest;
    }
}
