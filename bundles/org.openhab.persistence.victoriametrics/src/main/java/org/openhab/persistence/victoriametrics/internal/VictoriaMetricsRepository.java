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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.InputStreamResponseListener;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.types.util.UnitUtils;
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
 * @author Michael Weger - Use Jetty, query raw samples, join state components, read string states, abort requests on
 *         stop, convert values from the stored unit
 */
@NonNullByDefault
public class VictoriaMetricsRepository {
    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    private static final long ITEM_NAMES_RANGE_DAYS = 365;
    // the label APIs treat a start of 0 like a missing start and only return the labels of the current day
    private static final Instant EARLIEST_START = Instant.ofEpochMilli(1);

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsRepository.class);
    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsFilterCriteriaQueryCreator queryCreator;
    private final HttpClient httpClient;
    private final String baseUrl;
    private volatile boolean connected;
    private volatile boolean closed;
    private final Set<Request> runningRequests = ConcurrentHashMap.newKeySet();

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
        return connected && !closed;
    }

    /**
     * Connect to VictoriaMetrics server by checking its health endpoint
     *
     * @return <code>true</code> if successful, otherwise <code>false</code>
     */
    public boolean connect() {
        try {
            ContentResponse response = send(newRequest(PATH_HEALTH));
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
     * Abort the running requests and reject all further requests
     * <p>
     * Used when the service stops, so that a hanging request cannot write or re-queue points after the deactivation.
     * The shared {@link HttpClient} is not stopped, only the requests of this repository are aborted.
     */
    public void close() {
        closed = true;
        connected = false;
        runningRequests.forEach(request -> request.abort(new IOException("VictoriaMetrics persistence stopped")));
    }

    private ContentResponse send(Request request) throws InterruptedException, TimeoutException, ExecutionException {
        register(request);
        try {
            return request.send();
        } finally {
            runningRequests.remove(request);
        }
    }

    private void register(Request request) throws ExecutionException {
        // registered before checking closed, so that close() either sees the request or the request sees closed
        runningRequests.add(request);
        if (closed) {
            runningRequests.remove(request);
            throw new ExecutionException(new IOException("VictoriaMetrics persistence stopped"));
        }
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
            ContentResponse response = send(newRequest(PATH_IMPORT).method(HttpMethod.POST)
                    .body(new StringRequestContent("text/plain", body, StandardCharsets.UTF_8)));
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
     * Query raw samples
     *
     * @param filter the query filter
     * @param alias the alias the item is stored with, or <code>null</code>
     * @param itemUnit the unit to convert the values of series with a stored unit to, or <code>null</code> to read
     *            all values unchanged
     * @return Query results
     */
    public List<VictoriaMetricsRow> query(FilterCriteria filter, @Nullable String alias, @Nullable Unit<?> itemUnit) {
        try {
            List<VictoriaMetricsRow> rows = new ArrayList<>();
            List<VictoriaMetricsRow> result = List.of();
            for (VictoriaMetricsQuery query : queryCreator.createQueries(filter, alias, Instant.now())) {
                rows.addAll(export(query, itemUnit));
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

    private List<VictoriaMetricsRow> export(VictoriaMetricsQuery query, @Nullable Unit<?> itemUnit)
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
        boolean completed = false;
        try {
            register(request);
            request.send(listener);
            Response response = listener.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            try (InputStream inputStream = listener.getInputStream()) {
                if (response.getStatus() != HttpStatus.OK_200) {
                    throw new IOException("HTTP status " + response.getStatus() + ": "
                            + new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                }
                List<VictoriaMetricsRow> rows = parseExport(inputStream, itemUnit);
                completed = true;
                return rows;
            }
        } finally {
            if (!completed) {
                request.abort(new IOException("Export aborted"));
            }
            runningRequests.remove(request);
        }
    }

    /**
     * Parse the JSON lines returned by the export API, skipping non-finite values and merging the components of a
     * state into one row
     * <p>
     * The values of series with a unit label are converted from that unit to the item unit, so values stored before
     * the unit of the item was changed are read correctly.
     *
     * @param inputStream the response body
     * @param itemUnit the unit to convert the values of series with a stored unit to, or <code>null</code> to read
     *            all values unchanged
     * @return the samples of all exported series
     * @throws IOException if reading fails
     */
    List<VictoriaMetricsRow> parseExport(InputStream inputStream, @Nullable Unit<?> itemUnit) throws IOException {
        List<VictoriaMetricsRow> rows = new ArrayList<>();
        Set<String> unconvertibleUnits = new LinkedHashSet<>();
        Map<ComponentKey, Map<String, BigDecimal>> componentRows = new LinkedHashMap<>();
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
            JsonElement componentElement = metric.get(TAG_COMPONENT_NAME);
            JsonElement stateElement = metric.get(TAG_STATE_NAME);
            JsonElement unitElement = metric.get(TAG_UNIT_NAME);
            Unit<?> storedUnit = itemUnit != null && unitElement != null
                    ? getStoredUnit(unitElement.getAsString(), itemUnit, unconvertibleUnits)
                    : null;
            for (int i = 0; i < values.size(); i++) {
                BigDecimal value = toBigDecimal(values.get(i));
                if (value == null) {
                    continue;
                }
                Instant time = Instant.ofEpochMilli(timestamps.get(i).getAsLong());
                if (stateElement != null) {
                    rows.add(new VictoriaMetricsRow(time, itemName, stateElement.getAsString()));
                } else if (componentElement == null) {
                    rows.add(new VictoriaMetricsRow(time, itemName,
                            storedUnit != null && itemUnit != null ? convert(value, storedUnit, itemUnit) : value));
                } else {
                    componentRows.computeIfAbsent(new ComponentKey(time, itemName), k -> new LinkedHashMap<>())
                            .put(componentElement.getAsString(), value);
                }
            }
        }
        // the components of a state are stored as separate series with the same timestamp
        componentRows.forEach((key, components) -> rows
                .add(new VictoriaMetricsRow(key.time(), key.itemName(), Collections.unmodifiableMap(components))));
        return rows;
    }

    private record ComponentKey(Instant time, String itemName) {
    }

    /**
     * Get the unit stored with a series, if its values have to be converted to the item unit
     * <p>
     * Values in a unit that cannot be parsed or converted are read in the item unit, as without a stored unit.
     *
     * @param unitLabel the value of the unit label
     * @param itemUnit the unit of the item
     * @param unconvertibleUnits the stored units already logged as not convertible
     * @return the stored unit, or <code>null</code> if the values are read in the item unit
     */
    private @Nullable Unit<?> getStoredUnit(String unitLabel, Unit<?> itemUnit, Set<String> unconvertibleUnits) {
        Unit<?> storedUnit = UnitUtils.parseUnit(unitLabel);
        if (itemUnit.equals(storedUnit)) {
            return null;
        }
        if (storedUnit == null || new QuantityType<>(BigDecimal.ONE, storedUnit).toInvertibleUnit(itemUnit) == null) {
            if (unconvertibleUnits.add(unitLabel)) {
                logger.debug("Reading values stored in unit '{}' in the item unit {}, the units are not convertible",
                        unitLabel, itemUnit);
            }
            return null;
        }
        return storedUnit;
    }

    /**
     * Convert a value to the item unit, rounded to the 64-bit floating point precision of the stored samples like the
     * state filter, so that converted values can be compared with it
     */
    private BigDecimal convert(BigDecimal value, Unit<?> storedUnit, Unit<?> itemUnit) {
        QuantityType<?> converted = new QuantityType<>(value, storedUnit).toInvertibleUnit(itemUnit);
        return converted != null ? BigDecimal.valueOf(converted.doubleValue()) : value;
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

        Set<String> result = getLabelValues(TAG_ITEM_NAME, selector, start);
        return result != null ? result : Set.of();
    }

    /**
     * Return the string states stored for an item
     *
     * @param metricName the metric name of the item
     * @param itemName the item name or alias
     * @return the stored states, or <code>null</code> if they could not be read
     */
    public @Nullable Set<String> getStringStates(String metricName, String itemName) {
        String selector = "{" + LABEL_METRIC_NAME + "=\"" + metricName + "\"," + TAG_ITEM_NAME + "=\""
                + VictoriaMetricsPoint.escapeLabelValue(itemName) + "\"}";
        return getLabelValues(TAG_STATE_NAME, selector, EARLIEST_START);
    }

    private @Nullable Set<String> getLabelValues(String labelName, String selector, Instant start) {
        try {
            // the end defaults to the current time, which would exclude future states
            ContentResponse response = send(newRequest(PATH_LABEL_VALUES.formatted(labelName))
                    .param("match[]", selector).param("start", formatTimestamp(start))
                    .param("end", formatTimestamp(VictoriaMetricsFilterCriteriaQueryCreator.OPEN_END)));
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.warn("Failed to query values of label '{}': HTTP status {}: {}", labelName, response.getStatus(),
                        response.getContentAsString());
                return null;
            }
            JsonArray values = JsonParser.parseString(response.getContentAsString()).getAsJsonObject()
                    .getAsJsonArray("data");
            if (values == null) {
                throw new JsonParseException("Unexpected query result: " + response.getContentAsString());
            }
            Set<String> result = new LinkedHashSet<>();
            for (JsonElement value : values) {
                result.add(value.getAsString());
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException | JsonParseException | IllegalStateException e) {
            // IllegalStateException is thrown for unexpected JSON types
            logger.warn("Failed to query values of label '{}': {}", labelName, e.getMessage());
        }
        return null;
    }

    private Request newRequest(String path) {
        Request request = httpClient.newRequest(baseUrl + path).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        String user = configuration.getUser();
        String token = configuration.getToken();
        String authorization;
        if (!user.isEmpty()) {
            String credentials = user + ":" + configuration.getPassword();
            authorization = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        } else if (!token.isEmpty()) {
            authorization = "Bearer " + token;
        } else {
            return request;
        }
        return request.headers(headers -> headers.put(HttpHeader.AUTHORIZATION, authorization));
    }

    /**
     * Format a timestamp as accepted by the VictoriaMetrics API: Unix seconds with millisecond precision
     */
    static String formatTimestamp(Instant instant) {
        return BigDecimal.valueOf(instant.toEpochMilli(), 3).toPlainString();
    }

    /**
     * A stored state
     *
     * @param value a {@link BigDecimal}, a {@link Map} of component names to {@link BigDecimal} values, or a
     *            {@link String} for string states
     */
    public record VictoriaMetricsRow(Instant time, String itemName, Object value) {
    }
}
