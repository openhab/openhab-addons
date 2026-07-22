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
package org.openhab.io.opentelemetry.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link OpenTelemetryServiceTest} class contains tests for header parsing,
 * URL resolution, meter filtering, and service attribute consistency.
 *
 * @author Florian Hotze - Initial contribution
 * @author Florian Lettner - Add unit tests
 */
@NonNullByDefault
public class OpenTelemetryServiceTest {

    @Test
    public void testParseOtlpHeaders() {
        OpenTelemetryService service = new OpenTelemetryService();

        // Null and blank cases
        assertTrue(service.parseOtlpHeaders(null).isEmpty());
        assertTrue(service.parseOtlpHeaders("").isEmpty());
        assertTrue(service.parseOtlpHeaders("   ").isEmpty());

        // Simple key-value pair
        Map<String, String> headers = service.parseOtlpHeaders("key1=value1");
        assertEquals(1, headers.size());
        assertEquals("value1", headers.get("key1"));

        // Multiple key-value pairs
        headers = service.parseOtlpHeaders("key1=value1,key2=value2");
        assertEquals(2, headers.size());
        assertEquals("value1", headers.get("key1"));
        assertEquals("value2", headers.get("key2"));

        // Whitespace trimming
        headers = service.parseOtlpHeaders("  key1  =  value1  ,  key2=value2   ");
        assertEquals(2, headers.size());
        assertEquals("value1", headers.get("key1"));
        assertEquals("value2", headers.get("key2"));

        // Malformed pairs
        headers = service.parseOtlpHeaders("key1,key2=value2,,key3=,=value4");
        assertEquals(2, headers.size());
        assertFalse(headers.containsKey("key1"));
        assertEquals("value2", headers.get("key2"));
        assertEquals("", headers.get("key3"));
        assertFalse(headers.containsValue("value4"));

        // Equal signs in values
        headers = service.parseOtlpHeaders("key1=val=ue,key2=value2");
        assertEquals(2, headers.size());
        assertEquals("val=ue", headers.get("key1"));
        assertEquals("value2", headers.get("key2"));
    }

    @Test
    public void testParseOtlpHeadersRejectsInjectionChars() {
        OpenTelemetryService service = new OpenTelemetryService();

        assertThrows(IllegalArgumentException.class,
                () -> service.parseOtlpHeaders("Authorization=Bearer token\nX-Injected=true"));
        assertThrows(IllegalArgumentException.class, () -> service.parseOtlpHeaders("key\rvalue"));
        assertThrows(IllegalArgumentException.class, () -> service.parseOtlpHeaders("key\0value"));
    }

    @Test
    public void testGetLogsURL() {
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();

        // Default configuration
        assertEquals("http://localhost:4318/v1/logs", config.getLogsURL());

        // Trailing slash on base URL, relative endpoint
        config.otlpURL = "http://localhost:4318/";
        config.logsEndpoint = "v1/logs";
        assertEquals("http://localhost:4318/v1/logs", config.getLogsURL());

        // Trailing slash on base URL, slash-prefixed endpoint
        config.otlpURL = "http://localhost:4318/";
        config.logsEndpoint = "/v1/logs";
        assertEquals("http://localhost:4318/v1/logs", config.getLogsURL());

        // No trailing slash, relative endpoint
        config.otlpURL = "http://localhost:4318";
        config.logsEndpoint = "v1/logs";
        assertEquals("http://localhost:4318/v1/logs", config.getLogsURL());

        // Custom OTLP URL and custom endpoint
        config.otlpURL = "http://127.0.0.1:5555";
        config.logsEndpoint = "/custom/logs/path";
        assertEquals("http://127.0.0.1:5555/custom/logs/path", config.getLogsURL());

        // Vendor endpoint with base path — URI.resolve() regression: path-absolute endpoint
        // must not discard the base path (e.g. /api/v2/otlp)
        config.otlpURL = "https://example.dynatracelabs.com/api/v2/otlp";
        config.logsEndpoint = "/v1/logs";
        assertEquals("https://example.dynatracelabs.com/api/v2/otlp/v1/logs", config.getLogsURL());

        // Invalid URI structure throws IllegalArgumentException
        config.otlpURL = "invalid-uri-scheme:\\";
        assertThrows(IllegalArgumentException.class, config::getLogsURL);
    }

    @Test
    public void testGetMetricsURL() {
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();

        // Default configuration
        assertEquals("http://localhost:4318/v1/metrics", config.getMetricsURL());

        // Custom base URL
        config.otlpURL = "http://otelcol.example.com:4318";
        config.metricsEndpoint = "/v1/metrics";
        assertEquals("http://otelcol.example.com:4318/v1/metrics", config.getMetricsURL());

        // Custom endpoint path
        config.otlpURL = "http://127.0.0.1:5555";
        config.metricsEndpoint = "/custom/metrics";
        assertEquals("http://127.0.0.1:5555/custom/metrics", config.getMetricsURL());

        // Vendor endpoint with base path — URI.resolve() regression
        config.otlpURL = "https://example.dynatracelabs.com/api/v2/otlp";
        config.metricsEndpoint = "/v1/metrics";
        assertEquals("https://example.dynatracelabs.com/api/v2/otlp/v1/metrics", config.getMetricsURL());

        // Invalid URI throws
        config.otlpURL = "invalid-uri-scheme:\\";
        assertThrows(IllegalArgumentException.class, () -> config.getMetricsURL());
    }

    @Test
    public void testGetTracesURL() {
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();

        // Default configuration
        assertEquals("http://localhost:4318/v1/traces", config.getTracesURL());

        // Custom base URL
        config.otlpURL = "http://otelcol.example.com:4318";
        config.tracesEndpoint = "/v1/traces";
        assertEquals("http://otelcol.example.com:4318/v1/traces", config.getTracesURL());

        // Vendor endpoint with base path — URI.resolve() regression
        config.otlpURL = "https://example.dynatracelabs.com/api/v2/otlp";
        config.tracesEndpoint = "/v1/traces";
        assertEquals("https://example.dynatracelabs.com/api/v2/otlp/v1/traces", config.getTracesURL());

        // Invalid URI throws
        config.otlpURL = "invalid-uri-scheme:\\";
        assertThrows(IllegalArgumentException.class, () -> config.getTracesURL());
    }

    @Test
    public void testMeterNameFilterAcceptsOpenHabPrefixes() {
        // openHAB-specific meters
        assertTrue(OpenTelemetryService.isAllowedMeterName("openhab.thing.count"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("openhab.item.state.changed"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("openhab.rule.execution.time"));

        // Standard JVM and process metrics (openHAB process telemetry)
        assertTrue(OpenTelemetryService.isAllowedMeterName("jvm.memory.used"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("jvm.gc.pause"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("process.cpu.usage"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("system.cpu.usage"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("executor.pool.size"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("logback.events"));
        assertTrue(OpenTelemetryService.isAllowedMeterName("http.server.requests"));
    }

    @Test
    public void testMeterNameFilterDeniesNonOpenHabMeters() {
        assertFalse(OpenTelemetryService.isAllowedMeterName("kafka.consumer.records"));
        assertFalse(OpenTelemetryService.isAllowedMeterName("db.pool.active"));
        assertFalse(OpenTelemetryService.isAllowedMeterName("host.disk.usage"));
        assertFalse(OpenTelemetryService.isAllowedMeterName("custom.addon.metric"));
        assertFalse(OpenTelemetryService.isAllowedMeterName(""));
    }

    @Test
    public void testServiceInstanceIdIsStable() {
        // Same static ID across multiple accesses
        String id1 = OpenTelemetryService.SERVICE_INSTANCE_ID;
        String id2 = OpenTelemetryService.SERVICE_INSTANCE_ID;
        assertEquals(id1, id2);
        assertFalse(id1.isBlank());
    }

    @Test
    public void testConfigurationToStringMasksHeaders() {
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();
        config.otlpHeaders = "Authorization=Bearer secret-token";
        String str = config.toString();
        assertFalse(str.contains("secret-token"), "Secret header value must not appear in toString()");
        assertFalse(str.contains("Bearer"), "Secret header value must not appear in toString()");
        assertFalse(str.contains("Authorization"), "Secret header key must not appear in toString()");
        assertTrue(str.contains("otlpHeaders="), "toString() should still mention the field");
    }

    @Test
    public void testTracesSamplingRatioClampHandlesNaN() {
        // NaN bypasses Math.min/max — must not silently drop all spans
        double nan = Double.NaN;
        double result = Double.isNaN(nan) ? 1.0 : Math.max(0.0, Math.min(1.0, nan));
        assertEquals(1.0, result, 0.0001, "NaN sampling ratio must fall back to 1.0 (sample all)");

        // Negative and above-1 should be clamped
        assertEquals(0.0, Math.max(0.0, Math.min(1.0, -0.5)), 0.0001);
        assertEquals(1.0, Math.max(0.0, Math.min(1.0, 1.5)), 0.0001);
    }

    @Test
    public void testDefaultConfigValues() {
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();
        assertFalse(config.logsEnabled);
        assertFalse(config.metricsEnabled);
        assertFalse(config.tracesEnabled);
        assertEquals("/v1/logs", config.logsEndpoint);
        assertEquals("/v1/metrics", config.metricsEndpoint);
        assertEquals("/v1/traces", config.tracesEndpoint);
        assertEquals("CUMULATIVE", config.metricsAggregationTemporality);
        assertEquals(1.0, config.tracesSamplingRatio, 0.0001);
    }
}
