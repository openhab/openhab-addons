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
package org.openhab.persistence.timescaledb.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Reads per-item configuration from the {@link MetadataRegistry} using the {@code timescaledb} namespace.
 *
 * <p>
 * Example item metadata:
 *
 * <pre>
 * Number:Temperature MySensor {
 *     timescaledb="sensor.temperature" [ aggregation="AVG", downsampleInterval="1h", retainRawDays="5",
 *         retentionDays="365", kind="sensor", location="living_room" ]
 * }
 * </pre>
 *
 * <ul>
 * <li>{@code getValue()} — user-defined string (measurement label / filter tag), stored in
 * {@code item_meta.value}</li>
 * <li>{@code getConfiguration()} — full config map stored as JSONB in {@code item_meta.metadata}; reserved keys:
 * {@code aggregation}, {@code downsampleInterval}, {@code retainRawDays}, {@code retentionDays}</li>
 * </ul>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
@Component(service = TimescaleDBMetadataService.class)
public class TimescaleDBMetadataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBMetadataService.class);
    private static final Gson GSON = new Gson();

    /** The metadata namespace used by this persistence service. */
    public static final String METADATA_NAMESPACE = "timescaledb";

    private static final int DEFAULT_RETAIN_RAW_DAYS = 5;
    private static final int DEFAULT_RETENTION_DAYS = 0;

    private final MetadataRegistry metadataRegistry;

    @Activate
    public TimescaleDBMetadataService(final @Reference MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * Returns the {@link DownsampleConfig} for the given item, or empty if no downsampling
     * is configured or the metadata cannot be parsed.
     *
     * @param itemName The item name.
     * @return Optional containing the parsed config, or empty.
     */
    public Optional<DownsampleConfig> getDownsampleConfig(String itemName) {
        MetadataKey key = new MetadataKey(METADATA_NAMESPACE, itemName);
        @Nullable
        Metadata metadata = metadataRegistry.get(key);
        if (metadata == null) {
            return Optional.empty();
        }
        return parseConfig(itemName, metadata);
    }

    /**
     * Returns the names of all items that have a {@code timescaledb} metadata entry,
     * regardless of whether they configure downsampling, retention-only, or both.
     *
     * @return List of item names with any timescaledb metadata.
     */
    public List<String> getConfiguredItemNames() {
        List<String> result = new ArrayList<>();
        for (Metadata metadata : metadataRegistry.getAll()) {
            if (!METADATA_NAMESPACE.equals(metadata.getUID().getNamespace())) {
                continue;
            }
            result.add(metadata.getUID().getItemName());
        }
        return result;
    }

    /**
     * Returns the user-defined value string from {@code metadata.getValue()}, stored verbatim in
     * {@code item_meta.value}. Returns {@code null} if no metadata is configured or the value is blank.
     *
     * <p>
     * Example: {@code timescaledb="sensor.temperature" [...]} → returns {@code "sensor.temperature"}.
     *
     * @param itemName The item name.
     * @return The value string, or {@code null}.
     */
    public @Nullable String getMetadataValueString(String itemName) {
        MetadataKey key = new MetadataKey(METADATA_NAMESPACE, itemName);
        @Nullable
        Metadata metadata = metadataRegistry.get(key);
        if (metadata == null) {
            return null;
        }
        String v = metadata.getValue();
        return v.isBlank() ? null : v;
    }

    /**
     * Returns the full {@code getConfiguration()} map serialized as a JSON string, suitable for storage
     * in {@code item_meta.metadata} (JSONB column). Returns {@code null} if no metadata is configured
     * or the config map is empty.
     *
     * <p>
     * All config keys are stored unfiltered, including reserved keys ({@code aggregation},
     * {@code downsampleInterval}, {@code retainRawDays}, {@code retentionDays}) and any user-defined tags.
     *
     * @param itemName The item name.
     * @return JSON string of the config map, or {@code null}.
     */
    public @Nullable String getMetadataConfigJson(String itemName) {
        MetadataKey key = new MetadataKey(METADATA_NAMESPACE, itemName);
        @Nullable
        Metadata metadata = metadataRegistry.get(key);
        if (metadata == null) {
            return null;
        }
        Map<String, Object> config = metadata.getConfiguration();
        if (config.isEmpty()) {
            return null;
        }
        return GSON.toJson(config);
    }

    private Optional<DownsampleConfig> parseConfig(String itemName, Metadata metadata) {
        var config = metadata.getConfiguration();
        Object aggObj = config.get("aggregation");
        String functionStr = aggObj != null ? aggObj.toString().trim() : "";

        if (functionStr.isBlank()) {
            // No aggregation function — check for retention-only config.
            // Note: openHAB requires a non-empty metadata value, so use a single space (" ")
            // in item files and the UI when you only want retention without downsampling.
            int retentionDays = getInt(config, "retentionDays", DEFAULT_RETENTION_DAYS);
            if (retentionDays < 0) {
                LOGGER.warn("Item '{}': retentionDays must be >= 0, ignoring negative value {}", itemName,
                        retentionDays);
                return Optional.empty();
            }
            if (retentionDays > 0) {
                LOGGER.debug("Item '{}': retention-only config with retentionDays={}", itemName, retentionDays);
                return Optional.of(DownsampleConfig.retentionOnly(retentionDays));
            }
            return Optional.empty();
        }

        AggregationFunction function;
        try {
            function = AggregationFunction.valueOf(functionStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Item '{}': unknown aggregation function '{}' in timescaledb metadata — skipping", itemName,
                    functionStr);
            return Optional.empty();
        }

        String intervalStr = getString(config, "downsampleInterval", null);
        if (intervalStr == null || intervalStr.isBlank()) {
            LOGGER.warn("Item '{}': timescaledb metadata has aggregation '{}' but no downsampleInterval — skipping",
                    itemName, functionStr);
            return Optional.empty();
        }

        String sqlInterval;
        try {
            sqlInterval = DownsampleConfig.toSqlInterval(intervalStr);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Item '{}': {}", itemName, e.getMessage());
            return Optional.empty();
        }

        int retainRawDays = getInt(config, "retainRawDays", DEFAULT_RETAIN_RAW_DAYS);
        if (retainRawDays < 0) {
            LOGGER.warn("Item '{}': retainRawDays must be >= 0, using default {}", itemName, DEFAULT_RETAIN_RAW_DAYS);
            retainRawDays = DEFAULT_RETAIN_RAW_DAYS;
        }
        int retentionDays = getInt(config, "retentionDays", DEFAULT_RETENTION_DAYS);
        if (retentionDays < 0) {
            LOGGER.warn("Item '{}': retentionDays must be >= 0, using default {}", itemName, DEFAULT_RETENTION_DAYS);
            retentionDays = DEFAULT_RETENTION_DAYS;
        }

        DownsampleConfig result = new DownsampleConfig(function, sqlInterval, retainRawDays, retentionDays);
        LOGGER.debug("Item '{}': parsed DownsampleConfig {}", itemName, result);
        return Optional.of(result);
    }

    private static @Nullable String getString(Map<String, Object> config, String key, @Nullable String defaultValue) {
        Object val = config.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private static int getInt(Map<String, Object> config, String key, int defaultValue) {
        Object val = config.get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid integer value '{}' for metadata key '{}', using default {}", val, key, defaultValue);
            return defaultValue;
        }
    }
}
