/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.teslapowerwall.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.teslapowerwall.internal.api.BatterySOE;
import org.openhab.binding.teslapowerwall.internal.api.GridStatus;
import org.openhab.binding.teslapowerwall.internal.api.MeterAggregates;
import org.openhab.binding.teslapowerwall.internal.api.Operations;
import org.openhab.binding.teslapowerwall.internal.api.SystemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles performing the actual HTTP requests for communicating with Tesla Powerwall units.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class TeslaPowerwallWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String getBatterySOEUri;
    private String getGridStatusUri;
    private String getSystemStatusUri;
    private String getMeterAggregatesUri;
    private String getTokenUri;
    private String getOperationUri;
    private String token = "";
    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallWebTargets.class);
    private HttpClient httpClient;

    private final Gson gson = new Gson();

    public TeslaPowerwallWebTargets(String hostname, HttpClient httpClient) {
        this.httpClient = httpClient;
        String baseUri = "https://" + hostname + "/";
        getBatterySOEUri = baseUri + "api/system_status/soe";
        getGridStatusUri = baseUri + "api/system_status/grid_status";
        getSystemStatusUri = baseUri + "api/system_status";
        getMeterAggregatesUri = baseUri + "api/meters/aggregates";
        getTokenUri = baseUri + "api/login/Basic";
        getOperationUri = baseUri + "api/operation";
    }

    @Nullable
    public BatterySOE getBatterySOE(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        String response = invoke(getBatterySOEUri, email, password);
        return gson.fromJson(response, BatterySOE.class);
    }

    @Nullable
    public GridStatus getGridStatus(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        String response = invoke(getGridStatusUri, email, password);
        return gson.fromJson(response, GridStatus.class);
    }

    @Nullable
    public SystemStatus getSystemStatus(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        String response = invoke(getSystemStatusUri, email, password);
        return gson.fromJson(response, SystemStatus.class);
    }

    @Nullable
    public MeterAggregates getMeterAggregates(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        String response = invoke(getMeterAggregatesUri, email, password);
        return gson.fromJson(response, MeterAggregates.class);
    }

    @Nullable
    public Operations getOperations(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        String response = invoke(getOperationUri, email, password);
        return gson.fromJson(response, Operations.class);
    }

    public String getToken(String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", "customer");
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("force_sm_off", false);
        logger.debug("logonjson = {}", jsonObject.toString());
        String response = invoke(getTokenUri, HttpMethod.POST, "Content-Type", "application/json",
                jsonObject.toString());
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        String token = jsonResponse.get("token").getAsString();
        logger.debug("Token: {}", token);
        return token;
    }

    private String invoke(String uri, String email, String password)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        if (token.isEmpty()) {
            token = getToken(email, password);
        }
        return invoke(uri, HttpMethod.GET, "Authorization", "Bearer " + token, "");
    }

    private String invoke(String uri, HttpMethod method, String headerKey, String headerValue, String params)
            throws TeslaPowerwallCommunicationException, TeslaPowerwallAuthenticationException {
        logger.debug("Calling url: {}", uri);
        int status = 0;
        String jsonResponse = "";
        synchronized (this) {
            try {
                Request request = httpClient.newRequest(uri).method(method).header(headerKey, headerValue)
                        .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .content(new StringContentProvider(params), "application/json");
                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {}", method, uri);
                }
                ContentResponse response = request.send();
                status = response.getStatus();
                jsonResponse = response.getContentAsString();
                if (!jsonResponse.isEmpty()) {
                    logger.trace("JSON response: '{}'", jsonResponse);
                }
                if (status == HttpStatus.UNAUTHORIZED_401) {
                    throw new TeslaPowerwallAuthenticationException("Unauthorized");
                }
                if (!HttpStatus.isSuccess(status)) {
                    throw new TeslaPowerwallCommunicationException(
                            String.format("Tesla Powerwall returned error <%d> while invoking %s", status, uri));
                }
            } catch (TimeoutException | ExecutionException | InterruptedException ex) {
                throw new TeslaPowerwallCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
