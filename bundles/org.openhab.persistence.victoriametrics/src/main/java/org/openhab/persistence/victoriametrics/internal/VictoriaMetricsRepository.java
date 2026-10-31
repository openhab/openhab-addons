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
package org.openhab.persistence.victoriametrics.internal;

import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsFilterCriteriaQueryCreator.VictoriaMetricsQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Manages VictoriaMetrics server interaction using the HTTP API.
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Use Jetty, query raw samples, restrict remove to whole series
 */
@NonNullByDefault
public class VictoriaMetricsRepository {
    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    private static final long ITEM_NAMES_RANGE_DAYS = 365;

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsRepository.class);
    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsFilterCriteriaQueryCreator queryCreator;
    private final HttpClient httpClient;
    private final String baseUrl;
    private volatile boolean connected;

    public VictoriaMetricsRepository(VictoriaMetricsConfiguration configuration,
            VictoriaMetricsMetricNameResolver metricNameResolver, HttpClient httpClient) {
        this.configuration = configuration;
        this.queryCreator = new VictoriaMetricsFilterCriteriaQueryCreator(metricNameResolver);
        this.httpClient = httpClient;
        String url = configuration.getUrl();
        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * Returns if the last connection check or write was successful
     *
     * @return True if it's connected, otherwise false
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Connect to VictoriaMetrics server by checking its health endpoint
     *
     * @return <code>true</code> if successful, otherwise <code>false</code>
     */
    public boolean connect() {
        try {
            ContentResponse response = newRequest(PATH_HEALTH).send();
            connected = response.getStatus() == HttpStatus.OK_200;
            if (connected) {
                logger.debug("Successfully connected to VictoriaMetrics at {}", baseUrl);
            } else {
                logger.debug("VictoriaMetrics health check failed with HTTP status {}", response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connected = false;
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("VictoriaMetrics health check failed: {}", e.getMessage());
            connected = false;
        }
        return connected;
    }

    /**
     * Disconnect from VictoriaMetrics server
     */
    public void disconnect() {
        connected = false;
    }

    /**
     * Write points to database
     *
     * @param points {@link List<VictoriaMetricsPoint>} to write
     * @return <code>true</code> if points have been written or were rejected as invalid by the server,
     *         <code>false</code> if writing should be retried
     */
    public boolean write(List<VictoriaMetricsPoint> points) {
        if (points.isEmpty()) {
            return true;
        }
        String body = points.stream().map(VictoriaMetricsPoint::toPrometheusFormat)
                .collect(Collectors.joining("\n", "", "\n"));
        try {
            ContentResponse response = newRequest(PATH_IMPORT).method(HttpMethod.POST)
                    .content(new StringContentProvider("text/plain", body, StandardCharsets.UTF_8)).send();
            int status = response.getStatus();
            if (HttpStatus.isSuccess(status)) {
                return true;
            } else if (status == HttpStatus.BAD_REQUEST_400) {
                // retrying would fail again and block all subsequent points
                logger.warn("VictoriaMetrics rejected {} points: {}", points.size(), response.getContentAsString());
                return true;
            }
            logger.debug("Writing to database failed with HTTP status {}", status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Writing to database failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Delete all samples of an item
     * <p>
     * The VictoriaMetrics delete API can only delete complete series, so filters with a time range or state are
     * rejected instead of deleting more data than requested.
     *
     * @param filter the query filter
     * @return <code>true</code> if query executed successfully, <code>false</code> otherwise
     */
    public boolean remove(FilterCriteria filter) {
        String itemName = filter.getItemName();
        if (itemName == null) {
            return false;
        }
        if (filter.getBeginDate() != null || filter.getEndDate() != null || filter.getState() != null) {
            logger.warn(
                    "VictoriaMetrics can only delete all data of an item, removing by time range or state is not supported: {}",
                    filter);
            return false;
        }
        try {
            ContentResponse response = newRequest(PATH_DELETE_SERIES).method(HttpMethod.POST)
                    .param("match[]", queryCreator.createSeriesSelector(itemName)).send();
            if (HttpStatus.isSuccess(response.getStatus())) {
                return true;
            }
            logger.warn("Deleting from database failed with HTTP status {}: {}", response.getStatus(),
                    response.getContentAsString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Deleting from database failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Query raw samples
     *
     * @param filter the query filter
     * @param alias the alias the item is stored with, or <code>null</code>
     * @return Query results
     */
    public List<VictoriaMetricsRow> query(FilterCriteria filter, @Nullable String alias) {
        try {
            List<VictoriaMetricsRow> rows = new ArrayList<>();
            List<VictoriaMetricsRow> result = List.of();
            for (VictoriaMetricsQuery query : queryCreator.createQueries(filter, alias, Instant.now())) {
                rows.addAll(export(query));
                result = queryCreator.applyCriteria(rows.stream(), filter);
                if (queryCreator.isComplete(result, filter)) {
                    break;
                }
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException | IOException | JsonParseException | IllegalStateException
                | IllegalArgumentException e) {
            // IllegalStateException is thrown for unexpected JSON types, IllegalArgumentException for a non-numeric
            // filter state
            logger.warn("Failed to execute query '{}': {}", filter, e.getMessage());
        }
        return List.of();
    }

    private List<VictoriaMetricsRow> export(VictoriaMetricsQuery query)
            throws InterruptedException, TimeoutException, ExecutionException, IOException {
        logger.trace("Query {}", query);
        Request request = newRequest(PATH_EXPORT).param("match[]", query.selector()).param("end",
                formatTimestamp(query.end()));
        Instant start = query.start();
        if (start != null) {
            request.param("start", formatTimestamp(start));
        }

        // the export can be large, so stream it instead of buffering the complete response
        InputStreamResponseListener listener = new InputStreamResponseListener();
        request.send(listener);
        boolean completed = false;
        try {
            Response response = listener.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            try (InputStream inputStream = listener.getInputStream()) {
                if (response.getStatus() != HttpStatus.OK_200) {
                    throw new IOException("HTTP status " + response.getStatus() + ": "
                            + new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                }
                List<VictoriaMetricsRow> rows = parseExport(inputStream);
                completed = true;
                return rows;
            }
        } finally {
            if (!completed) {
                request.abort(new IOException("Export aborted"));
            }
        }
    }

    /**
     * Parse the JSON lines returned by the export API, skipping non-finite values
     *
     * @param inputStream the response body
     * @return the samples of all exported series
     * @throws IOException if reading fails
     */
    List<VictoriaMetricsRow> parseExport(InputStream inputStream) throws IOException {
        List<VictoriaMetricsRow> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            JsonObject series = JsonParser.parseString(line).getAsJsonObject();
            JsonObject metric = series.getAsJsonObject("metric");
            JsonArray values = series.getAsJsonArray("values");
            JsonArray timestamps = series.getAsJsonArray("timestamps");
            if (metric == null || values == null || timestamps == null || values.size() != timestamps.size()) {
                throw new JsonParseException("Unexpected export format: " + line);
            }
            JsonElement itemElement = metric.get(TAG_ITEM_NAME);
            if (itemElement == null) {
                itemElement = metric.get(LABEL_METRIC_NAME);
            }
            String itemName = itemElement != null ? itemElement.getAsString() : "";
            for (int i = 0; i < values.size(); i++) {
                BigDecimal value = toBigDecimal(values.get(i));
                if (value != null) {
                    rows.add(new VictoriaMetricsRow(Instant.ofEpochMilli(timestamps.get(i).getAsLong()), itemName,
                            value));
                }
            }
        }
        return rows;
    }

    private @Nullable BigDecimal toBigDecimal(JsonElement element) {
        if (element.isJsonPrimitive()) {
            try {
                return element.getAsBigDecimal();
            } catch (NumberFormatException e) {
                // NaN or Inf
            }
        }
        return null;
    }

    /**
     * Return the names of all items with samples during the last year
     * <p>
     * Unlike InfluxDB, the number of stored points is not returned: counting the samples of all series reads all
     * samples of the time range and exceeds the default query duration limit on larger installations, while the names
     * are read from the index only.
     *
     * @return the item names (or aliases)
     */
    public Set<String> getStoredItemNames() {
        // prefix and suffix only contain [a-zA-Z0-9_:], so they need no regex escaping
        String prefix = configuration.getMetricPrefix();
        String suffix = configuration.getMetricSuffix();
        String selector = prefix.isEmpty() && suffix.isEmpty() ? "{" + TAG_ITEM_NAME + "!=\"\"}"
                : "{" + LABEL_METRIC_NAME + "=~\"" + prefix + ".*" + suffix + "\"," + TAG_ITEM_NAME + "!=\"\"}";
        Instant start = Instant.now().minus(ITEM_NAMES_RANGE_DAYS, ChronoUnit.DAYS);

        Set<String> result = new LinkedHashSet<>();
        try {
            ContentResponse response = newRequest(PATH_LABEL_VALUES.formatted(TAG_ITEM_NAME)).param("match[]", selector)
                    .param("start", formatTimestamp(start)).send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.warn("Failed to query stored items: HTTP status {}: {}", response.getStatus(),
                        response.getContentAsString());
                return result;
            }
            JsonArray names = JsonParser.parseString(response.getContentAsString()).getAsJsonObject()
                    .getAsJsonArray("data");
            if (names == null) {
                throw new JsonParseException("Unexpected query result: " + response.getContentAsString());
            }
            for (JsonElement name : names) {
                result.add(name.getAsString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException | JsonParseException | IllegalStateException e) {
            // IllegalStateException is thrown for unexpected JSON types
            logger.warn("Failed to query stored items: {}", e.getMessage());
        }
        return result;
    }

    private Request newRequest(String path) {
        Request request = httpClient.newRequest(baseUrl + path).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        String user = configuration.getUser();
        String token = configuration.getToken();
        if (!user.isEmpty()) {
            String credentials = user + ":" + configuration.getPassword();
            request.header(HttpHeader.AUTHORIZATION,
                    "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        } else if (!token.isEmpty()) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }

    /**
     * Format a timestamp as accepted by the VictoriaMetrics API: Unix seconds with millisecond precision
     */
    static String formatTimestamp(Instant instant) {
        return BigDecimal.valueOf(instant.toEpochMilli(), 3).toPlainString();
    }

    public record VictoriaMetricsRow(Instant time, String itemName, BigDecimal value) {
    }
}
