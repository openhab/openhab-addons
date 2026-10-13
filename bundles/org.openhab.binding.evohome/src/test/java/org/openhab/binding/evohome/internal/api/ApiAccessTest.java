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
package org.openhab.binding.evohome.internal.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests HTTP failures and server-directed retry delays.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class ApiAccessTest {
    private final HttpClient http = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ContentResponse response = mock(ContentResponse.class);
    private final HttpFields headers = new HttpFields();
    private final ApiAccess api = new ApiAccess(http);

    @BeforeEach
    void setup() throws Exception {
        when(http.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{}");
        when(response.getHeaders()).thenReturn(headers);
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 429, 500, 503 })
    void rejectsHttpFailures(int status) {
        when(response.getStatus()).thenReturn(status);
        EvohomeApiClientException error = assertThrows(EvohomeApiClientException.class,
                () -> api.doAuthenticatedPut("https://example.test", Map.of()));
        assertEquals(status, error.getStatusCode());
    }

    @Test
    void acceptsNoContentForCommands() {
        when(response.getStatus()).thenReturn(204);
        assertDoesNotThrow(() -> api.doAuthenticatedPut("https://example.test", Map.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "null", "{broken" })
    void rejectsEmptyOrMalformedReadResponse(String body) {
        when(response.getContentAsString()).thenReturn(body);
        assertThrows(EvohomeApiClientException.class, () -> api.doAuthenticatedGet("https://example.test", Map.class));
    }

    @Test
    void propagatesTimeout() throws Exception {
        when(request.send()).thenThrow(new TimeoutException());
        assertThrows(TimeoutException.class, () -> api.doAuthenticatedGet("https://example.test", Map.class));
    }

    @Test
    void propagatesTransportFailure() throws Exception {
        when(request.send()).thenThrow(new ExecutionException(new IllegalStateException("transport")));
        assertThrows(EvohomeApiClientException.class, () -> api.doAuthenticatedGet("https://example.test", Map.class));
    }

    @Test
    void preservesInterruption() throws Exception {
        when(request.send()).thenThrow(new InterruptedException());
        try {
            assertThrows(EvohomeApiClientException.class,
                    () -> api.doAuthenticatedGet("https://example.test", Map.class));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void honorsNumericRetryAfterAcrossReadsAndWrites() throws Exception {
        when(response.getStatus()).thenReturn(429);
        headers.put("Retry-After", "120");
        EvohomeApiClientException first = assertThrows(EvohomeApiClientException.class,
                () -> api.doAuthenticatedGet("https://example.test", Map.class));
        assertEquals(120, first.getRetryAfterSeconds());
        assertThrows(EvohomeApiClientException.class, () -> api.doAuthenticatedPut("https://example.test", Map.of()));
        verify(request, times(1)).send();
    }

    @Test
    void honorsHttpDateRetryAfter() {
        when(response.getStatus()).thenReturn(503);
        headers.put("Retry-After",
                ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(5).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        EvohomeApiClientException error = assertThrows(EvohomeApiClientException.class,
                () -> api.doAuthenticatedGet("https://example.test", Map.class));
        assertTrue(error.getRetryAfterSeconds() >= 290);
        assertTrue(error.getRetryAfterSeconds() <= 301);
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid", "-1", "9223372036854775807" })
    void malformedRetryAfterStillUsesBackoff(String value) {
        when(response.getStatus()).thenReturn(429);
        headers.put("Retry-After", value);
        EvohomeApiClientException error = assertThrows(EvohomeApiClientException.class,
                () -> api.doRequest(HttpMethod.GET, "https://example.test", Map.of(), null, "", Map.class));
        assertEquals(60, error.getRetryAfterSeconds());
    }
}
