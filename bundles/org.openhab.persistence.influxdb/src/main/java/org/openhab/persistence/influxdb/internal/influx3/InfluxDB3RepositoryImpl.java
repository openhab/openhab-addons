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
package org.openhab.persistence.influxdb.internal.influx3;

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Implementation of {@link InfluxDBRepository} for InfluxDB 3.0
 *
 * InfluxDB 3 has no OSGi/JPMS-friendly official Java client (it wraps Apache Arrow Flight, gRPC and Netty native
 * libraries), so this talks to the InfluxDB 3 HTTP API directly: line protocol writes via {@code /api/v3/write_lp}
 * and InfluxQL queries via {@code /api/v3/query_influxql}.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class InfluxDB3RepositoryImpl implements InfluxDBRepository {
    private static final MediaType LINE_PROTOCOL_MEDIA_TYPE = Objects
            .requireNonNull(MediaType.parse("text/plain; charset=utf-8"));

    private final Logger logger = LoggerFactory.getLogger(InfluxDB3RepositoryImpl.class);
    private final InfluxDBConfiguration configuration;
    private final FilterCriteriaQueryCreator queryCreator;

    private @Nullable OkHttpClient client;

    public InfluxDB3RepositoryImpl(InfluxDBConfiguration configuration,
            InfluxDBMetadataService influxDBMetadataService) {
        this.configuration = configuration;
        this.queryCreator = new InfluxDB3FilterCriteriaQueryCreatorImpl(configuration, influxDBMetadataService);
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public boolean connect() {
        this.client = new OkHttpClient();
        return checkConnectionStatus();
    }

    @Override
    public void disconnect() {
        final OkHttpClient currentClient = client;
        if (currentClient != null) {
            currentClient.dispatcher().executorService().shutdown();
            currentClient.connectionPool().evictAll();
        }
        this.client = null;
    }

    @Override
    public boolean checkConnectionStatus() {
        final OkHttpClient currentClient = client;
        if (currentClient == null) {
            logger.warn("checkConnection: database is not connected");
            return false;
        }
        try {
            HttpUrl url = HttpUrl.get(configuration.getUrl()).newBuilder().addPathSegment("ping").build();
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = currentClient.newCall(request).execute()) {
                boolean isUp = response.isSuccessful();
                if (isUp) {
                    logger.debug("database status is OK");
                } else {
                    logger.warn("database not ready, HTTP status {}", response.code());
                }
                return isUp;
            }
        } catch (IOException | IllegalArgumentException e) {
            logger.warn("database error: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean write(List<InfluxPoint> influxPoints) {
        final OkHttpClient currentClient = this.client;
        if (currentClient == null) {
            return false;
        }

        String lineProtocolBody = influxPoints.stream().map(this::convertPointToLineProtocol)
                .filter(Optional::isPresent).map(Optional::get).reduce((a, b) -> a + "\n" + b).orElse("");
        if (lineProtocolBody.isEmpty()) {
            return true;
        }

        HttpUrl url = HttpUrl.get(configuration.getUrl()).newBuilder().addPathSegments("api/v3/write_lp")
                .addQueryParameter("db", configuration.getDatabaseName()).addQueryParameter("precision", "ms").build();
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + configuration.getToken())
                .post(RequestBody.create(lineProtocolBody.getBytes(StandardCharsets.UTF_8), LINE_PROTOCOL_MEDIA_TYPE))
                .build();

        try (Response response = currentClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                ResponseBody body = response.body();
                logger.debug("Writing to database failed, HTTP status {}: {}", response.code(),
                        body != null ? body.string() : "");
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.debug("Writing to database failed", e);
            return false;
        }
    }

    @Override
    public boolean remove(FilterCriteria filter) {
        logger.warn("Removing data is not supported in InfluxDB v3.");
        return false;
    }

    /**
     * Converts a point to a single InfluxDB line-protocol line. Field-value formatting is based on the Java type
     * produced by {@code InfluxDBStateConvertUtils.stateToObject()} rather than the value itself: {@link BigDecimal}
     * (used for possibly-fractional quantities) is always written as a float, while {@link Integer}/{@link Long}
     * (used only for genuinely integer encodings) get the line-protocol integer suffix. Deciding this per-value
     * instead would make the same field alternate between integer and float across writes, which InfluxDB 3's
     * strict per-field schema rejects as a schema conflict.
     */
    private Optional<String> convertPointToLineProtocol(InfluxPoint point) {
        Optional<String> formattedValue = formatFieldValue(point.getValue());
        if (formattedValue.isEmpty()) {
            logger.warn("Could not convert {}, discarding this datapoint", point);
            return Optional.empty();
        }

        StringBuilder line = new StringBuilder(escapeMeasurement(point.getMeasurementName()));
        // Tags are sorted by key, as recommended by the InfluxDB line protocol reference for write performance.
        point.getTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(tag -> line.append(',')
                .append(escapeTagKeyOrValue(tag.getKey())).append('=').append(escapeTagKeyOrValue(tag.getValue())));
        line.append(' ').append(FIELD_VALUE_NAME).append('=').append(formattedValue.get()).append(' ')
                .append(point.getTime().toEpochMilli());
        return Optional.of(line.toString());
    }

    private Optional<String> formatFieldValue(@Nullable Object value) {
        if (value instanceof String string) {
            return Optional.of('"' + string.replace("\\", "\\\\").replace("\"", "\\\"") + '"');
        } else if (value instanceof BigDecimal bigDecimal) {
            return Optional.of(bigDecimal.toPlainString());
        } else if (value instanceof Integer || value instanceof Long) {
            return Optional.of(value + "i");
        } else if (value instanceof Boolean bool) {
            return Optional.of(bool.toString());
        } else if (value == null) {
            return Optional.of("\"null\"");
        } else {
            return Optional.empty();
        }
    }

    private String escapeMeasurement(String value) {
        return value.replace(",", "\\,").replace(" ", "\\ ");
    }

    private String escapeTagKeyOrValue(String value) {
        return value.replace(",", "\\,").replace("=", "\\=").replace(" ", "\\ ");
    }

    @Override
    public List<InfluxRow> query(FilterCriteria filter, String retentionPolicy, @Nullable String alias) {
        final OkHttpClient currentClient = client;
        if (currentClient == null) {
            logger.warn("Failed to execute query '{}': database is not connected", filter);
            return List.of();
        }

        String itemName = Objects.requireNonNull(filter.getItemName()); // we checked non-null before
        String influxQlQuery = queryCreator.createQuery(filter, retentionPolicy, alias);
        logger.trace("Query {}", influxQlQuery);

        HttpUrl url = HttpUrl.get(configuration.getUrl()).newBuilder().addPathSegments("api/v3/query_influxql")
                .addQueryParameter("db", configuration.getDatabaseName()).addQueryParameter("q", influxQlQuery)
                .addQueryParameter("format", "json").build();
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + configuration.getToken())
                .get().build();

        try (Response response = currentClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String bodyString = responseBody != null ? responseBody.string() : "";
            if (!response.isSuccessful()) {
                logger.warn("Failed to execute query '{}': HTTP status {}: {}", filter, response.code(), bodyString);
                return List.of();
            }
            return parseQueryResult(bodyString, itemName);
        } catch (IOException | JsonParseException e) {
            logger.warn("Failed to execute query '{}': {}", filter, e.getMessage());
            return List.of();
        }
    }

    private List<InfluxRow> parseQueryResult(String bodyString, String defaultItemName) {
        if (bodyString.isBlank()) {
            return List.of();
        }
        JsonArray rows = JsonParser.parseString(bodyString).getAsJsonArray();
        List<InfluxRow> result = new ArrayList<>(rows.size());
        for (JsonElement rowElement : rows) {
            JsonObject row = rowElement.getAsJsonObject();
            Instant time = Instant.parse(row.get(COLUMN_TIME_NAME_V1).getAsString());
            Object value = toJavaValue(row.get(COLUMN_VALUE_NAME_V1));
            JsonElement itemElement = row.get(TAG_ITEM_NAME);
            String itemName = itemElement != null && !itemElement.isJsonNull() ? itemElement.getAsString()
                    : defaultItemName;
            result.add(new InfluxRow(time, itemName, value));
        }
        return result;
    }

    private Object toJavaValue(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "null";
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return primitive.getAsBigDecimal();
        } else if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else {
            return primitive.getAsString();
        }
    }

    @Override
    public Map<String, Integer> getStoredItemsCount() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("getItemInfo not supported for persistence service influxDB3");
    }
}
