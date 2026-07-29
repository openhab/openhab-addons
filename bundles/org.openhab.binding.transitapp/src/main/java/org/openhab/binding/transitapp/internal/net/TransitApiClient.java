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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.transitapp.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transitapp.internal.net.dto.TripDetailsResult;

import com.google.gson.Gson;

@NonNullByDefault
public class TransitApiClient {
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public TransitApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private volatile long rateLimitResetTime = 0;
    private static final long CACHE_TTL_MS = 30_000;

    @SuppressWarnings("null")
    public String fetchStopDepartures(String apiKey, String globalStopId) throws Exception {
        long now = System.currentTimeMillis();
        if (now < rateLimitResetTime) {
            throw new IOException("Rate limit active until " + Instant.ofEpochMilli(rateLimitResetTime));
        }

        CachedResponse cached = cache.get(globalStopId);
        if (cached != null && (now - cached.timestamp) < CACHE_TTL_MS) {
            return cached.payload;
        }

        String url = "https://external.transitapp.com/v4/public/stop_departures?global_stop_id="
                + URLEncoder.encode(globalStopId, StandardCharsets.UTF_8);
        ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("apiKey", apiKey)
                .timeout(10, TimeUnit.SECONDS).send();

        if (response.getStatus() == 429) {
            String retryAfter = "60";
            if (response.getHeaders().get("Retry-After") != null) {
                retryAfter = response.getHeaders().get("Retry-After");
            }
            try {
                rateLimitResetTime = now + (Long.parseLong(retryAfter) * 1000);
            } catch (NumberFormatException e) {
                rateLimitResetTime = now + 60000;
            }
            throw new IOException(
                    "HTTP 429 Too Many Requests. Backoff until " + Instant.ofEpochMilli(rateLimitResetTime));
        }

        if (response.getStatus() != 200) {
            throw new IOException("Transit API returned HTTP error status: " + response.getStatus());
        }

        String body = response.getContentAsString();
        cache.put(globalStopId, new CachedResponse(body, now));
        return body;
    }

    public String fetchRouteDetails(String apiKey, String globalRouteId) throws Exception {
        String url = "https://external.transitapp.com/v4/public/route_details?global_route_id="
                + URLEncoder.encode(globalRouteId, StandardCharsets.UTF_8);
        ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("apiKey", apiKey)
                .timeout(10, TimeUnit.SECONDS).send();

        if (response.getStatus() != 200) {
            throw new IOException("Transit API returned HTTP error status: " + response.getStatus());
        }
        return response.getContentAsString();
    }

    public String fetchTripDetails(String apiKey, String tripId) throws Exception {
        String url = "https://external.transitapp.com/v4/public/trip_details?trip_id="
                + URLEncoder.encode(tripId, StandardCharsets.UTF_8);
        ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("apiKey", apiKey)
                .timeout(10, TimeUnit.SECONDS).send();

        if (response.getStatus() != 200) {
            throw new IOException("Transit API returned HTTP error status: " + response.getStatus());
        }
        return response.getContentAsString();
    }

    public String fetchNearbyStops(String apiKey, double lat, double lon) throws Exception {
        String url = "https://external.transitapp.com/v4/public/nearby_stops?lat=" + lat + "&lon=" + lon;
        ContentResponse response = httpClient.newRequest(url).method(HttpMethod.GET).header("apiKey", apiKey)
                .timeout(10, TimeUnit.SECONDS).send();

        if (response.getStatus() != 200) {
            throw new IOException("Transit API returned HTTP error status: " + response.getStatus());
        }
        return response.getContentAsString();
    }

    public StopDeparturesResult getStopDepartures(String apiKey, String globalStopId) throws Exception {
        String json = fetchStopDepartures(apiKey, globalStopId);
        StopDeparturesResult result = gson.fromJson(json, StopDeparturesResult.class);
        if (result == null) {
            throw new IOException("Parsed JSON for stop departures is null");
        }
        return result;
    }

    public RouteDetailsResult getRouteDetails(String apiKey, String routeId) throws Exception {
        String json = fetchRouteDetails(apiKey, routeId);
        RouteDetailsResult result = gson.fromJson(json, RouteDetailsResult.class);
        if (result == null) {
            throw new IOException("Parsed JSON for route details is null");
        }
        return result;
    }

    public TripDetailsResult getTripDetails(String apiKey, String tripId) throws Exception {
        String json = fetchTripDetails(apiKey, tripId);
        TripDetailsResult result = gson.fromJson(json, TripDetailsResult.class);
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
