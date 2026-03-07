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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * Unit tests for {@link TimescaleDBMetadataService}.
 */
class TimescaleDBMetadataServiceTest {

    private MetadataRegistry registry;
    private TimescaleDBMetadataService service;

    @BeforeEach
    void setUp() {
        registry = mock(MetadataRegistry.class);
        service = new TimescaleDBMetadataService(registry);
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — happy paths
    // ------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({ "AVG,1h,1 hour", "MAX,15m,15 minutes", "MIN,1d,1 day", "SUM,30m,30 minutes" })
    void getDownsampleConfig_validFunctionAndInterval(String function, String interval, String expectedSql) {
        stubMetadata("MySensor", function, Map.of("downsampleInterval", interval));

        var config = service.getDownsampleConfig("MySensor");

        assertTrue(config.isPresent());
        assertEquals(AggregationFunction.valueOf(function), config.get().function());
        assertEquals(expectedSql, config.get().sqlInterval());
        assertEquals(5, config.get().retainRawDays()); // default
        assertEquals(0, config.get().retentionDays()); // default
    }

    @Test
    void getDownsampleConfig_customRetainRawAndRetentionDays() {
        stubMetadata("MySensor", "AVG",
                Map.of("downsampleInterval", "1h", "retainRawDays", "7", "retentionDays", "365"));

        var config = service.getDownsampleConfig("MySensor").orElseThrow();

        assertEquals(7, config.retainRawDays());
        assertEquals(365, config.retentionDays());
    }

    @Test
    void getDownsampleConfig_allSupportedIntervals() {
        for (Map.Entry<String, String> entry : DownsampleConfig.INTERVAL_MAP.entrySet()) {
            stubMetadata("Item_" + entry.getKey(), "AVG", Map.of("downsampleInterval", entry.getKey()));
            var config = service.getDownsampleConfig("Item_" + entry.getKey());
            assertTrue(config.isPresent(), "Should parse interval: " + entry.getKey());
            assertEquals(entry.getValue(), config.get().sqlInterval());
        }
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — no / empty metadata
    // ------------------------------------------------------------------

    @Test
    void getDownsampleConfig_noMetadata_returnsEmpty() {
        when(registry.get(new MetadataKey("timescaledb", "Unknown"))).thenReturn(null);

        assertTrue(service.getDownsampleConfig("Unknown").isEmpty());
    }

    @Test
    void getDownsampleConfig_emptyFunction_returnsEmpty() {
        // Metadata present but value="" means retention-only, no downsampling
        stubMetadata("MySensor", "", Map.of("retentionDays", "30"));

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — invalid / unsupported values
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "2h", "3m", "1w", "invalid", "" })
    void getDownsampleConfig_invalidInterval_returnsEmpty(String badInterval) {
        if (badInterval.isBlank()) {
            // handled by the missing-interval branch
            stubMetadata("MySensor", "AVG", Map.of());
        } else {
            stubMetadata("MySensor", "AVG", Map.of("downsampleInterval", badInterval));
        }

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfig_invalidFunction_returnsEmpty() {
        stubMetadata("MySensor", "MEDIAN", Map.of("downsampleInterval", "1h"));

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfig_missingInterval_returnsEmpty() {
        // Function present but no interval → cannot downsample
        stubMetadata("MySensor", "AVG", Map.of());

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfig_invalidRetainRawDays_usesDefault() {
        stubMetadata("MySensor", "AVG", Map.of("downsampleInterval", "1h", "retainRawDays", "not-a-number"));

        var config = service.getDownsampleConfig("MySensor").orElseThrow();
        assertEquals(5, config.retainRawDays()); // falls back to default
    }

    // ------------------------------------------------------------------
    // DownsampleConfig.toSqlInterval — allowlist enforcement
    // ------------------------------------------------------------------

    @Test
    void toSqlInterval_invalidValue_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> DownsampleConfig.toSqlInterval("99h"));
    }

    @ParameterizedTest
    @CsvSource({ "1m,1 minute", "5m,5 minutes", "15m,15 minutes", "30m,30 minutes", "1h,1 hour", "6h,6 hours",
            "1d,1 day" })
    void toSqlInterval_validValues(String input, String expected) {
        assertEquals(expected, DownsampleConfig.toSqlInterval(input));
    }

    // ------------------------------------------------------------------
    // getItemNamesWithDownsampling
    // ------------------------------------------------------------------

    @Test
    void getItemNamesWithDownsampling_returnsOnlyItemsWithNonEmptyFunction() {
        Metadata withFunction = metadata("SensorA", "AVG", Map.of("downsampleInterval", "1h"));
        Metadata noFunction = metadata("SensorB", "", Map.of("retentionDays", "30"));
        Metadata otherNamespace = new Metadata(new MetadataKey("influxdb", "SensorC"), "some", Map.of());

        when(registry.getAll()).thenReturn((Collection) List.of(withFunction, noFunction, otherNamespace));

        List<String> names = service.getItemNamesWithDownsampling();

        assertEquals(1, names.size());
        assertTrue(names.contains("SensorA"));
    }

    @Test
    void getItemNamesWithDownsampling_emptyRegistry_returnsEmptyList() {
        when(registry.getAll()).thenReturn(List.of());
        assertTrue(service.getItemNamesWithDownsampling().isEmpty());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void stubMetadata(String itemName, String value, Map<String, Object> config) {
        MetadataKey key = new MetadataKey("timescaledb", itemName);
        Metadata meta = metadata(itemName, value, config);
        when(registry.get(key)).thenReturn(meta);
    }

    private static Metadata metadata(String itemName, String value, Map<String, Object> config) {
        return new Metadata(new MetadataKey("timescaledb", itemName), value, config);
    }
}
