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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
class TimescaleDBMetadataServiceTest {

    private MetadataRegistry registry;
    private TimescaleDBMetadataService service;

    @BeforeEach
    void setUp() {
        registry = mock(MetadataRegistry.class);
        service = new TimescaleDBMetadataService(registry);
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — happy paths (aggregation now in config map)
    // ------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({ "AVG,1h,1 hour", "MAX,15m,15 minutes", "MIN,1d,1 day", "SUM,30m,30 minutes" })
    void getDownsampleConfigValidaggregationandinterval(String aggregation, String interval, String expectedSql) {
        stubMetadata("MySensor", "sensor.temperature",
                Map.of("aggregation", aggregation, "downsampleInterval", interval));

        var config = service.getDownsampleConfig("MySensor");

        assertTrue(config.isPresent());
        assertEquals(AggregationFunction.valueOf(aggregation), config.get().function());
        assertEquals(expectedSql, config.get().sqlInterval());
        assertEquals(5, config.get().retainRawDays()); // default
        assertEquals(0, config.get().retentionDays()); // default
    }

    @Test
    void getDownsampleConfigCustomretainrawandretentiondays() {
        stubMetadata("MySensor", "sensor.temperature",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h", "retainRawDays", "7", "retentionDays", "365"));

        var config = service.getDownsampleConfig("MySensor").orElseThrow();

        assertEquals(7, config.retainRawDays());
        assertEquals(365, config.retentionDays());
    }

    @Test
    void getDownsampleConfigAllsupportedintervals() {
        for (Map.Entry<String, String> entry : DownsampleConfig.INTERVAL_MAP.entrySet()) {
            stubMetadata("Item_" + entry.getKey(), "my.sensor",
                    Map.of("aggregation", "AVG", "downsampleInterval", entry.getKey()));
            var config = service.getDownsampleConfig("Item_" + entry.getKey());
            assertTrue(config.isPresent(), "Should parse interval: " + entry.getKey());
            assertEquals(entry.getValue(), config.get().sqlInterval());
        }
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — no / empty aggregation
    // ------------------------------------------------------------------

    @Test
    void getDownsampleConfigNometadataReturnsempty() {
        when(registry.get(new MetadataKey("timescaledb", "Unknown"))).thenReturn(null);

        assertTrue(service.getDownsampleConfig("Unknown").isEmpty());
    }

    @Test
    void getDownsampleConfigNoAggregationKeyWithRetentiondaysReturnsRetentiononlyconfig() {
        // No aggregation key + retentionDays → retention-only config
        stubMetadata("MySensor", "my.sensor", Map.of("retentionDays", "30"));

        Optional<DownsampleConfig> result = service.getDownsampleConfig("MySensor");
        assertTrue(result.isPresent());
        assertFalse(result.get().hasDownsampling());
        assertEquals(30, result.get().retentionDays());
        assertNull(result.get().function());
        assertNull(result.get().sqlInterval());
    }

    @Test
    void getDownsampleConfigNoAggregationKeyWithoutRetentiondaysReturnsempty() {
        // No aggregation key + no retentionDays → nothing to do
        stubMetadata("MySensor", "my.sensor", Map.of());

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfigBlankValueFieldDoesNotAffectAggregationParsing() {
        // getValue() is now user-defined label — a blank value must not affect aggregation parsing
        stubMetadata("MySensor", " ", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));

        Optional<DownsampleConfig> result = service.getDownsampleConfig("MySensor");
        assertTrue(result.isPresent());
        assertEquals(AggregationFunction.AVG, result.get().function());
    }

    // ------------------------------------------------------------------
    // getDownsampleConfig — invalid / unsupported values
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = { "2h30m", "3m", "1w", "invalid", "" })
    void getDownsampleConfigInvalidintervalReturnsempty(String badInterval) {
        if (badInterval.isBlank()) {
            stubMetadata("MySensor", "s", Map.of("aggregation", "AVG"));
        } else {
            stubMetadata("MySensor", "s", Map.of("aggregation", "AVG", "downsampleInterval", badInterval));
        }

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfigInvalidaggregationReturnsempty() {
        stubMetadata("MySensor", "s", Map.of("aggregation", "MEDIAN", "downsampleInterval", "1h"));

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfigMissingintervalReturnsempty() {
        stubMetadata("MySensor", "s", Map.of("aggregation", "AVG"));

        assertTrue(service.getDownsampleConfig("MySensor").isEmpty());
    }

    @Test
    void getDownsampleConfigInvalidretainrawdaysUsesdefault() {
        stubMetadata("MySensor", "s",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h", "retainRawDays", "not-a-number"));

        var config = service.getDownsampleConfig("MySensor").orElseThrow();
        assertEquals(5, config.retainRawDays()); // falls back to default
    }

    // ------------------------------------------------------------------
    // DownsampleConfig.toSqlInterval — allowlist enforcement
    // ------------------------------------------------------------------

    @Test
    void toSqlIntervalInvalidvalueThrowsillegalargument() {
        assertThrows(IllegalArgumentException.class, () -> DownsampleConfig.toSqlInterval("99h"));
    }

    @ParameterizedTest
    @CsvSource({ "1m,1 minute", "5m,5 minutes", "15m,15 minutes", "30m,30 minutes", "1h,1 hour", "6h,6 hours",
            "1d,1 day" })
    void toSqlIntervalValidvalues(String input, String expected) {
        assertEquals(expected, DownsampleConfig.toSqlInterval(input));
    }

    // ------------------------------------------------------------------
    // getConfiguredItemNames
    // ------------------------------------------------------------------

    @Test
    void getConfiguredItemNamesReturnsallTimescaledbitemsRegardlessofvalue() {
        Metadata withAggregation = metadata("SensorA", "sensor.a",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h"));
        Metadata retentionOnly = metadata("SensorB", "sensor.b", Map.of("retentionDays", "30"));
        Metadata otherNamespace = new Metadata(new MetadataKey("influxdb", "SensorC"), "some", Map.of());

        when(registry.getAll()).thenAnswer(inv -> List.of(withAggregation, retentionOnly, otherNamespace));

        List<String> names = service.getConfiguredItemNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("SensorA"));
        assertTrue(names.contains("SensorB"));
        assertFalse(names.contains("SensorC"));
    }

    @Test
    void getConfiguredItemNamesEmptyregistryReturnsemptylist() {
        when(registry.getAll()).thenReturn(List.of());
        assertTrue(service.getConfiguredItemNames().isEmpty());
    }

    // ------------------------------------------------------------------
    // getMetadataValueString
    // ------------------------------------------------------------------

    @Test
    void getMetadataValueStringNometadataReturnsNull() {
        when(registry.get(new MetadataKey("timescaledb", "Unknown"))).thenReturn(null);

        assertNull(service.getMetadataValueString("Unknown"));
    }

    @Test
    void getMetadataValueStringReturnsValueField() {
        stubMetadata("MySensor", "sensor.temperature", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));

        assertEquals("sensor.temperature", service.getMetadataValueString("MySensor"));
    }

    @Test
    void getMetadataValueStringBlankValueReturnsNull() {
        stubMetadata("MySensor", " ", Map.of("retentionDays", "30"));

        assertNull(service.getMetadataValueString("MySensor"),
                "Blank getValue() must be treated as absent and return null");
    }

    @Test
    void getMetadataValueStringDoesNotUseConfigKeys() {
        // The value field is getValue(), not any config key — aggregation must NOT appear here
        stubMetadata("MySensor", "sensor.temperature", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));

        assertEquals("sensor.temperature", service.getMetadataValueString("MySensor"));
        assertNotEquals("AVG", service.getMetadataValueString("MySensor"),
                "getMetadataValueString must return getValue(), not the aggregation config key");
    }

    // ------------------------------------------------------------------
    // getMetadataConfigJson
    // ------------------------------------------------------------------

    @Test
    void getMetadataConfigJsonNometadataReturnsNull() {
        when(registry.get(new MetadataKey("timescaledb", "Unknown"))).thenReturn(null);

        assertNull(service.getMetadataConfigJson("Unknown"));
    }

    @Test
    void getMetadataConfigJsonEmptyConfigReturnsNull() {
        stubMetadata("MySensor", "sensor.temperature", Map.of());

        assertNull(service.getMetadataConfigJson("MySensor"));
    }

    @Test
    void getMetadataConfigJsonReturnsSerializedMap() {
        stubMetadata("MySensor", "sensor.temperature", Map.of("aggregation", "AVG", "downsampleInterval", "1h"));

        String json = service.getMetadataConfigJson("MySensor");
        assertNotNull(json);
        assertTrue(json.startsWith("{"), "Must be a JSON object");
        assertTrue(json.contains("\"aggregation\""), "Must contain aggregation key");
        assertTrue(json.contains("\"AVG\""), "Must contain aggregation value");
        assertTrue(json.contains("\"downsampleInterval\""), "Must contain downsampleInterval key");
    }

    @Test
    void getMetadataConfigJsonIncludesAllConfigKeys() {
        // All config keys must be stored — no filtering
        stubMetadata("MySensor", "sensor.temperature", Map.of("aggregation", "AVG", "downsampleInterval", "1h",
                "retainRawDays", "5", "retentionDays", "365", "location", "living_room", "kind", "sensor"));

        String json = service.getMetadataConfigJson("MySensor");
        assertNotNull(json);
        assertTrue(json.contains("\"location\""), "User-defined tag 'location' must be stored");
        assertTrue(json.contains("\"kind\""), "User-defined tag 'kind' must be stored");
        assertTrue(json.contains("\"retainRawDays\""), "Reserved key retainRawDays must be stored");
        assertTrue(json.contains("\"retentionDays\""), "Reserved key retentionDays must be stored");
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
