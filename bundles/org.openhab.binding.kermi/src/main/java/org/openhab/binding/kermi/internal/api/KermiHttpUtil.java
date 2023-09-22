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
package org.openhab.binding.kermi.internal.api;

import static org.openhab.binding.kermi.internal.KermiBindingConstants.parseUrl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.client.HttpResponseException;
import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.micrometer.core.instrument.util.StringUtils;

public class KermiHttpUtil {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private final Logger logger = LoggerFactory.getLogger(KermiHttpUtil.class);

    private String hostname = "";
    private String password = "";
    private HttpUtil httpUtil;
    private Properties httpHeaders;
    private Gson gson;

    private Map<String, DeviceInfo> deviceInfo;

    public KermiHttpUtil() {
        httpHeaders = new Properties();
        gson = new Gson();
        httpUtil = new HttpUtil();
    }

    public void executeCheckBridgeOnline() throws KermiCommunicationException {
        executeUrl("GET", "http://" + hostname, null, null);
    }

    public GetDevicesResponse getDevicesByFilter() throws KermiCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("WithDetails", Boolean.FALSE);
        jsonObject.addProperty("WithChildDevices", Boolean.FALSE);
        jsonObject.addProperty("Recursive", Boolean.FALSE);
        jsonObject.add("DeviceTypes", new JsonArray());
        jsonObject.add("MenuEntries", new JsonArray());
        String executeUrl = executeUrl("POST", parseUrl(KermiBindingConstants.HPM_GETDEVICESBYFILTER_URL, hostname),
                jsonObject.toString(), CONTENT_TYPE);
        return gson.fromJson(executeUrl, GetDevicesResponse.class);
    }

    public GetDeviceResponse getDeviceInfoByDeviceId(String deviceId) throws KermiCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DeviceId", deviceId);
        jsonObject.addProperty("WithDetails", Boolean.TRUE);
        jsonObject.addProperty("Recursive", Boolean.TRUE);
        String executeUrl = executeUrl("POST", parseUrl(KermiBindingConstants.HPM_GETDEVICE_URL, hostname),
                jsonObject.toString(), CONTENT_TYPE);
        return gson.fromJson(executeUrl, GetDeviceResponse.class);
    }

    /**
     * Issue a HTTP GET request and retry on failure
     *
     * @param url the url to execute
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws KermiCommunicationException when the request execution failed or interrupted
     */
    public synchronized String executeUrl(String httpMethod, String url, String content, String contentType)
            throws KermiCommunicationException {

        if (StringUtils.isBlank(hostname)) {
            return "Not connected";
        }

        int attemptCount = 1;
        try {
            while (true) {
                Throwable lastException = null;
                String result = null;
                try {
                    InputStream _content = (content != null)
                            ? new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))
                            : null;
                    result = HttpUtil.executeUrl(httpMethod, url, httpHeaders, _content, contentType, 5000);
                    logger.debug("[{} {}] {}", httpMethod, url, result);
                } catch (IOException e) {
                    // HttpUtil::executeUrl wraps InterruptedException into IOException.
                    // Unwrap and rethrow it so that we don't retry on InterruptedException
                    if (e.getCause() instanceof InterruptedException) {
                        throw (InterruptedException) e.getCause();
                    }

                    if (e.getCause() instanceof ExecutionException) {
                        ExecutionException iex = (ExecutionException) e.getCause();
                        if (iex != null && iex.getCause() instanceof HttpResponseException) {
                            HttpResponseException hre = (HttpResponseException) iex.getCause();
                            if (401 == hre.getResponse().getStatus()) {
                                logger.debug("Perform login");
                                attemptCount = 0;
                                performLogin();
                            }
                        }
                    }
                    lastException = e;
                }

                if (result != null) {
                    if (attemptCount > 1) {
                        logger.debug("Attempt #{} successful {}", attemptCount, url);
                    }
                    return result;
                }

                if (attemptCount >= 3) {
                    logger.debug("Failed connecting to {} after {} attempts.", url, attemptCount, lastException);
                    // throw new FroniusCommunicationException("Unable to connect", lastException);
                }

                logger.debug("HTTP error on attempt #{} {}", attemptCount, url);
                Thread.sleep(500 * attemptCount);
                attemptCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KermiCommunicationException("Interrupted", e);
        }
    }

    private void performLogin() throws KermiCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Password", password);
        executeUrl("POST", getBaseApiUrl() + "Security/Login", jsonObject.toString(), CONTENT_TYPE);
    }

    private String getBaseApiUrl() {
        return "http://" + hostname + "/api/";
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDeviceInfo(Map<String, DeviceInfo> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

}
