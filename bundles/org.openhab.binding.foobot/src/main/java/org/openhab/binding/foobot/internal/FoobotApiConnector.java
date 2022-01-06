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
package org.openhab.binding.foobot.internal;

import static org.openhab.binding.foobot.internal.FoobotBindingConstants.*;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.foobot.internal.json.FoobotDevice;
import org.openhab.binding.foobot.internal.json.FoobotJsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Connector class communicating with Foobot api and parsing returned json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class FoobotApiConnector {

    public static final String API_RATE_LIMIT_EXCEEDED_MESSAGE = "Api rate limit exceeded";
    public static final int API_RATE_LIMIT_EXCEEDED = -2;

    private static final int UNKNOWN_REMAINING = -1;
    private static final String HEADER_X_API_KEY_TOKEN = "X-API-KEY-TOKEN";
    private static final String HEADER_X_API_KEY_LIMIT_REMAINING = "x-api-key-limit-remaining";
    private static final int REQUEST_TIMEOUT_SECONDS = 3;
    private static final Gson GSON = new Gson();
    private static final Type FOOTBOT_DEVICE_LIST_TYPE = new TypeToken<ArrayList<FoobotDevice>>() {
    }.getType();

    private final Logger logger = LoggerFactory.getLogger(FoobotApiConnector.class);

    private @Nullable HttpClient httpClient;
    private String apiKey = "";
    private int apiKeyLimitRemaining = UNKNOWN_REMAINING;

    public void setHttpClient(@Nullable HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return Returns the last known api remaining limit or -1 if not known.
     */
    public int getApiKeyLimitRemaining() {
        return apiKeyLimitRemaining;
    }

    /**
     * Retrieves the list of associated devices with the given username from the foobot api.
     *
     * @param username to get the associated devices for
     * @return List of devices
     * @throws FoobotApiException in case there was a problem communicating or parsing the response
     */
    public synchronized List<FoobotDevice> getAssociatedDevices(String username) throws FoobotApiException {
        try {
            final String url = URL_TO_FETCH_DEVICES.replace("%username%",
                    URLEncoder.encode(username, StandardCharsets.UTF_8));
            logger.debug("URL = {}", url);

            List<FoobotDevice> foobotDevices = GSON.fromJson(request(url, apiKey), FOOTBOT_DEVICE_LIST_TYPE);
            return Objects.requireNonNull(foobotDevices);
        } catch (JsonParseException e) {
            throw new FoobotApiException(0, e.getMessage());
        }
    }

    /**
     * Retrieves the sensor data for the device with the given uuid from the foobot api.
     *
     * @param uuid of the device to get the sensor data for
     * @return sensor data of the device
     * @throws FoobotApiException in case there was a problem communicating or parsing the response
     */
    public synchronized @Nullable FoobotJsonData getSensorData(String uuid) throws FoobotApiException {
        try {
            final String url = URL_TO_FETCH_SENSOR_DATA.replace("%uuid%",
                    URLEncoder.encode(uuid, StandardCharsets.UTF_8));
            logger.debug("URL = {}", url);

            return GSON.fromJson(request(url, apiKey), FoobotJsonData.class);
        } catch (JsonParseException e) {
            throw new FoobotApiException(0, e.getMessage());
        }
    }

    protected String request(String url, String apiKey) throws FoobotApiException {
        apiKeyLimitRemaining = UNKNOWN_REMAINING;
        if (httpClient == null) {
            logger.debug("No http connection possible: httpClient == null");
            throw new FoobotApiException(0, "No http connection possible");
        }
        final Request request = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.ACCEPT_ENCODING, StandardCharsets.UTF_8.name());
        request.header(HEADER_X_API_KEY_TOKEN, apiKey);
        final ContentResponse response;

        try {
            response = request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoobotApiException(0, e.getMessage());
        } catch (TimeoutException | ExecutionException e) {
            throw new FoobotApiException(0, e.getMessage());
        }
        final String content = response.getContentAsString();

        logger.trace("Foobot content = {}", content);
        logger.debug("Foobot response = {}", response);
        setApiKeyLimitRemaining(response);
        switch (response.getStatus()) {
            case HttpStatus.FORBIDDEN_403:
                throw new FoobotApiException(response.getStatus(),
                        "Access denied. Did you set the correct api-key and/or username?");
            case HttpStatus.TOO_MANY_REQUESTS_429:
                apiKeyLimitRemaining = API_RATE_LIMIT_EXCEEDED;
                throw new FoobotApiException(response.getStatus(), API_RATE_LIMIT_EXCEEDED_MESSAGE);
            case HttpStatus.OK_200:
                if (content == null || content.isBlank()) {
                    throw new FoobotApiException(0, "No data returned");
                }
                return content;
            default:
                logger.trace("Foobot returned status '{}', reason: {}, content = {}", response.getStatus(),
                        response.getReason(), content);
                throw new FoobotApiException(response.getStatus(), response.getReason());
        }
    }

    private void setApiKeyLimitRemaining(ContentResponse response) {
        final HttpField field = response.getHeaders().getField(HEADER_X_API_KEY_LIMIT_REMAINING);

        if (field != null) {
            apiKeyLimitRemaining = field.getIntValue();
        }
    }
}
