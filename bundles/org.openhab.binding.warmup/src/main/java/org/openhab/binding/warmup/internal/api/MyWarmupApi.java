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
package org.openhab.binding.warmup.internal.api;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.openhab.binding.warmup.internal.WarmupBindingConstants;
import org.openhab.binding.warmup.internal.handler.MyWarmupConfigurationDTO;
import org.openhab.binding.warmup.internal.model.auth.AuthRequestDTO;
import org.openhab.binding.warmup.internal.model.auth.AuthResponseDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link MyWarmupApi} class contains code specific to calling the My Warmup API.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class MyWarmupApi {

    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(MyWarmupApi.class);
    private final HttpClient httpClient;

    private MyWarmupConfigurationDTO configuration;
    private @Nullable String authToken;

    /**
     * Construct the API client
     *
     * @param httpClient HttpClient to make HTTP Calls
     * @param configuration Thing configuration which contains API credentials
     */
    public MyWarmupApi(final HttpClient httpClient, MyWarmupConfigurationDTO configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
    }

    /**
     * Update the configuration, trigger a refresh of the access token
     *
     * @param configuration contains username and password
     */
    public void setConfiguration(MyWarmupConfigurationDTO configuration) {
        authToken = null;
        this.configuration = configuration;
    }

    private Boolean validateSession() {
        if (authToken == null) {
            return authenticate();
        }
        return true;
    }

    private Boolean authenticate() {
        String body = GSON.toJson(new AuthRequestDTO(configuration.username, configuration.password,
                WarmupBindingConstants.AUTH_METHOD, WarmupBindingConstants.AUTH_APP_ID));

        final ContentResponse response = callWarmup(WarmupBindingConstants.APP_ENDPOINT, body, false);

        AuthResponseDTO ar = GSON.fromJson(response.getContentAsString(), AuthResponseDTO.class);

        if (ar.getStatus().getResult().equals("success")) {
            authToken = ar.getResponse().getToken();
            return true;
        }
        return false;
    }

    /**
     * Query the API to get the status of all devices connected to the Bridge.
     *
     * @return The {@link QueryResponseDTO} object if retrieved, else null
     */
    public synchronized @Nullable QueryResponseDTO getStatus() {
        return callWarmupGraphQL("query QUERY { user { locations{ id name "
                + " rooms { id roomName runMode overrideDur targetTemp currentTemp "
                + " thermostat4ies{ deviceSN airTemp floor1Temp floor2Temp heatingTarget } } "
                + " holiday{ holStart holEnd holTemp } address { timezone }  geoLocation{ latitude longitude }}}}");
    }

    /**
     * Call the API to set a temperature override on a specific room (device)
     *
     * @param locationId Id of the location
     * @param roomId Id of the room
     * @param temperature Temperature to set
     * @param duration Duration in minutes of the override
     */
    public void setOverride(String locationId, String roomId, QuantityType<Temperature> temperature, Integer duration) {
        callWarmupGraphQL(String.format("mutation{deviceOverride(lid:%s,rid:%s,temperature:%d,minutes:%d)}", locationId,
                roomId, temperature.multiply(new BigDecimal(10)).intValue(), duration));
    }

    public void toggleFrostProtectionMode(String locationId, String roomId, OnOffType command) {
        callWarmupGraphQL(String.format("mutation{turn%s(lid:%s,rid:%s){id}}", command == OnOffType.ON ? "Off" : "On",
                locationId, roomId));
    }

    private @Nullable QueryResponseDTO callWarmupGraphQL(String body) {
        if (validateSession()) {
            ContentResponse response = callWarmup(WarmupBindingConstants.QUERY_ENDPOINT,
                    "{\"query\": \"" + body + "\"}", true);

            QueryResponseDTO qr = GSON.fromJson(response.getContentAsString(), QueryResponseDTO.class);

            if (qr.getStatus().equals("success")) {
                return qr;
            }
        }

        authToken = null;
        return null;
    }

    private synchronized @Nullable ContentResponse callWarmup(String endpoint, String body, Boolean authenticated) {
        try {
            final Request request = httpClient.newRequest(endpoint);

            request.method(HttpMethod.POST);

            request.getHeaders().remove(HttpHeader.USER_AGENT);
            request.header(HttpHeader.USER_AGENT, WarmupBindingConstants.USER_AGENT);
            request.header(HttpHeader.CONTENT_TYPE, "application/json");
            request.header("App-Token", WarmupBindingConstants.APP_TOKEN);
            if (authenticated) {
                request.header("Warmup-Authorization", authToken);
            }

            request.content(new StringContentProvider(body));

            request.timeout(10, TimeUnit.SECONDS);

            logger.trace("Sending body to My Warmup: Endpoint {}, Body {}", endpoint, body);
            final ContentResponse response = request.send();
            logger.trace("Response from my warmup: Status {}, Body {}", response.getStatus(),
                    response.getContentAsString());

            if (response.getStatus() == HttpStatus.OK_200) {
                return response;
            } else if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                authToken = null;
            }
        } catch (Exception e) {
            authToken = null;
        }
        return null;
    }
}
