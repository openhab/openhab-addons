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
package org.openhab.binding.meater.internal.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.meater.internal.MeaterBridgeConfiguration;
import org.openhab.binding.meater.internal.dto.MeaterProbeDTO;
import org.openhab.binding.meater.internal.dto.MeaterProbeDTO.Device;
import org.openhab.binding.meater.internal.exceptions.MeaterAuthenticationException;
import org.openhab.binding.meater.internal.exceptions.MeaterException;
import org.openhab.core.i18n.LocaleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MeaterRestAPI} class defines the MEATER REST API
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
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(MeaterRestAPI.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final MeaterBridgeConfiguration configuration;
    private String authToken = "";
    private LocaleProvider localeProvider;

    public MeaterRestAPI(MeaterBridgeConfiguration configuration, Gson gson, HttpClient httpClient,
            LocaleProvider localeProvider) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
    }

    public boolean refresh(Map<String, MeaterProbeDTO.Device> meaterProbeThings) {
        try {
            MeaterProbeDTO dto = getDevices(MeaterProbeDTO.class);
            if (dto != null) {
                List<Device> devices = dto.getData().getDevices();
                if (devices != null) {
                    if (!devices.isEmpty()) {
                        for (Device meaterProbe : devices) {
                            meaterProbeThings.put(meaterProbe.id, meaterProbe);
                        }
                    } else {
                        meaterProbeThings.clear();
                    }
                    return true;
                }
            }
        } catch (MeaterException e) {
            logger.warn("Failed to refresh! {}", e.getMessage());
        }
        return false;
    }

    private void login() throws MeaterException {
        try {
            // Login
            String json = "{ \"email\": \"" + configuration.email + "\",  \"password\": \"" + configuration.password
                    + "\" }";
            Request request = httpClient.newRequest(API_ENDPOINT + LOGIN).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
            request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
            request.content(new StringContentProvider(json), JSON_CONTENT_TYPE);

            logger.trace("{}.", request.toString());

            ContentResponse httpResponse = request.send();
            if (!HttpStatus.isSuccess(httpResponse.getStatus())) {
                throw new MeaterException("Failed to login " + httpResponse.getContentAsString());
            }
            // Fetch JWT
            json = httpResponse.getContentAsString();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonObject childObject = jsonObject.getAsJsonObject("data");
            JsonElement tokenJson = childObject.get("token");
            if (tokenJson != null) {
                this.authToken = tokenJson.getAsString();
            } else {
                throw new MeaterException("Token is not present in the JSON response");
            }
        } catch (TimeoutException | ExecutionException | JsonParseException e) {
            throw new MeaterException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MeaterException(e);
        }
    }

    private String getFromApi(String uri) throws MeaterException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = httpClient.newRequest(API_ENDPOINT + uri).method(HttpMethod.GET)
                            .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
                    request.header(HttpHeader.ACCEPT_LANGUAGE, localeProvider.getLocale().getLanguage());

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                        // This will currently not happen because "WWW-Authenticate" header is missing; see below.
                        logger.debug("getFromApi failed, authentication failed, HTTP status: 401");
                        throw new MeaterAuthenticationException("Authentication failed");
                    } else if (!HttpStatus.isSuccess(response.getStatus())) {
                        logger.debug("getFromApi failed, HTTP status: {}", response.getStatus());
                        throw new MeaterException("Failed to fetch from API!");
                    } else {
                        return content;
                    }
                } catch (TimeoutException e) {
                    logger.debug("TimeoutException error in get: {}", e.getMessage());
                }
            }
            throw new MeaterException("Failed to fetch from API!");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpResponseException httpResponseException) {
                Response response = httpResponseException.getResponse();
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    /*
                     * When contextId is not valid, the service will respond with HTTP code 401 without
                     * any "WWW-Authenticate" header, violating RFC 7235. Jetty will then throw
                     * HttpResponseException. We need to handle this in order to attempt
                     * reauthentication.
                     */
                    logger.debug("getFromApi failed, authentication failed, HTTP status: 401");
                    throw new MeaterAuthenticationException("Authentication failed");
                }
            }
            throw new MeaterException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MeaterException(e);
        }
    }

    public @Nullable <T> T getDevices(Class<T> dto) throws MeaterException {
        String uri = DEVICES;
        String json = "";

        if (authToken.isEmpty()) {
            login();
        }

        try {
            json = getFromApi(uri);
        } catch (MeaterAuthenticationException e) {
            logger.debug("getFromApi failed {}", e.getMessage());
            this.authToken = "";
            login();
            json = getFromApi(uri);
        }

        if (json.isEmpty()) {
            throw new MeaterException("JSON from API is empty!");
        } else {
            try {
                return gson.fromJson(json, dto);
            } catch (JsonSyntaxException e) {
                throw new MeaterException("Error parsing JSON", e);
            }
        }
    }
}
