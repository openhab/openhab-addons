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
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNull;
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
    private Properties httpHeaders;
    private Gson gson;

    public KermiHttpUtil() {
        httpHeaders = new Properties();
        gson = new Gson();
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

    /**
     * Issue a HTTP GET request and retry on failure
     *
     * @param url the url to execute
     * @param timeout the socket timeout in milliseconds to wait for data
     * @return the response body
     * @throws KermiCommunicationException when the request execution failed or interrupted
     */
    @SuppressWarnings("null")
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

    public GetDevicesResponse getAllDevices() throws KermiCommunicationException {
        String response = executeUrl("GET", parseUrl(KermiBindingConstants.HPM_DEVICE_GETALLDEVICES_URL, hostname),
                null, null);
        return gson.fromJson(response, GetDevicesResponse.class);
    }

    public MenuGetChildEntriesResponse getMenuChildEntries(String deviceId, @NonNull String parentMenuEntryId)
            throws KermiCommunicationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DeviceId", deviceId);
        jsonObject.addProperty("ParentMenuEntryId", parentMenuEntryId);
        jsonObject.addProperty("WithDetails", Boolean.TRUE);

        String response = executeUrl("POST", parseUrl(KermiBindingConstants.HPM_MENU_GETCHILDENTRIES_URL, hostname),
                jsonObject.toString(), CONTENT_TYPE);
        return gson.fromJson(response, MenuGetChildEntriesResponse.class);
    }

    /**
     * Fetch update datapoint values for the given idTuples
     *
     * @param idTuples with [0] being the DeviceId, and [1] being the DatapointConfigId
     * @return
     * @throws KermiCommunicationException
     */
    public DatapointReadValuesResponse getDatapointReadValues(Set<String[]> idTuples)
            throws KermiCommunicationException {

        JsonObject jsonObject = new JsonObject();
        JsonArray datapointValues = new JsonArray(idTuples.size());
        jsonObject.add("DatapointValues", datapointValues);
        idTuples.forEach(idt -> {
            JsonObject _entryObject = new JsonObject();
            _entryObject.addProperty("DeviceId", idt[0]);
            _entryObject.addProperty("DatapointConfigId", idt[1]);
            datapointValues.add(_entryObject);
        });

        String response = executeUrl("POST", parseUrl(KermiBindingConstants.HPM_DATAPOINT_READVALUES_URL, hostname),
                jsonObject.toString(), CONTENT_TYPE);
        return gson.fromJson(response, DatapointReadValuesResponse.class);
    }

}
