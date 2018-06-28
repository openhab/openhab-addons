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
import org.openhab.binding.konnected.handler.KonnectedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Get and Put reqeust class.
 *
 * @author Zachary Christiansen
 */
public class KonnectedHTTPUtils {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHandler.class);
    private int REQUEST_TIMEOUT;

    public KonnectedHTTPUtils() {
        this.REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
    }

    public String doPut(String urlAddress, String payload) throws IOException {

        // HttpUtil http = new HttpUtil();
        logger.debug("The String url we want to put is : {}", urlAddress);
        logger.debug("The payload we want to put is: {}", payload);
        ByteArrayInputStream input = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        String test = HttpUtil.executeUrl("PUT", urlAddress, getHttpHeaders(), input, "application/json",
                this.REQUEST_TIMEOUT);
        logger.debug(test);
        return test;
    }

    protected Properties getHttpHeaders() {
        Properties httpHeaders = new Properties();
        httpHeaders.put("Content-Type", "application/json");
        return httpHeaders;
    }

    public synchronized String doGet(String urlAddress) throws IOException {
        logger.debug("The String url we want to get is : {}", urlAddress);
        String test = HttpUtil.executeUrl("GET", urlAddress, this.REQUEST_TIMEOUT);
        logger.debug(test);
        return test;
    }

}