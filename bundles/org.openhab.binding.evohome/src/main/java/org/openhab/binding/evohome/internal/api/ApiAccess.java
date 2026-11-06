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
package org.openhab.binding.evohome.internal.api;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
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
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Provides access to (an optionally OAUTH based) API. Makes sure that all the necessary headers are set.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Propagate failures and respect retry delays
 *
 */
@NonNullByDefault
public class ApiAccess {
    private static final int REQUEST_TIMEOUT_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(ApiAccess.class);
    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().create();

    private @Nullable Authentication authenticationData;
    private @Nullable String applicationId;
    private Instant retryAfter = Instant.MIN;

    public ApiAccess(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the authentication details on the type
     *
     * @param authentication The authentication details to apply
     */
    public void setAuthentication(@Nullable Authentication authentication) {
        authenticationData = authentication;
    }

    /**
     * Gets the current authentication details of the type
     *
     * @return The current authentication details
     */
    public @Nullable Authentication getAuthentication() {
        return authenticationData;
    }

    /**
     * Sets the application id on the type
     *
     * @param applicationId The application id to apply
     */
    public void setApplicationId(@Nullable String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Issues an HTTP request on the API's URL. Makes sure that the request is correctly formatted.
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param headers The optional additional headers to apply, can be null
     * @param requestData The optional request data to use, can be null
     * @param contentType The content type to use with the request data. Required when using requestData
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    public @Nullable <TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers,
            @Nullable String requestData, String contentType, @Nullable Class<TOut> outClass)
            throws TimeoutException, EvohomeApiClientException {
        if (Instant.now().isBefore(retryAfter)) {
            throw new EvohomeApiClientException("Waiting before retrying API request", 429,
                    Math.max(1, Duration.between(Instant.now(), retryAfter).getSeconds() + 1));
        }
        logger.debug("Requesting: [{}]", url);
        try {
            Request request = httpClient.newRequest(url).method(method);

            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }

            if (requestData != null) {
                request.content(new StringContentProvider(requestData), contentType);
            }

            ContentResponse response = request.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

            int status = response.getStatus();
            logger.trace("HTTP response status: {}", status);
            if (status < 200 || status >= 300) {
                long delay = 0;
                if (status == 429 || status == 503) {
                    delay = parseRetryAfter(response.getHeaders().get("Retry-After"));
                    if (status == 429) {
                        delay = Math.max(60, delay);
                    }
                    retryAfter = Instant.now().plusSeconds(delay);
                }
                throw new EvohomeApiClientException("API request failed: HTTP " + status, status, delay);
            }
            if (outClass == null) {
                return null;
            }
            @Nullable
            TOut result = gson.fromJson(response.getContentAsString(), outClass);
            if (result == null) {
                throw new EvohomeApiClientException("Empty API response");
            }
            return result;
        } catch (ExecutionException e) {
            throw new EvohomeApiClientException("API communication failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EvohomeApiClientException("API request interrupted", e);
        } catch (JsonParseException e) {
            throw new EvohomeApiClientException("Invalid API response", e);
        }
    }

    private long parseRetryAfter(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        try {
            long seconds = Long.parseLong(value.trim());
            Instant.now().plusSeconds(seconds);
            return Math.max(0, seconds);
        } catch (NumberFormatException e) {
            try {
                Instant deadline = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                return Math.max(0, Duration.between(Instant.now(), deadline).getSeconds() + 1);
            } catch (DateTimeException ex) {
                return 0;
            }
        } catch (DateTimeException | ArithmeticException e) {
            return 0;
        }
    }

    /**
     * Issues an HTTP GET request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param url The URL to query
     * @param outClass The type of the requested result
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    public @Nullable <TOut> TOut doAuthenticatedGet(String url, Class<TOut> outClass)
            throws TimeoutException, EvohomeApiClientException {
        return doAuthenticatedRequest(HttpMethod.GET, url, null, outClass);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param url The URL to query
     * @param requestContainer The object to use as JSON data for the request
     * @throws TimeoutException Thrown when a request times out
     */
    public void doAuthenticatedPut(String url, Object requestContainer)
            throws TimeoutException, EvohomeApiClientException {
        doAuthenticatedRequest(HttpMethod.PUT, url, requestContainer, null);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input.
     * Makes sure that the request is correctly formatted.*
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param headers The optional additional headers to apply, can be null
     * @param requestContainer The object to use as JSON data for the request, can be null
     * @param outClass The type of the requested result, can be null
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    private @Nullable <TOut> TOut doRequest(HttpMethod method, String url, Map<String, String> headers,
            @Nullable Object requestContainer, @Nullable Class<TOut> outClass)
            throws TimeoutException, EvohomeApiClientException {
        String json = null;
        if (requestContainer != null) {
            json = gson.toJson(requestContainer);
        }

        return doRequest(method, url, headers, json, "application/json", outClass);
    }

    /**
     * Issues an HTTP request on the API's URL, using an object that is serialized to JSON as input and
     * using the authentication applied to the type.
     * Makes sure that the request is correctly formatted.*
     *
     * @param method The HTTP method to use (POST, GET, ...)
     * @param url The URL to query
     * @param requestContainer The object to use as JSON data for the request
     * @param outClass The type of the requested result
     * @return The result of the request or null
     * @throws TimeoutException Thrown when a request times out
     */
    private @Nullable <TOut> TOut doAuthenticatedRequest(HttpMethod method, String url,
            @Nullable Object requestContainer, @Nullable Class<TOut> outClass)
            throws TimeoutException, EvohomeApiClientException {
        Map<String, String> headers = new HashMap<>();
        Authentication localAuthenticationData = authenticationData;
        String localApplicationId = applicationId;

        if (localAuthenticationData != null) {
            headers.put("Authorization", "Bearer " + localAuthenticationData.getAccessToken());
        }
        if (localApplicationId != null) {
            headers.put("applicationId", localApplicationId);
        }
        headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

        return doRequest(method, url, headers, requestContainer, outClass);
    }
}
