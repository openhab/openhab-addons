/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.warmup.internal.WarmupBindingConstants;
import org.openhab.binding.warmup.internal.handler.MyWarmupConfigurationDTO;
import org.openhab.binding.warmup.internal.model.auth.AuthRequestDTO;
import org.openhab.binding.warmup.internal.model.auth.AuthResponseDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.core.library.types.OnOffType;
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

    private void validateSession() throws MyWarmupApiException {
        if (authToken == null) {
            authenticate();
        }
    }

    private void authenticate() throws MyWarmupApiException {
        String body = GSON.toJson(new AuthRequestDTO(configuration.username, configuration.password,
                WarmupBindingConstants.AUTH_METHOD, WarmupBindingConstants.AUTH_APP_ID));

        ContentResponse response = callWarmup(WarmupBindingConstants.APP_ENDPOINT, body, false);

        AuthResponseDTO ar = GSON.fromJson(response.getContentAsString(), AuthResponseDTO.class);

        if (ar != null && ar.getStatus() != null && ar.getStatus().getResult().equals("success")) {
            authToken = ar.getResponse().getToken();
        } else {
            throw new MyWarmupApiException("Authentication Failed");
        }
    }

    /**
     * Query the API to get the status of all devices connected to the Bridge.
     *
     * @return The {@link QueryResponseDTO} object if retrieved, else null
     * @throws MyWarmupApiException API callout error
     */
    public synchronized QueryResponseDTO getStatus() throws MyWarmupApiException {
        return callWarmupGraphQL("query QUERY { user { locations{ id name "
                + " rooms { id roomName runMode overrideDur targetTemp currentTemp "
                + " thermostat4ies{ deviceSN lastPoll }}}}}");
    }

    /**
     * Call the API to set a temperature override on a specific room
     *
     * @param locationId Id of the location
     * @param roomId Id of the room
     * @param temperature Temperature to set * 10
     * @param duration Duration in minutes of the override
     * @throws MyWarmupApiException API callout error
     */
    public void setOverride(String locationId, String roomId, int temperature, Integer duration)
            throws MyWarmupApiException {
        callWarmupGraphQL(String.format("mutation{deviceOverride(lid:%s,rid:%s,temperature:%d,minutes:%d)}", locationId,
                roomId, temperature, duration));
    }

    /**
     * Call the API to toggle frost protection mode on a specific room
     *
     * @param locationId Id of the location
     * @param roomId Id of the room
     * @param command Temperature to set
     * @throws MyWarmupApiException API callout error
     */
    public void toggleFrostProtectionMode(String locationId, String roomId, OnOffType command)
            throws MyWarmupApiException {
        callWarmupGraphQL(String.format("mutation{turn%s(lid:%s,rid:%s){id}}", command == OnOffType.ON ? "Off" : "On",
                locationId, roomId));
    }

    private QueryResponseDTO callWarmupGraphQL(String body) throws MyWarmupApiException {
        validateSession();
        ContentResponse response = callWarmup(WarmupBindingConstants.QUERY_ENDPOINT, "{\"query\": \"" + body + "\"}",
                true);

        QueryResponseDTO qr = GSON.fromJson(response.getContentAsString(), QueryResponseDTO.class);

        if (qr != null && qr.getStatus().equals("success")) {
            return qr;
        } else {
            throw new MyWarmupApiException("Unexpected reponse from API");
        }
    }

    private synchronized ContentResponse callWarmup(String endpoint, String body, Boolean authenticated)
            throws MyWarmupApiException {
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
            ContentResponse response = request.send();
            logger.trace("Response from my warmup: Status {}, Body {}", response.getStatus(),
                    response.getContentAsString());

            if (response.getStatus() == HttpStatus.OK_200) {
                return response;
            } else if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                logger.debug("Authentication failure {} {}", response.getStatus(), response.getContentAsString());
                authToken = null;
                throw new MyWarmupApiException("Authentication failure");
            } else {
                logger.debug("Unexpected response {} {}", response.getStatus(), response.getContentAsString());
            }
            throw new MyWarmupApiException("Callout failed");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MyWarmupApiException(e);
        }
    }
}
