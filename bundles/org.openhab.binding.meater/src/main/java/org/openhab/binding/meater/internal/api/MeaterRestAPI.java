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
package org.openhab.binding.meater.internal.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.meater.internal.MeaterBridgeConfiguration;
import org.openhab.binding.meater.internal.MeaterException;
import org.openhab.binding.meater.internal.dto.MeaterProbeDTO;
import org.openhab.binding.meater.internal.dto.MeaterProbeDTO.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MeaterRestAPI} class defines the Meater REST API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterRestAPI {
    private static final String API_ENDPOINT = "https://public-api.cloud.meater.com/v1/";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String LOGIN = "login";
    private static final String DEVICES = "devices";
    private static final int MAX_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(MeaterRestAPI.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final MeaterBridgeConfiguration configuration;
    private String authToken = "";
    private String userId = "";

    public MeaterRestAPI(MeaterBridgeConfiguration configuration, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    public boolean refresh(Map<String, MeaterProbeDTO.Device> meaterProbeThings) {
        try {
            // Login
            login();

            MeaterProbeDTO dto = getDevices(MeaterProbeDTO.class);
            List<Device> devices = dto.getData().getDevices();

            if (devices != null) {
                for (Device meaterProbe : devices) {
                    meaterProbeThings.put(meaterProbe.id, meaterProbe);
                }
            }
            return true;
        } catch (MeaterException | JsonSyntaxException e) {
            logger.warn("Failed to refresh! {}", e.getMessage());
        }
        return false;
    }

    private void login() throws MeaterException {
        try {
            // Login
            String json = "{ \"email\": \"" + configuration.email + "\",  \"password\": \"" + configuration.password
                    + "\" }";
            Request request = httpClient.newRequest(API_ENDPOINT + LOGIN).method(HttpMethod.POST);
            request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
            request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
            request.content(new StringContentProvider(json), JSON_CONTENT_TYPE);

            logger.debug("HTTP POST Request {}.", request.toString());

            ContentResponse httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new MeaterException("Failed to login " + httpResponse.getContentAsString());
            }
            // Fetch JWT
            json = httpResponse.getContentAsString();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonObject childObject = jsonObject.getAsJsonObject("data");
            this.authToken = childObject.get("token").getAsString();
            this.userId = childObject.get("userId").getAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new MeaterException(e);
        }
    }

    private String getFromApi(String uri) throws MeaterException, InterruptedException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = httpClient.newRequest(API_ENDPOINT + uri).method(HttpMethod.GET);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("getFromApi failed, HTTP status: {}", response.getStatus());
                        login();
                    } else {
                        return content;
                    }
                } catch (TimeoutException e) {
                    logger.debug("TimeoutException error in get: {}", e.getMessage());
                }
            }
            throw new MeaterException("Failed to fetch from API!");
        } catch (JsonSyntaxException | MeaterException | ExecutionException e) {
            throw new MeaterException(e);
        }
    }

    public <T> T getDevices(Class<T> dto) throws MeaterException {
        String uri = DEVICES;
        String json;

        try {
            json = getFromApi(uri);
        } catch (MeaterException | InterruptedException e) {
            throw new MeaterException(e);
        }
        return gson.fromJson(json, dto);
    }
}
