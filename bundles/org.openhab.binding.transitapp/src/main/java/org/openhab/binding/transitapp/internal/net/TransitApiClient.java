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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class TransitApiClient {
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    private long rateLimitResetTime = 0;
    private static final long CACHE_TTL_MS = 30_000;

    @SuppressWarnings("null")
    public String fetchStopDepartures(String apiKey, String globalStopId) throws IOException, InterruptedException {
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
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("apiKey", apiKey).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            String retryAfter = "60";
            if (response.headers().firstValue("Retry-After").isPresent()) {
                String val = response.headers().firstValue("Retry-After").get();
                if (val != null) {
                    retryAfter = val;
                }
            }
            try {
                rateLimitResetTime = now + (Long.parseLong(retryAfter) * 1000);
            } catch (NumberFormatException e) {
                rateLimitResetTime = now + 60000;
            }
            throw new IOException(
                    "HTTP 429 Too Many Requests. Backoff until " + Instant.ofEpochMilli(rateLimitResetTime));
        }

        if (response.statusCode() != 200) {
            throw new IOException("Transit API returned HTTP error status: " + response.statusCode());
        }

        String body = response.body();
        cache.put(globalStopId, new CachedResponse(body, now));
        return body;
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
