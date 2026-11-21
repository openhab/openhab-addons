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
package org.openhab.binding.transit.internal.net;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.transit.internal.net.dto.EstimatePlanDurationResult;
import org.openhab.binding.transit.internal.net.dto.NetworkAlertsResult;
import org.openhab.binding.transit.internal.net.dto.PlanResult;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transit.internal.net.dto.StopsResult;
import org.openhab.binding.transit.internal.net.dto.TripDetailsResult;
import org.openhab.binding.transit.internal.net.dto.VehiclesResult;
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
            return httpClient.newRequest(url).method(HttpMethod.GET).headers(h -> h.put(API_KEY_HEADER, apiKey))
                    .timeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (TimeoutException e) {
            throw new IOException("Request to Transit API timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new IOException(
                    "Request to Transit API failed: " + (cause != null ? cause.getMessage() : e.getMessage()), e);
        }
    }

    /**
     * Sends a GET request (respecting an active rate limit backoff) and returns the response body.
     */
    private String get(String url, String apiKey, String endpoint) throws IOException, InterruptedException {
        checkRateLimit();
        ContentResponse response = sendGetRequest(url, apiKey);
        handleResponseStatus(response);
        String body = response.getContentAsString();
        logger.trace("Raw JSON response for {} ({}): {}", endpoint, url, body);
        return body;
    }

    private <T> T parse(String json, Class<T> type, String endpoint) throws IOException {
        T result;
        try {
            result = gson.fromJson(json, type);
        } catch (JsonParseException e) {
            throw new IOException("Invalid JSON for " + endpoint + ": " + e.getMessage(), e);
        }
        if (result == null) {
            throw new IOException("Parsed JSON for " + endpoint + " is null");
        }
        return result;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Requests the departures of one or more stops.
     *
     * @param globalStopIds one global stop ID or a comma-separated list of global stop IDs
     */
    public String fetchStopDepartures(String apiKey, String globalStopIds, int maxDepartures)
            throws IOException, InterruptedException {
        long now = System.currentTimeMillis();
        cleanupCache(now);

        // Cache key includes maxDepartures to avoid incorrect cache hits
        String cacheKey = globalStopIds + ":" + maxDepartures;
        CachedResponse cached = cache.get(cacheKey);
        if (cached != null && (now - cached.timestamp) < cacheTimeMs) {
            return cached.payload;
        }

        String url = API_BASE_URL + "stop_departures?global_stop_ids=" + encode(globalStopIds) + "&max_num_departures="
                + maxDepartures;
        String body = get(url, apiKey, "stop_departures");
        cache.put(cacheKey, new CachedResponse(body, System.currentTimeMillis()));
        return body;
    }

    public String fetchRouteDetails(String apiKey, String globalRouteId) throws IOException, InterruptedException {
        return get(API_BASE_URL + "route_details?global_route_id=" + encode(globalRouteId), apiKey, "route_details");
    }

    public String fetchTripDetails(String apiKey, String tripSearchKey) throws IOException, InterruptedException {
        return get(API_BASE_URL + "trip_details?trip_search_key=" + encode(tripSearchKey), apiKey, "trip_details");
    }

    public String fetchNearbyStops(String apiKey, double lat, double lon) throws IOException, InterruptedException {
        return get(API_BASE_URL + "nearby_stops?lat=" + lat + "&lon=" + lon, apiKey, "nearby_stops");
    }

    public String fetchSearchStops(String apiKey, double lat, double lon, String query)
            throws IOException, InterruptedException {
        return get(API_BASE_URL + "search_stops?lat=" + lat + "&lon=" + lon + "&query=" + encode(query), apiKey,
                "search_stops");
    }

    /**
     * Requests the active alerts of one or more networks.
     *
     * @param networkIds one network ID or a comma-separated list of network IDs (e.g. {@code VVS|Stuttgart})
     */
    public String fetchNetworkAlerts(String apiKey, String networkIds) throws IOException, InterruptedException {
        return get(API_BASE_URL + "alerts_for_networks?network_ids=" + encode(networkIds)
                + "&show_active_alerts_only=true", apiKey, "alerts_for_networks");
    }

    public String fetchPlan(String apiKey, TransitPlace from, TransitPlace to)
            throws IOException, InterruptedException {
        // should_update_realtime defaults to false, without it the plan only contains timetable data
        return get(
                API_BASE_URL + "plan?" + from.toQuery("from") + "&" + to.toQuery("to") + "&should_update_realtime=true",
                apiKey, "plan");
    }

    public String fetchEstimatePlanDuration(String apiKey, TransitPlace from, TransitPlace to)
            throws IOException, InterruptedException {
        return get(API_BASE_URL + "estimate_plan_duration?" + from.toQuery("from") + "&" + to.toQuery("to"), apiKey,
                "estimate_plan_duration");
    }

    /**
     * Requests the vehicle positions of a route.
     *
     * @param directionId {@code 0} or {@code 1} to filter by direction, {@code null} for both directions
     */
    public String fetchVehicles(String apiKey, String globalRouteId, @Nullable Integer directionId)
            throws IOException, InterruptedException {
        String url = API_VEHICLES_URL + "?global_route_id=" + encode(globalRouteId);
        if (directionId != null) {
            url += "&direction_id=" + directionId;
        }
        return get(url, apiKey, "vehicles");
    }

    public StopDeparturesResult getStopDepartures(String apiKey, String globalStopIds, int maxDepartures)
            throws IOException, InterruptedException {
        return parse(fetchStopDepartures(apiKey, globalStopIds, maxDepartures), StopDeparturesResult.class,
                "stop departures");
    }

    public RouteDetailsResult getRouteDetails(String apiKey, String routeId) throws IOException, InterruptedException {
        return parse(fetchRouteDetails(apiKey, routeId), RouteDetailsResult.class, "route details");
    }

    public TripDetailsResult getTripDetails(String apiKey, String tripSearchKey)
            throws IOException, InterruptedException {
        return parse(fetchTripDetails(apiKey, tripSearchKey), TripDetailsResult.class, "trip details");
    }

    public StopsResult getNearbyStops(String apiKey, double lat, double lon) throws IOException, InterruptedException {
        return parse(fetchNearbyStops(apiKey, lat, lon), StopsResult.class, "nearby stops");
    }

    public NetworkAlertsResult getNetworkAlerts(String apiKey, String networkIds)
            throws IOException, InterruptedException {
        return parse(fetchNetworkAlerts(apiKey, networkIds), NetworkAlertsResult.class, "network alerts");
    }

    public PlanResult getPlan(String apiKey, TransitPlace from, TransitPlace to)
            throws IOException, InterruptedException {
        return parse(fetchPlan(apiKey, from, to), PlanResult.class, "plan");
    }

    public EstimatePlanDurationResult getEstimatePlanDuration(String apiKey, TransitPlace from, TransitPlace to)
            throws IOException, InterruptedException {
        return parse(fetchEstimatePlanDuration(apiKey, from, to), EstimatePlanDurationResult.class,
                "estimate plan duration");
    }

    public VehiclesResult getVehicles(String apiKey, String globalRouteId, @Nullable Integer directionId)
            throws IOException, InterruptedException {
        return parse(fetchVehicles(apiKey, globalRouteId, directionId), VehiclesResult.class, "vehicles");
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
