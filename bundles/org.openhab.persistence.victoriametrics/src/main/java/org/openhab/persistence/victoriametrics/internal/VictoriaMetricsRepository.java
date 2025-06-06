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
package org.openhab.persistence.victoriametrics.internal;

import static java.net.URLEncoder.*;
import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Manages VictoriaMetrics server interaction maintaining client connection.
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@NonNullByDefault
public class VictoriaMetricsRepository {
    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsRepository.class);
    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsMetadataService metadataService;
    private final Gson gson = new Gson();

    /**
     * Creates a new instance of VictoriaMetricsRepository.
     *
     * @param configuration the configuration for connecting to VictoriaMetrics
     * @param metadataService the service for managing metadata
     */
    public VictoriaMetricsRepository(VictoriaMetricsConfiguration configuration,
            VictoriaMetricsMetadataService metadataService) {
        this.configuration = configuration;
        this.metadataService = metadataService;
    }

    /**
     * Checks if the repository is connected to the VictoriaMetrics server.
     * This could be improved as its called on every write operation and its not really needed.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            HttpURLConnection conn = getConnection("/api/v1/status/tsdb", "GET");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            if (responseCode == 200) {
                logger.trace("Connected to VictoriaMetrics at {}", configuration.getUrl());
                return true;
            } else {
                logger.warn("Failed to connect to VictoriaMetrics, response code: {}", responseCode);
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public boolean connect() {
        return isConnected();
    }

    public void disconnect() {
    }

    public boolean write(List<VictoriaMetricsPoint> points) {
        if (points.isEmpty()) {
            return true;
        }
        try {
            HttpURLConnection conn = getConnection("/api/v1/import/prometheus", "POST");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                for (VictoriaMetricsPoint point : points) {
                    os.write(point.toPrometheusFormat().getBytes());
                    os.write('\n');
                }
            }
            int code = conn.getResponseCode();
            conn.disconnect();
            if (code == 204)
                logger.trace("Wrote {} points to VictoriaMetrics", points.size());
            else
                logger.warn("Failed to write points to VictoriaMetrics, response code: {}", code);
            return code == 204;
        } catch (IOException e) {
            logger.error("Failed to write points to VictoriaMetrics", e);
            return false;
        }
    }

    public List<VictoriaRow> query(FilterCriteria filter) {
        try {
            // Build PromQL query for item, filtered by openhab tag and optional time range
            String itemName = filter.getItemName();
            if (itemName == null) {
                logger.warn("No item name specified for query");
                return List.of();
            }
            String name = metadataService.getMeasurementNameOrDefault(itemName);
            String measurementName = configuration.isReplaceUnderscore() ? name.replace('_', '.') : name;
            String metricSelector = measurementName + "{" + FIELD_SOURCE_NAME + "=\"" + configuration.getSourceName()
                    + "\"}";
            String promql;
            String endpoint;
            String urlString;
            if (filter.getBeginDate() != null && filter.getEndDate() != null) {
                // Use range query
                endpoint = "/api/v1/query_range";
                promql = metricSelector;
                ZonedDateTime startDate = filter.getBeginDate();
                ZonedDateTime endDate = filter.getEndDate();
                ZonedDateTime now = ZonedDateTime.now();
                // Default to last 24 hours if no dates provided
                long start = startDate != null ? startDate.toEpochSecond() : now.minusDays(1).toEpochSecond();
                long end = endDate != null ? endDate.toEpochSecond() : now.toEpochSecond();
                // We cannot exceed "maxPointsPerTimeseries", which defaults to 3000
                int step = Math.max(QUERY_DEFAULT_STEP, (int) ((end - start) / QUERY_MAX_POINTS));
                urlString = String.format("%s?query=%s&start=%d&end=%d&step=%d", endpoint,
                        encode(promql, StandardCharsets.UTF_8), start, end, step);
            } else {
                // Instant query
                endpoint = "/api/v1/query";
                promql = metricSelector;
                urlString = String.format("%s?query=%s", endpoint, encode(promql, StandardCharsets.UTF_8));
            }
            // Prepare HTTP request
            HttpURLConnection conn = getConnection(urlString, "GET");
            int code = conn.getResponseCode();
            if (code != 200) {
                String responseJson;
                try (java.io.InputStream is = conn.getInputStream()) {
                    responseJson = new String(is.readAllBytes());
                }
                conn.disconnect();
                JsonObject json = gson.fromJson(responseJson, JsonObject.class);
                String errorMessage = json != null ? json.get("error").getAsString() : "Unknown error";
                throw new IOException("Query failed, code: " + code + ", URL: " + urlString + ", r: " + errorMessage);
            }
            String responseJson;
            try (java.io.InputStream is = conn.getInputStream()) {
                responseJson = new String(is.readAllBytes());
            }
            conn.disconnect();
            // Parse JSON response with Gson
            JsonObject json = gson.fromJson(responseJson, JsonObject.class);
            if (json == null || !"success".equals(json.get("status").getAsString())) {
                logger.warn("VictoriaMetrics query failed: {}", responseJson);
                return List.of();
            }
            JsonArray data = json.getAsJsonObject("data").getAsJsonArray("result");
            List<VictoriaRow> rows = new java.util.ArrayList<>();
            for (JsonElement el : data) {
                JsonObject obj = el.getAsJsonObject();
                String entryName = obj.getAsJsonObject("metric").get("__name__").getAsString();
                JsonArray values = obj.has("values") ? obj.getAsJsonArray("values") : null; // For range query
                JsonArray value = obj.has("value") ? obj.getAsJsonArray("value") : null; // For instant query
                if (values != null) {
                    // Range query: each "values" entry is [timestamp, value]
                    for (JsonElement vEl : values) {
                        JsonArray vArr = vEl.getAsJsonArray();
                        long ts = vArr.get(0).getAsLong();
                        String valStr = vArr.get(1).getAsString();
                        rows.add(new VictoriaRow(entryName, valStr, Instant.ofEpochSecond(ts)));
                    }
                } else if (value != null) {
                    // Instant query
                    long ts = value.get(0).getAsLong();
                    String valStr = value.get(1).getAsString();
                    rows.add(new VictoriaRow(entryName, valStr, Instant.ofEpochSecond(ts)));
                }
            }
            // Invert if specified
            if (filter.getOrdering() == FilterCriteria.Ordering.DESCENDING) {
                rows.sort((r1, r2) -> r2.time.compareTo(r1.time));
            }
            // Apply pagination if specified
            int pageSize = filter.getPageSize();
            int pageNumber = filter.getPageNumber();
            int fromIndex = pageNumber * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, rows.size());
            if (fromIndex < rows.size()) {
                return rows.subList(fromIndex, toIndex);
            } else {
                return List.of(); // or whatever empty result
            }
        } catch (Exception e) {
            logger.error("Failed to query VictoriaMetrics", e);
            return List.of();
        }
    }

    public boolean remove(FilterCriteria filter) {
        try {
            String itemName = filter.getItemName();
            if (itemName == null) {
                logger.warn("No item name specified for remove");
                return false;
            }
            String name = metadataService.getMeasurementNameOrDefault(itemName);
            String match = name + "{" + FIELD_SOURCE_NAME + "=\"" + configuration.getSourceName() + "\"}";
            String urlString = "/api/v1/admin/tsdb/delete_series?match[]=" + encode(match, StandardCharsets.UTF_8);
            ZonedDateTime start = filter.getBeginDate();
            if (start != null)
                urlString += "&start=" + (start.toEpochSecond());
            ZonedDateTime end = filter.getEndDate();
            if (end != null)
                urlString += "&end=" + (end.toEpochSecond());
            HttpURLConnection conn = getConnection(urlString, "POST");
            int code = conn.getResponseCode();
            conn.disconnect();
            if (code != 204) {
                logger.warn("VictoriaMetrics remove failed, response code: {}", code);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to remove data from VictoriaMetrics", e);
            return false;
        }
    }

    /**
     * Returns a map of all stored item names with their count of stored points.
     *
     * @return Map with {@code <ItemName, ItemCount>} entries
     */
    public Map<String, Integer> getStoredItemsCount() {
        try {
            String promql = "count({source=\"openhab\"}) by (__name__)";
            String responseJson = promQLQuery(promql);
            Map<String, Integer> result = new java.util.LinkedHashMap<>();
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseJson, JsonObject.class);
            // Check if query succeeded
            if (json == null || !"success".equals(json.get("status").getAsString())) {
                logger.warn("VictoriaMetrics query for stored items failed: {}", responseJson);
                return result;
            }
            JsonArray data = json.getAsJsonObject("data").getAsJsonArray("result");
            for (JsonElement el : data) {
                JsonObject obj = el.getAsJsonObject();
                String name = obj.getAsJsonObject("metric").get("__name__").getAsString();
                // Value array: [ <timestamp>, "<count as string>" ]
                int count = Integer.parseInt(obj.getAsJsonArray("value").get(1).getAsString());
                result.put(name, count);
            }
            return result;
        } catch (Exception e) {
            logger.error("Failed to get stored items count from VictoriaMetrics", e);
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * Executes a PromQL query against the VictoriaMetrics server.
     *
     * @param promql the PromQL query string
     * @return the response from the server as a JSON string
     * @throws IOException if an error occurs during the request
     */
    private String promQLQuery(String promql) throws IOException {
        String url = "/api/v1/query?query=" + encode(promql, StandardCharsets.UTF_8);
        HttpURLConnection conn = getConnection(url, "GET");
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("VictoriaMetrics query failed, response code: " + code);
        }
        try (java.io.InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }

    /**
     * Creates a connection to the VictoriaMetrics server with the specified URL and method.
     *
     * @param path the path to the API endpoint
     * @param method the HTTP method to use (GET, POST, etc.)
     * @return the HttpURLConnection object
     * @throws IOException if an error occurs while opening the connection
     */
    private HttpURLConnection getConnection(String path, String method) throws IOException {
        if (path.startsWith("/"))
            path = path.substring(1);
        URL url = URI.create(configuration.getUrl() + path).toURL();
        logger.trace("Connecting at {}", url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        // Check if the config has an user
        String username = configuration.getUser();
        if (!username.isEmpty()) {
            String auth = username + ":" + configuration.getPassword();
            String encoded = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoded);
        }
        // Check if config has a token (enterprise only)
        String token = configuration.getToken();
        if (!token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        return conn;
    }

    // Row object for result mapping
    public record VictoriaRow(String itemName, Object value, Instant time) {
    }
}
