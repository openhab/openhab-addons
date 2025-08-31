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

import okhttp3.Protocol;
import okhttp3.ResponseBody;

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

    /**
     * Gson instance for JSON parsing and serialization.
     */
    private final Gson gson = new Gson();

    /**
     * Cached connection status and last check time to avoid frequent checks on every write operation.
     */
    private volatile boolean lastConnectionStatus = false;
    private volatile long lastConnectionCheckTime = 0;

    /**
     * Default http client (we use okhttp3 for better performance)
     */
    private static final okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
            .protocols(List.of(Protocol.HTTP_1_1)) // VictoriaMetrics supports HTTP/1.1
            .retryOnConnectionFailure(true).connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS).build();

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
        long now = System.currentTimeMillis();
        if (now - lastConnectionCheckTime < CONNECTION_HEARTBEAT_INTERVAL) {
            return lastConnectionStatus;
        }
        synchronized (this) {
            // Double-check inside synchronized block
            if (now - lastConnectionCheckTime < CONNECTION_HEARTBEAT_INTERVAL) {
                return lastConnectionStatus;
            }
            try {
                okhttp3.Request request = requestBuilder("/api/v1/status/tsdb").get().build();
                try (okhttp3.Response response = httpClient.newCall(request).execute()) {
                    lastConnectionStatus = response.isSuccessful();
                    if (lastConnectionStatus) {
                        logger.trace("Connected to VictoriaMetrics at {}", configuration.getUrl());
                    } else {
                        logger.warn("Failed to connect to VictoriaMetrics, response code: {}", response.code());
                    }
                }
            } catch (IOException e) {
                lastConnectionStatus = false;
            } finally {
                lastConnectionCheckTime = System.currentTimeMillis();
            }
            return lastConnectionStatus;
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
            StringBuilder bodyBuilder = new StringBuilder();
            for (VictoriaMetricsPoint point : points) {
                bodyBuilder.append(point.toPrometheusFormat()).append('\n');
            }
            okhttp3.RequestBody body = okhttp3.RequestBody.create(bodyBuilder.toString(),
                    okhttp3.MediaType.parse("text/plain"));
            okhttp3.Request request = requestBuilder("/api/v1/import/prometheus").post(body).build();
            try (okhttp3.Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 204 || response.code() == 200) {
                    logger.trace("Wrote {} points to VictoriaMetrics", points.size());
                    return true;
                } else {
                    logger.warn("Failed to write points to VictoriaMetrics, response code: {}", response.code());
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("Failed to write points to VictoriaMetrics", e);
            return false;
        }
    }

    public List<VictoriaRow> query(FilterCriteria filter) {
        try {
            String itemName = filter.getItemName();
            if (itemName == null) {
                logger.warn("No item name specified for query");
                return List.of();
            }
            String metricSelector = getMetricSelector(itemName);
            String promql;
            String endpoint;
            String urlString;
            if (filter.getBeginDate() != null && filter.getEndDate() != null) {
                endpoint = "/api/v1/query_range";
                promql = metricSelector;
                ZonedDateTime startDate = filter.getBeginDate();
                ZonedDateTime endDate = filter.getEndDate();
                ZonedDateTime now = ZonedDateTime.now();
                // Default to last 24 hours if no dates provided
                long start = startDate != null ? startDate.toEpochSecond() : now.minusHours(1).toEpochSecond();
                long end = endDate != null ? endDate.toEpochSecond() : now.toEpochSecond();
                // We cannot exceed "maxPointsPerTimeseries", which defaults to 3000
                int step = Math.max(QUERY_DEFAULT_STEP, (int) ((end - start) / QUERY_MAX_POINTS));
                urlString = String.format("%s?query=%s&start=%d&end=%d&step=%d", endpoint,
                        encode(promql, StandardCharsets.UTF_8), start, end, step);
            } else {
                endpoint = "/api/v1/query";
                promql = metricSelector;
                urlString = String.format("%s?query=%s", endpoint, encode(promql, StandardCharsets.UTF_8));
            }
            okhttp3.Request request = requestBuilder(urlString).get().build();
            try (okhttp3.Response response = httpClient.newCall(request).execute()) {
                String responseJson = response.body().string();
                if (response.code() != 200) {
                    JsonObject json = gson.fromJson(responseJson, JsonObject.class);
                    String errorMessage = json != null ? json.get("error").getAsString() : "Unknown error";
                    throw new IOException(
                            "Query failed, code: " + response.code() + ", URL: " + urlString + ", r: " + errorMessage);
                }
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
                    JsonArray values = obj.has("values") ? obj.getAsJsonArray("values") : null;
                    JsonArray value = obj.has("value") ? obj.getAsJsonArray("value") : null;
                    if (values != null) {
                        for (JsonElement vEl : values) {
                            JsonArray vArr = vEl.getAsJsonArray();
                            long ts = vArr.get(0).getAsLong();
                            String valStr = vArr.get(1).getAsString();
                            rows.add(new VictoriaRow(entryName, valStr, Instant.ofEpochSecond(ts)));
                        }
                    } else if (value != null) {
                        long ts = value.get(0).getAsLong();
                        String valStr = value.get(1).getAsString();
                        rows.add(new VictoriaRow(entryName, valStr, Instant.ofEpochSecond(ts)));
                    }
                }
                if (filter.getOrdering() == FilterCriteria.Ordering.DESCENDING) {
                    rows.sort((r1, r2) -> r2.time.compareTo(r1.time));
                }
                int pageSize = filter.getPageSize();
                int pageNumber = filter.getPageNumber();
                int fromIndex = pageNumber * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, rows.size());
                if (fromIndex < rows.size()) {
                    return rows.subList(fromIndex, toIndex);
                } else {
                    return List.of();
                }
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
            String match = getMetricSelector(itemName);
            StringBuilder urlBuilder = new StringBuilder("/api/v1/admin/tsdb/delete_series?match[]=")
                    .append(encode(match, StandardCharsets.UTF_8));
            ZonedDateTime start = filter.getBeginDate();
            if (start != null)
                urlBuilder.append("&start=").append(start.toEpochSecond());
            ZonedDateTime end = filter.getEndDate();
            if (end != null)
                urlBuilder.append("&end=").append(end.toEpochSecond());
            okhttp3.RequestBody body = okhttp3.RequestBody.create(new byte[0]);
            okhttp3.Request request = requestBuilder(urlBuilder.toString()).post(body).build();
            try (okhttp3.Response response = httpClient.newCall(request).execute()) {
                if (response.code() != 204) {
                    logger.warn("VictoriaMetrics remove failed, response code: {}", response.code());
                    return false;
                }
                return true;
            }
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
        okhttp3.Request request = requestBuilder(url).get().build();
        try (okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("VictoriaMetrics query failed, response code: " + response.code());
            }
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        }
    }

    /**
     * Creates a connection to the VictoriaMetrics server with the specified URL and method.
     *
     * @param path the path to the API endpoint
     * @return an okhttp3.Request.Builder for the specified path with authentication headers
     */
    private okhttp3.Request.Builder requestBuilder(String path) {
        if (path.startsWith("/"))
            path = path.substring(1);
        // Ensure the base URL ends with a slash
        String baseUrl = configuration.getUrl();
        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
        // Construct the full URL
        String fullUrl = baseUrl + path;
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(fullUrl);
        String user = configuration.getUser();
        String token = configuration.getToken();
        if (!user.isEmpty()) {
            String credentials = okhttp3.Credentials.basic(user, configuration.getPassword());
            builder.header("Authorization", credentials);
        } else if (!token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        logger.trace("Request to VictoriaMetrics: {}", fullUrl);
        return builder;
    }

    /**
     * Converts an item name to a VictoriaMetrics metric name, applying any necessary transformations.
     *
     * @param itemName the item name to convert
     * @return the converted metric name
     */
    private String getMetricSelector(String itemName) {
        String name = metadataService.getMeasurementNameOrDefault(itemName);
        if (configuration.isCamelToSnakeCase()) {
            name = VictoriaMetricsCaseConvertUtils.camelToSnake(name);
        }
        return configuration.getMeasurementPrefix() + name;
    }

    // Row object for result mapping
    public record VictoriaRow(String itemName, Object value, Instant time) {
    }
}
