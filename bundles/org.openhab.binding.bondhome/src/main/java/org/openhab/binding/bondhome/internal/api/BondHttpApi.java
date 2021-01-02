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
package org.openhab.binding.bondhome.internal.api;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * {@link BondHttpApi} wraps the Bond REST API and provides various low
 * level function to access the device api (not cloud api).
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondHttpApi {
    private final Logger logger = LoggerFactory.getLogger(BondHttpApi.class);
    private final BondBridgeHandler bridgeHandler;
    private Gson gson = new Gson();

    public BondHttpApi(BondBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Gets version information about the Bond bridge
     *
     * @return the {@link BondSysVersion}
     * @throws IOException
     */
    @Nullable
    public BondSysVersion getBridgeVersion() throws IOException {
        String json = request("/v2/sys/version");
        logger.trace("BondHome device info : {}", json);
        return gson.fromJson(json, BondSysVersion.class);
    }

    /**
     * Gets a list of the attached devices
     *
     * @return an array of device id's
     * @throws IOException
     */
    @Nullable
    public List<String> getDevices() throws IOException {
        List<String> list = new ArrayList<>();
        String json = request("/v2/devices/");
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            if (!entry.getKey().equals("_")) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    /**
     * Gets basic device information
     *
     * @param deviceId The ID of the device
     * @return the {@link org.openhab.binding.bondhome.internal.api.BondDevice}
     * @throws IOException
     */
    @Nullable
    public BondDevice getDevice(String deviceId) throws IOException {
        String json = request("/v2/devices/" + deviceId);
        logger.trace("BondHome device info : {}", json);
        return gson.fromJson(json, BondDevice.class);
    }

    /**
     * Gets the current state of a device
     *
     * @param deviceId The ID of the device
     * @return the {@link org.openhab.binding.bondhome.internal.api.BondDeviceState}
     * @throws IOException
     */
    @Nullable
    public BondDeviceState getDeviceState(String deviceId) throws IOException {
        String json = request("/v2/devices/" + deviceId + "/state");
        logger.trace("BondHome device state : {}", json);
        return gson.fromJson(json, BondDeviceState.class);
    }

    /**
     * Gets the current properties of a device
     *
     * @param deviceId The ID of the device
     * @return the {@link org.openhab.binding.bondhome.internal.api.BondDeviceProperties}
     * @throws IOException
     */
    @Nullable
    public BondDeviceProperties getDeviceProperties(String deviceId) throws IOException {
        String json = request("/v2/devices/" + deviceId + "/properties");
        logger.trace("BondHome device properties : {}", json);
        return gson.fromJson(json, BondDeviceProperties.class);
    }

    /**
     * Executes a device action
     *
     * @param deviceId The ID of the device
     * @param actionId The Bond action
     * @return the {@link org.openhab.binding.bondhome.internal.api.BondDeviceProperties}
     */
    public synchronized void executeDeviceAction(String deviceId, BondDeviceAction action, @Nullable Integer argument) {
        String url = "http://" + bridgeHandler.getBridgeIpAddress() + "/v2/devices/" + deviceId + "/actions/"
                + action.getActionId();
        String payload = "{}";
        if (argument != null) {
            payload = "{\"argument\":" + argument + "}";
        }
        InputStream content = new ByteArrayInputStream(payload.getBytes());
        try {
            logger.debug("HTTP PUT to {} with content {}", url, payload);

            Properties headers = new Properties();
            headers.put("BOND-Token", bridgeHandler.getBridgeToken());

            String httpResponse = HttpUtil.executeUrl(HttpMethod.PUT, url, headers, content, "application/json",
                    BOND_API_TIMEOUT_MS);
            logger.debug("HTTP response from {}: {}", deviceId, httpResponse);
        } catch (IOException ignored) {
            logger.warn("Unable to execute device action!");
        }
    }

    /**
     * Submit GET request and return response, check for invalid responses
     *
     * @param uri: URI (e.g. "/settings")
     */
    private synchronized String request(String uri) throws IOException {
        String httpResponse = "ERROR";
        String url = "http://" + bridgeHandler.getBridgeIpAddress() + uri;
        int numRetriesRemaining = 3;
        do {
            try {
                logger.debug("HTTP GET to {}", url);

                final Properties headers = new Properties();
                headers.put("BOND-Token", bridgeHandler.getBridgeToken());

                httpResponse = HttpUtil.executeUrl(HttpMethod.GET, url, headers, null, "", BOND_API_TIMEOUT_MS);
                if (httpResponse == null) {
                    throw new IOException("No response received!");
                }
                // handle known errors
                if (httpResponse.contains(API_ERR_HTTP_401_UNAUTHORIZED)) {
                    // Don't retry or throw an exception if we get unauthorized, just set the bridge offline
                    numRetriesRemaining = 0;
                    bridgeHandler.setBridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                            "Incorrect local token for Bond Bridge.");
                }
                if (httpResponse.contains(API_ERR_HTTP_404_NOTFOUND)) {
                    // Don't retry if the device wasn't found by the bridge.
                    numRetriesRemaining = 0;
                    throw new IOException(
                            API_ERR_HTTP_404_NOTFOUND + ", set/correct device ID in the thing/binding config");
                }
                // all api responses return Json. If we get something else it must
                // be an error message, e.g. http result code
                if (!httpResponse.startsWith("{") && !httpResponse.startsWith("[")) {
                    throw new IOException("Unexpected http response: " + httpResponse);
                }

                logger.debug("HTTP response from request to {}: {}", uri, httpResponse);
                return httpResponse;
            } catch (IOException e) {
                Throwable ioeCause = e.getCause();
                @Nullable
                String errorMessage = e.getMessage();
                if (ioeCause != null) {
                    @Nullable
                    String innerErrorMessage = ioeCause.getMessage();
                    if (innerErrorMessage != null) {
                        logger.info("Last request to Bond Bridge failed; {} retries remaining. Failure cause: {}",
                                numRetriesRemaining, innerErrorMessage);
                    } else {
                        logger.info("Last request to Bond Bridge failed; {} retries remaining. Failure cause unknown",
                                numRetriesRemaining);
                    }
                } else if (errorMessage != null) {
                    logger.info("Last request to Bond Bridge failed; {} retries remaining. Failure cause: {}",
                            numRetriesRemaining, errorMessage);
                } else {
                    logger.info("Last request to Bond Bridge failed; {} retries remaining. Failure cause unknown",
                            numRetriesRemaining);
                }
                numRetriesRemaining--;
                if (numRetriesRemaining == 0) {
                    // TODO(SRGDamia1): Do I want to process more of the exceptions differently?
                    if (e.getCause() instanceof TimeoutException) {
                        logger.warn("Repeated Bond API calls to {} timed out.", uri);
                        bridgeHandler.setBridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                "Repeated timeouts attempting to reach bridge.");
                    } else if (e.getCause() instanceof InterruptedException) {
                        throw new IOException("Bond API call to " + uri + " failed: " + e.getMessage());
                    } else if (e.getCause() instanceof ExecutionException) {
                        throw new IOException("Bond API call to " + uri + " failed: " + e.getMessage());
                    } else {
                        throw new IOException("Bond API call to " + uri + " failed: " + e.getMessage());
                    }
                }
            }
        } while (numRetriesRemaining > 0);
        return "";
    }
}
