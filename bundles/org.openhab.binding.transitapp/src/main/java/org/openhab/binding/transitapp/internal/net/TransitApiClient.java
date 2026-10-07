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
package org.openhab.binding.transitapp.internal.net;

import static org.openhab.binding.transitapp.internal.TransitAppBindingConstants.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.transitapp.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transitapp.internal.net.dto.TripDetailsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The {@link TransitApiClient} is responsible for handling HTTP communication with the Transit API.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitApiClient {
    private final Logger logger = LoggerFactory.getLogger(TransitApiClient.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    // Configurable parameters from bridge config
    private final long cacheTimeMs;
    private final int retryAfterSeconds;

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private volatile long rateLimitResetTime = 0;

    public TransitApiClient(HttpClient httpClient) {
        this(httpClient, DEFAULT_CACHE_TIME_MS, DEFAULT_RETRY_AFTER_SECONDS);
    }

    public TransitApiClient(HttpClient httpClient, long cacheTimeMs, int retryAfterSeconds) {
        this.httpClient = httpClient;
        this.cacheTimeMs = cacheTimeMs;
        this.retryAfterSeconds = retryAfterSeconds;
        logger.debug("TransitApiClient initialized with cacheTimeMs={}, retryAfterSeconds={}", cacheTimeMs,
                retryAfterSeconds);
    }

    private void checkRateLimit() throws IOException {
        long now = System.currentTimeMillis();
        if (now < rateLimitResetTime) {
            throw new IOException("Rate limit active until " + Instant.ofEpochMilli(rateLimitResetTime));
        }
    }

    private void handleResponseStatus(ContentResponse response) throws IOException {
        long now = System.currentTimeMillis();
        if (response.getStatus() == HttpStatus.TOO_MANY_REQUESTS_429) {
            long delaySeconds = retryAfterSeconds;
            String headerVal = response.getHeaders().get(HttpHeader.RETRY_AFTER);
            if (headerVal != null) {
                try {
                    delaySeconds = Long.parseLong(headerVal);
                } catch (NumberFormatException e) {
                    logger.debug("Invalid Retry-After header value: {}, using configured default: {}", headerVal,
                            retryAfterSeconds);
                }
            }
            rateLimitResetTime = now + (delaySeconds * 1000);
            throw new IOException(
                    "HTTP 429 Too Many Requests. Backoff until " + Instant.ofEpochMilli(rateLimitResetTime));
        }

        if (!HttpStatus.isSuccess(response.getStatus())) {
            throw new IOException("Transit API returned HTTP error status: " + response.getStatus());
        }
    }

    private void cleanupCache(long now) {
        cache.entrySet().removeIf(entry -> (now - entry.getValue().timestamp) >= cacheTimeMs);
    }

    private ContentResponse sendGetRequest(String url, String apiKey) throws IOException, InterruptedException {
        try {
            return httpClient.newRequest(url).method(HttpMethod.GET).header(API_KEY_HEADER, apiKey)
                    .timeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (TimeoutException e) {
            throw new IOException("Request to Transit API timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new IOException(
                    "Request to Transit API failed: " + (cause != null ? cause.getMessage() : e.getMessage()), e);
        }
    }

    public String fetchStopDepartures(String apiKey, String globalStopId, int maxDepartures)
            throws IOException, InterruptedException {
        long now = System.currentTimeMillis();
        cleanupCache(now);

        // Cache key includes maxDepartures to avoid incorrect cache hits
        String cacheKey = globalStopId + ":" + maxDepartures;
        CachedResponse cached = cache.get(cacheKey);
        if (cached != null && (now - cached.timestamp) < cacheTimeMs) {
            return cached.payload;
        }

        checkRateLimit();

        // Passed explicitly as global_stop_id and max_num_departures
        String url = API_BASE_URL + "stop_departures?global_stop_id="
                + URLEncoder.encode(globalStopId, StandardCharsets.UTF_8) + "&max_num_departures=" + maxDepartures;

        ContentResponse response = sendGetRequest(url, apiKey);

        handleResponseStatus(response);

        String body = response.getContentAsString();
        logger.trace("Raw JSON response for stop_departures ({}, max={}): {}", globalStopId, maxDepartures, body);
        cache.put(cacheKey, new CachedResponse(body, System.currentTimeMillis()));
        return body;
    }

    public String fetchRouteDetails(String apiKey, String globalRouteId) throws IOException, InterruptedException {
        checkRateLimit();
        String url = API_BASE_URL + "route_details?global_route_id="
                + URLEncoder.encode(globalRouteId, StandardCharsets.UTF_8);
        ContentResponse response = sendGetRequest(url, apiKey);

        handleResponseStatus(response);
        String body = response.getContentAsString();
        logger.trace("Raw JSON response for route_details ({}): {}", globalRouteId, body);
        return body;
    }

    public String fetchTripDetails(String apiKey, String tripSearchKey) throws IOException, InterruptedException {
        checkRateLimit();
        String url = API_BASE_URL + "trip_details?trip_search_key="
                + URLEncoder.encode(tripSearchKey, StandardCharsets.UTF_8);
        ContentResponse response = sendGetRequest(url, apiKey);

        handleResponseStatus(response);
        String body = response.getContentAsString();
        logger.trace("Raw JSON response for trip_details ({}): {}", tripSearchKey, body);
        return body;
    }

    public String fetchNearbyStops(String apiKey, double lat, double lon) throws IOException, InterruptedException {
        checkRateLimit();
        String url = API_BASE_URL + "nearby_stops?lat=" + lat + "&lon=" + lon;
        ContentResponse response = sendGetRequest(url, apiKey);

        handleResponseStatus(response);
        String body = response.getContentAsString();
        logger.trace("Raw JSON response for nearby_stops (lat={}, lon={}): {}", lat, lon, body);
        return body;
    }

    public StopDeparturesResult getStopDepartures(String apiKey, String globalStopId, int maxDepartures)
            throws IOException, InterruptedException {
        String json = fetchStopDepartures(apiKey, globalStopId, maxDepartures);
        StopDeparturesResult result;
        try {
            result = gson.fromJson(json, StopDeparturesResult.class);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON for stop departures: " + e.getMessage(), e);
        }
        if (result == null) {
            throw new IOException("Parsed JSON for stop departures is null");
        }
        return result;
    }

    public RouteDetailsResult getRouteDetails(String apiKey, String routeId) throws IOException, InterruptedException {
        String json = fetchRouteDetails(apiKey, routeId);
        RouteDetailsResult result;
        try {
            result = gson.fromJson(json, RouteDetailsResult.class);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON for route details: " + e.getMessage(), e);
        }
        if (result == null) {
            throw new IOException("Parsed JSON for route details is null");
        }
        return result;
    }

    public TripDetailsResult getTripDetails(String apiKey, String tripSearchKey)
            throws IOException, InterruptedException {
        String json = fetchTripDetails(apiKey, tripSearchKey);
        TripDetailsResult result;
        try {
            result = gson.fromJson(json, TripDetailsResult.class);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON for trip details: " + e.getMessage(), e);
        }
        if (result == null) {
            throw new IOException("Parsed JSON for trip details is null");
        }
        return result;
    }

    private static class CachedResponse {
        final String payload;
        final long timestamp;

        CachedResponse(String payload, long timestamp) {
            this.payload = payload;
            this.timestamp = timestamp;
        }
    }
}
