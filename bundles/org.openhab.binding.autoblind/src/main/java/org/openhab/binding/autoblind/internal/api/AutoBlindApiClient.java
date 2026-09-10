/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.autoblind.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.autoblind.internal.AutoBlindBindingConstants;
import org.openhab.binding.autoblind.internal.api.dto.AllPeripheralResponse;
import org.openhab.binding.autoblind.internal.api.dto.RegistrationResponse;
import org.openhab.binding.autoblind.internal.api.dto.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * HTTP client for the AutoBlind hub local API on port 10123.
 *
 * @author Stephen Berg - Initial contribution
 */
@NonNullByDefault
public class AutoBlindApiClient {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String PATH_REGISTRATION = "/NM/v1/registration";
    private static final String PATH_GET_ALL_PERIPHERAL = "/NM/v1/GetAllPeripheral";
    private static final String PATH_STATUS = "/NM/v1/status";
    private static final String PATH_CONTROL = "/NM/v1/control";
    private static final String PATH_NOTIFICATION = "/NM/v1/notification";

    private static final String KEY_TIMESTAMP = "Timestamp";
    private static final String KEY_THING_NAME = "ThingName";
    private static final String KEY_TASK_ID = "TaskID";
    private static final String KEY_PERIPHERAL_UID = "PeripheralUID";
    private static final String KEY_BOTTOM_RAIL_POSITION = "BottomRailPosition";
    private static final String KEY_TIMEOUT = "Timeout";

    private final Logger logger = LoggerFactory.getLogger(AutoBlindApiClient.class);
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final AtomicLong lastCommandTime = new AtomicLong(0);

    private @Nullable String thingName;

    public AutoBlindApiClient(HttpClient httpClient, String host) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + host + ":" + AutoBlindBindingConstants.API_PORT;
    }

    private long nextTimestamp() {
        return System.currentTimeMillis();
    }

    private String post(String path, String jsonBody)
            throws InterruptedException, TimeoutException, ExecutionException {
        return post(path, jsonBody, 10);
    }

    private String post(String path, String jsonBody, int timeoutSec)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = httpClient.newRequest(baseUrl + path).method(HttpMethod.POST)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON).content(new StringContentProvider(jsonBody))
                .timeout(timeoutSec, TimeUnit.SECONDS).send();
        return response.getContentAsString();
    }

    private void enforceCommandSpacing() throws InterruptedException {
        long now = System.currentTimeMillis();
        long last = lastCommandTime.get();
        long elapsed = now - last;
        if (elapsed < AutoBlindBindingConstants.COMMAND_SPACING_MS) {
            Thread.sleep(AutoBlindBindingConstants.COMMAND_SPACING_MS - elapsed);
        }
        lastCommandTime.set(System.currentTimeMillis());
    }

    public RegistrationResponse register() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty(KEY_TIMESTAMP, nextTimestamp());
        String response = post(PATH_REGISTRATION, gson.toJson(body));
        RegistrationResponse reg = gson.fromJson(response, RegistrationResponse.class);
        if (reg == null) {
            reg = new RegistrationResponse();
        }
        this.thingName = reg.thingName;
        return reg;
    }

    public AllPeripheralResponse getAllPeripherals() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty(KEY_THING_NAME, thingName);
        body.addProperty(KEY_TASK_ID, nextTimestamp());
        body.addProperty(KEY_TIMESTAMP, nextTimestamp());
        String response = post(PATH_GET_ALL_PERIPHERAL, gson.toJson(body));
        AllPeripheralResponse result = gson.fromJson(response, AllPeripheralResponse.class);
        return result != null ? result : new AllPeripheralResponse();
    }

    public StatusResponse getStatus() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty(KEY_THING_NAME, thingName);
        body.addProperty(KEY_TIMESTAMP, nextTimestamp());
        String response = post(PATH_STATUS, gson.toJson(body));
        StatusResponse result = gson.fromJson(response, StatusResponse.class);
        return result != null ? result : new StatusResponse();
    }

    public synchronized void controlShade(int peripheralUid, int position)
            throws InterruptedException, TimeoutException, ExecutionException {
        enforceCommandSpacing();
        JsonObject body = new JsonObject();
        body.addProperty(KEY_PERIPHERAL_UID, peripheralUid);
        body.addProperty(KEY_BOTTOM_RAIL_POSITION, position);
        body.addProperty(KEY_TASK_ID, nextTimestamp());
        body.addProperty(KEY_THING_NAME, thingName);
        body.addProperty(KEY_TIMESTAMP, nextTimestamp());
        post(PATH_CONTROL, gson.toJson(body));
        logger.debug("Sent position {} to shade {}", position, peripheralUid);
    }

    public @Nullable String notification(long timestampSec, float timeoutSec) {
        JsonObject body = new JsonObject();
        body.addProperty(KEY_THING_NAME, thingName);
        body.addProperty(KEY_TIMESTAMP, timestampSec);
        body.addProperty(KEY_TIMEOUT, timeoutSec);
        int httpTimeout = (int) timeoutSec + AutoBlindBindingConstants.NOTIFICATION_HTTP_BUFFER_SEC;
        try {
            String response = post(PATH_NOTIFICATION, gson.toJson(body), httpTimeout);
            if (response != null && !response.isBlank()) {
                return response;
            }
        } catch (TimeoutException e) {
            logger.debug("Notification long-poll timed out (normal)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.debug("Notification request failed: {}", e.getMessage());
        }
        return null;
    }

    public @Nullable String getThingName() {
        return thingName;
    }
}
