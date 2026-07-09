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
 * The {@link OpenTelemetryServiceTest} class contains tests for header parsing
 * and URL resolution.
 *
 * @author Florian Hotze - Initial contribution
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
        headers = service.parseOtlpHeaders("key1,key2=value2,,key3=");
        assertEquals(2, headers.size());
        assertFalse(headers.containsKey("key1"));
        assertEquals("value2", headers.get("key2"));
        assertEquals("", headers.get("key3"));

        // Equal signs in values
        headers = service.parseOtlpHeaders("key1=val=ue,key2=value2");
        assertEquals(2, headers.size());
        assertEquals("val=ue", headers.get("key1"));
        assertEquals("value2", headers.get("key2"));
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

        // Invalid URI structure throws IllegalArgumentException
        config.otlpURL = "invalid-uri-scheme:\\";
        assertThrows(IllegalArgumentException.class, () -> config.getLogsURL());
    }
}
