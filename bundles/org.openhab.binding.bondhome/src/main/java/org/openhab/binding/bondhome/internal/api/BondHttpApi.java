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
package org.openhab.binding.bondhome.internal.api;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bondhome.internal.BondException;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
    private final HttpClientFactory httpClientFactory;
    private Gson gson = new Gson();

    public BondHttpApi(BondBridgeHandler bridgeHandler, final HttpClientFactory httpClientFactory) {
        this.bridgeHandler = bridgeHandler;
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Gets version information about the Bond bridge
     *
     * @return the {@link BondSysVersion}
     * @throws BondException
     */
    public BondSysVersion getBridgeVersion() throws BondException {
        String json = request("/v2/sys/version");
        logger.trace("BondHome device info : {}", json);
        try {
            return Objects.requireNonNull(gson.fromJson(json, BondSysVersion.class));
        } catch (JsonParseException e) {
            logger.debug("Could not parse sys/version JSON '{}'", json, e);
            throw new BondException("@text/offline.comm-error.unparseable-response");
        }
    }

    /**
     * Gets a list of the attached devices
     *
     * @return an array of device id's
     * @throws BondException
     */
    public List<String> getDevices() throws BondException {
        List<String> list = new ArrayList<>();
        String json = request("/v2/devices/");
        try {
            JsonElement element = JsonParser.parseString(json);
            JsonObject obj = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String key = entry.getKey();
                if (!key.startsWith("_")) {
                    list.add(key);
                }
            }
            return list;
        } catch (JsonParseException e) {
            logger.debug("Could not parse devices JSON '{}'", json, e);
            throw new BondException("@text/offline.comm-error.unparseable-response");
        }
    }

    /**
     * Gets basic device information
     *
     * @param deviceId The ID of the device
     * @return the {@link BondDevice}
     * @throws BondException
     */
    public BondDevice getDevice(String deviceId) throws BondException {
        String json = request("/v2/devices/" + deviceId);
        logger.trace("BondHome device info : {}", json);
        try {
            BondDevice device = Objects.requireNonNull(gson.fromJson(json, BondDevice.class));
            device.actions.removeIf(Objects::isNull);
            return device;
        } catch (JsonParseException e) {
            logger.debug("Could not parse device {}'s JSON '{}'", deviceId, json, e);
            throw new BondException("@text/offline.comm-error.unparseable-response");
        }
    }

    /**
     * Gets the current state of a device
     *
     * @param deviceId The ID of the device
     * @return the {@link BondDeviceState}
     * @throws BondException
     */
    public BondDeviceState getDeviceState(String deviceId) throws BondException {
        String json = request("/v2/devices/" + deviceId + "/state");
        logger.trace("BondHome device state : {}", json);
        try {
            return Objects.requireNonNull(gson.fromJson(json, BondDeviceState.class));
        } catch (JsonParseException e) {
            logger.debug("Could not parse device {}'s state JSON '{}'", deviceId, json, e);
            throw new BondException("@text/offline.comm-error.unparseable-response");
        }
    }

    /**
     * Gets the current properties of a device
     *
     * @param deviceId The ID of the device
     * @return the {@link BondDeviceProperties}
     * @throws BondException
     */
    public BondDeviceProperties getDeviceProperties(String deviceId) throws BondException {
        String json = request("/v2/devices/" + deviceId + "/properties");
        logger.trace("BondHome device properties : {}", json);
        try {
            return Objects.requireNonNull(gson.fromJson(json, BondDeviceProperties.class));
        } catch (JsonParseException e) {
            logger.debug("Could not parse device {}'s property JSON '{}'", deviceId, json, e);
            throw new BondException("@text/offline.comm-error.unparseable-response");
        }
    }

    /**
     * Executes a device action
     *
     * @param deviceId The ID of the device
     * @param action The Bond action
     * @param argument An additional argument for the actions (such as the fan speed)
     */
    public synchronized void executeDeviceAction(String deviceId, BondDeviceAction action, @Nullable Integer argument) {
        String url = "http://" + bridgeHandler.getBridgeIpAddress() + "/v2/devices/" + deviceId + "/actions/"
                + action.getActionId();
        String payload = "{}";
        if (argument != null) {
            payload = "{\"argument\":" + argument + "}";
        }
        InputStream content = new ByteArrayInputStream(payload.getBytes());
        logger.debug("HTTP PUT to {} with content {}", url, payload);

        final HttpClient httpClient = httpClientFactory.getCommonHttpClient();
        final Request request = httpClient.newRequest(url).method(HttpMethod.PUT)
                .header("BOND-Token", bridgeHandler.getBridgeToken())
                .timeout(BOND_API_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        try (final InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(content)) {
            request.content(inputStreamContentProvider, "application/json");
        }
        ContentResponse response;
        try {
            response = request.send();
        } catch (Exception e) {
            logger.debug("Unable to execute device action {} against device {}: {}", deviceId, action, e.getMessage());
            return;
        }

        logger.debug("HTTP response from {}: {}", deviceId, response.getStatus());
    }

    /**
     * Submit GET request and return response, check for invalid responses
     *
     * @param uri: URI (e.g. "/settings")
     */
    private synchronized String request(String uri) throws BondException {
        String httpResponse = "ERROR";
        String url = "http://" + bridgeHandler.getBridgeIpAddress() + uri;
        int numRetriesRemaining = 3;
        do {
            try {
                logger.debug("HTTP GET to {}", url);

                final HttpClient httpClient = httpClientFactory.getCommonHttpClient();
                final Request request = httpClient.newRequest(url).method(HttpMethod.GET).header("BOND-Token",
                        bridgeHandler.getBridgeToken());
                ContentResponse response;
                response = request.timeout(BOND_API_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
                String encoding = response.getEncoding() != null ? response.getEncoding().replace("\"", "").trim()
                        : StandardCharsets.UTF_8.name();
                try {
                    httpResponse = new String(response.getContent(), encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new BondException("@text/offline.comm-error.no-response");
                }
                // handle known errors
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    // Don't retry or throw an exception if we get unauthorized, just set the bridge offline
                    bridgeHandler.setBridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error.incorrect-local-token");
                    throw new BondException("@text/offline.conf-error.incorrect-local-token", true);
                }
                if (response.getStatus() == HttpStatus.NOT_FOUND_404) {
                    throw new BondException("@text/offline.comm-error.device-not-found");
                }
                // all api responses return Json. If we get something else it must
                // be an error message, e.g. http result code
                if (!httpResponse.startsWith("{") && !httpResponse.startsWith("[")) {
                    throw new BondException("@text/offline.comm-error.unexpected-response");
                }

                logger.debug("HTTP response from request to {}: {}", uri, httpResponse);
                return httpResponse;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Last request to Bond Bridge failed; {} retries remaining: {}", numRetriesRemaining,
                        e.getMessage());
                numRetriesRemaining--;
                if (numRetriesRemaining == 0) {
                    logger.debug("Repeated Bond API calls to {} failed.", uri);
                    bridgeHandler.setBridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error.api-call-failed");
                    throw new BondException("@text/offline.comm-error.api-call-failed", true);
                }
            }
        } while (true);
    }
}
