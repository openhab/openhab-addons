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

/**
 * Reads and parses per-item downsampling configuration from the {@link MetadataRegistry}
 * using the {@code timescaledb} namespace.
 *
 * <p>
 * Example item metadata:
 * 
 * <pre>
 * Number:Temperature MySensor {
 *     timescaledb="AVG" [ downsampleInterval="1h", retainRawDays="5", retentionDays="365" ]
 * }
 * </pre>
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@Component(service = TimescaleDBMetadataService.class)
public class TimescaleDBMetadataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBMetadataService.class);

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
     * Returns the names of all items that have a {@code timescaledb} metadata entry
     * with a non-empty aggregation function.
     *
     * @return List of item names configured for downsampling.
     */
    public List<String> getItemNamesWithDownsampling() {
        List<String> result = new ArrayList<>();
        for (Metadata metadata : metadataRegistry.getAll()) {
            if (!METADATA_NAMESPACE.equals(metadata.getUID().getNamespace())) {
                continue;
            }
            String value = metadata.getValue();
            if (!value.isBlank()) {
                result.add(metadata.getUID().getItemName());
            }
        }
        return result;
    }

    private Optional<DownsampleConfig> parseConfig(String itemName, Metadata metadata) {
        String functionStr = metadata.getValue();
        if (functionStr.isBlank()) {
            // Metadata present but no aggregation function — only per-item retention applies
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

        var config = metadata.getConfiguration();

        String intervalStr = getString(config, "downsampleInterval", null);
        if (intervalStr == null || intervalStr.isBlank()) {
            LOGGER.warn("Item '{}': timescaledb metadata has function '{}' but no downsampleInterval — skipping",
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
        int retentionDays = getInt(config, "retentionDays", DEFAULT_RETENTION_DAYS);

        DownsampleConfig result = new DownsampleConfig(function, sqlInterval, retainRawDays, retentionDays);
        LOGGER.debug("Item '{}': parsed DownsampleConfig {}", itemName, result);
        return Optional.of(result);
    }

    private static @Nullable String getString(java.util.Map<String, Object> config, String key,
            @Nullable String defaultValue) {
        Object val = config.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private static int getInt(java.util.Map<String, Object> config, String key, int defaultValue) {
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
