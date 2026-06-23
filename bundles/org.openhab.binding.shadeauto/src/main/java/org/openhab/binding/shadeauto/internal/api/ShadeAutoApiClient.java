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
package org.openhab.binding.shadeauto.internal.api;

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
import org.openhab.binding.shadeauto.internal.ShadeAutoBindingConstants;
import org.openhab.binding.shadeauto.internal.api.dto.AllPeripheralResponse;
import org.openhab.binding.shadeauto.internal.api.dto.RegistrationResponse;
import org.openhab.binding.shadeauto.internal.api.dto.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * HTTP client for the ShadeAuto hub local API on port 10123.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
public class ShadeAutoApiClient {

    private final Logger logger = LoggerFactory.getLogger(ShadeAutoApiClient.class);
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final AtomicLong lastCommandTime = new AtomicLong(0);

    private @Nullable String thingName;

    public ShadeAutoApiClient(HttpClient httpClient, String host) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + host + ":" + ShadeAutoBindingConstants.API_PORT;
    }

    private long nextTimestamp() {
        return System.currentTimeMillis();
    }

    private String post(String path, String jsonBody)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = httpClient.newRequest(baseUrl + path).method(HttpMethod.POST)
                .header("Content-Type", "application/json").content(new StringContentProvider(jsonBody))
                .timeout(10, TimeUnit.SECONDS).send();
        return response.getContentAsString();
    }

    private void enforceCommandSpacing() throws InterruptedException {
        long now = System.currentTimeMillis();
        long last = lastCommandTime.get();
        long elapsed = now - last;
        if (elapsed < ShadeAutoBindingConstants.COMMAND_SPACING_MS) {
            Thread.sleep(ShadeAutoBindingConstants.COMMAND_SPACING_MS - elapsed);
        }
        lastCommandTime.set(System.currentTimeMillis());
    }

    public RegistrationResponse register() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty("Timestamp", nextTimestamp());
        String response = post("/NM/v1/registration", gson.toJson(body));
        RegistrationResponse reg = gson.fromJson(response, RegistrationResponse.class);
        if (reg == null) {
            reg = new RegistrationResponse();
        }
        this.thingName = reg.thingName;
        return reg;
    }

    public AllPeripheralResponse getAllPeripherals() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty("ThingName", thingName);
        body.addProperty("TaskID", nextTimestamp());
        body.addProperty("Timestamp", nextTimestamp());
        String response = post("/NM/v1/GetAllPeripheral", gson.toJson(body));
        AllPeripheralResponse result = gson.fromJson(response, AllPeripheralResponse.class);
        return result != null ? result : new AllPeripheralResponse();
    }

    public StatusResponse getStatus() throws InterruptedException, TimeoutException, ExecutionException {
        JsonObject body = new JsonObject();
        body.addProperty("ThingName", thingName);
        body.addProperty("Timestamp", nextTimestamp());
        String response = post("/NM/v1/status", gson.toJson(body));
        StatusResponse result = gson.fromJson(response, StatusResponse.class);
        return result != null ? result : new StatusResponse();
    }

    public synchronized void controlShade(int peripheralUid, int position)
            throws InterruptedException, TimeoutException, ExecutionException {
        enforceCommandSpacing();
        JsonObject body = new JsonObject();
        body.addProperty("PeripheralUID", peripheralUid);
        body.addProperty("BottomRailPosition", position);
        body.addProperty("TaskID", nextTimestamp());
        body.addProperty("ThingName", thingName);
        body.addProperty("Timestamp", nextTimestamp());
        post("/NM/v1/control", gson.toJson(body));
        logger.debug("Sent position {} to shade {}", position, peripheralUid);
    }

    public @Nullable String getThingName() {
        return thingName;
    }
}
