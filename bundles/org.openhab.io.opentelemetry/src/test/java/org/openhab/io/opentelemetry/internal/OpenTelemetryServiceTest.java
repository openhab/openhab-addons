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

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.id.InstanceUUID;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.resources.Resource;

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

    private static Meter.Id meterId(String name, Tags tags) {
        return new Meter.Id(name, tags, null, null, Meter.Type.GAUGE);
    }

    @Test
    public void testCoreMetricTagFilterAcceptsTaggedMeters() {
        Tags coreTag = Tags.of(OpenTelemetryService.CORE_METRIC_TAG_KEY, OpenTelemetryService.CORE_METRIC_TAG_VALUE);
        assertTrue(OpenTelemetryService.isCoreMeter(meterId("openhab.thing.count", coreTag)));
        assertTrue(OpenTelemetryService.isCoreMeter(meterId("jvm.memory.used", coreTag)));
        assertTrue(OpenTelemetryService.isCoreMeter(meterId("process.cpu.usage", coreTag)));
        assertTrue(OpenTelemetryService.isCoreMeter(meterId("system.cpu.usage", coreTag)));
        assertTrue(OpenTelemetryService.isCoreMeter(meterId("executor.pool.size", coreTag)));
    }

    @Test
    public void testCoreMetricTagFilterDeniesMetersByNameAlone() {
        // A third-party meter using an openHAB-style prefix must still be denied if untagged
        assertFalse(OpenTelemetryService.isCoreMeter(meterId("executor.pool.size", Tags.empty())));
        assertFalse(OpenTelemetryService.isCoreMeter(meterId("jvm.memory.used", Tags.empty())));
        assertFalse(OpenTelemetryService.isCoreMeter(meterId("kafka.consumer.records", Tags.empty())));
        assertFalse(OpenTelemetryService.isCoreMeter(meterId("", Tags.empty())));
    }

    @Test
    public void testServiceInstanceIdUsesOpenHabInstanceUuid() {
        // Must delegate to core's persistent InstanceUUID, not a locally generated value
        assertEquals(InstanceUUID.get(), OpenTelemetryService.SERVICE_INSTANCE_ID);
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
        assertEquals(1.0, OpenTelemetryService.clampSamplingRatio(Double.NaN), 0.0001,
                "NaN sampling ratio must fall back to 1.0 (sample all)");
        assertEquals(0.0, OpenTelemetryService.clampSamplingRatio(-0.5), 0.0001);
        assertEquals(1.0, OpenTelemetryService.clampSamplingRatio(1.5), 0.0001);
        assertEquals(0.5, OpenTelemetryService.clampSamplingRatio(0.5), 0.0001);
    }

    @Test
    public void testIsGlobalOpenTelemetrySetReflectsGlobalState() {
        assertFalse(OpenTelemetryService.isGlobalOpenTelemetrySet());
        GlobalOpenTelemetry.set(OpenTelemetry.noop());
        try {
            assertTrue(OpenTelemetryService.isGlobalOpenTelemetrySet());
        } finally {
            GlobalOpenTelemetry.resetForTest();
        }
    }

    @Test
    public void testInvalidTracesSchemeReturnsNull() {
        // setEndpoint() in OtlpHttpSpanExporter rejects non-http(s) schemes;
        // the method must return null rather than propagating an IllegalArgumentException
        OpenTelemetryConfiguration config = new OpenTelemetryConfiguration();
        config.tracesEnabled = true;
        config.otlpURL = "ftp://host";
        config.tracesEndpoint = "/v1/traces";
        assertNull(
                new OpenTelemetryService().createSdkTracerProvider(config, Resource.empty(), Collections.emptyMap()));
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
