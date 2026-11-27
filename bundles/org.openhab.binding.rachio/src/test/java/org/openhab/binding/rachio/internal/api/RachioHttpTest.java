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
package org.openhab.binding.rachio.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests the Jetty-based Rachio HTTP transport.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioHttpTest {
    private HttpClient httpClient = Mockito.mock(HttpClient.class);
    private Request request = Mockito.mock(Request.class);
    private ContentResponse response = Mockito.mock(ContentResponse.class);
    private HttpFields.Mutable responseHeaders = HttpFields.build();

    @BeforeEach
    void setUp() throws Exception {
        httpClient = Mockito.mock(HttpClient.class);
        request = Mockito.mock(Request.class);
        response = Mockito.mock(ContentResponse.class);
        responseHeaders = HttpFields.build();

        when(httpClient.newRequest(Mockito.any(URI.class))).thenReturn(request);
        when(request.method(Mockito.anyString())).thenReturn(request);
        when(request.timeout(Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(request);
        when(request.headers(Mockito.any())).thenReturn(request);
        when(request.body(Mockito.any(Request.Content.class))).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(response.getContent()).thenReturn("{}".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void getUsesSharedClientAndPreservesResponseMetadata() throws Exception {
        responseHeaders.put(RACHIO_JSON_RATE_LIMIT, "1700");
        responseHeaders.put(RACHIO_JSON_RATE_REMAINING, "1699");
        responseHeaders.put(RACHIO_JSON_RATE_RESET, "2026-08-09T00:00:00Z");
        when(response.getContent()).thenReturn("{\"name\":\"El\u0151kert\"}".getBytes(StandardCharsets.UTF_8));
        RachioHttp http = new RachioHttp(httpClient, "api-key");

        RachioApiResult result = http.httpGet("https://api.example.test/device", "id=123");

        verify(httpClient).newRequest(URI.create("https://api.example.test/device?id=123"));
        verify(request).method("GET");
        verify(request).timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        HttpFields sentHeaders = headersOf(request);
        assertEquals("Bearer api-key", sentHeaders.get(HttpHeader.AUTHORIZATION));
        assertEquals(SERVLET_WEBHOOK_USER_AGENT, sentHeaders.get(HttpHeader.USER_AGENT));
        assertEquals(SERVLET_WEBHOOK_APPLICATION_JSON, sentHeaders.get(HttpHeader.CONTENT_TYPE));
        assertEquals(HttpStatus.OK_200, result.responseCode);
        assertEquals("{\"name\":\"El\u0151kert\"}", result.resultString);
        assertEquals(1, result.apiCalls);
        assertEquals(1700, result.rateLimit);
        assertEquals(1699, result.rateRemaining);
        assertEquals("2026-08-09T00:00:00Z", result.rateReset);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT" })
    void bodyRequestSendsLengthKnownUtf8JsonWithSingleContentType(String method) throws Exception {
        String body = "{\"name\":\"El\u0151kert\"}";
        RachioHttp http = new RachioHttp(httpClient, "");

        if ("POST".equals(method)) {
            http.httpPost("https://api.example.test/zone", body);
        } else {
            http.httpPut("https://api.example.test/zone", body);
        }

        verify(request).method(method);
        // the content type must come from the body alone, not also as a header
        assertNull(headersOf(request).get(HttpHeader.CONTENT_TYPE));
        ArgumentCaptor<Request.Content> contentCaptor = ArgumentCaptor.captor();
        verify(request).body(contentCaptor.capture());
        Request.Content sentBody = contentCaptor.getValue();
        assertEquals(SERVLET_WEBHOOK_APPLICATION_JSON, sentBody.getContentType());
        assertEquals(body.getBytes(StandardCharsets.UTF_8).length, sentBody.getLength());
        assertEquals(body, Content.Source.asString(sentBody, StandardCharsets.UTF_8));
    }

    @Test
    void nonSuccessResponsePreservesStatusWithoutRetainingResponseBody() throws Exception {
        String responseBody = "{\"error\":\"secret detail\"}";
        when(response.getStatus()).thenReturn(HttpStatus.TOO_MANY_REQUESTS_429);
        when(response.getContent()).thenReturn(responseBody.getBytes(StandardCharsets.UTF_8));
        RachioHttp http = new RachioHttp(httpClient, "api-key");

        RachioApiException exception = assertThrows(RachioApiException.class,
                () -> http.httpDelete("https://api.example.test/webhook", "id=123"));

        RachioApiResult result = exception.getApiResult();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS_429, result.responseCode);
        assertEquals("responseLength=" + responseBody.length(), result.resultString);
        assertTrue(result.isResponseRateLimit());
        assertTrue(exception.getMessage().contains("responseLength=" + responseBody.length()));
    }

    @Test
    void exhaustedRateLimitStopsSuccessfulResponseProcessing() throws Exception {
        responseHeaders.put(RACHIO_JSON_RATE_LIMIT, "1700");
        responseHeaders.put(RACHIO_JSON_RATE_REMAINING, "0");
        responseHeaders.put(RACHIO_JSON_RATE_RESET, "2026-08-09T00:00:00Z");
        RachioHttp http = new RachioHttp(httpClient, "api-key");

        RachioApiException exception = assertThrows(RachioApiException.class,
                () -> http.httpGet("https://api.example.test/device", null));

        assertEquals(HttpStatus.OK_200, exception.getApiResult().responseCode);
        assertTrue(exception.getApiResult().isRateLimitBlocked());
    }

    @Test
    void interruptedRequestRestoresInterruptFlag() throws Exception {
        InterruptedException interrupted = new InterruptedException("cancelled");
        when(request.send()).thenThrow(interrupted);
        RachioHttp http = new RachioHttp(httpClient, "api-key");

        try {
            RachioApiException exception = assertThrows(RachioApiException.class,
                    () -> http.httpGet("https://api.example.test/device", null));

            assertSame(interrupted, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void executionFailureExposesUnderlyingNetworkCause() throws Exception {
        UnknownHostException unknownHost = new UnknownHostException("api.example.test");
        when(request.send()).thenThrow(new ExecutionException(unknownHost));
        RachioHttp http = new RachioHttp(httpClient, "api-key");

        RachioApiException exception = assertThrows(RachioApiException.class,
                () -> http.httpGet("https://api.example.test/device", null));

        assertSame(unknownHost, exception.getCause());
        assertInstanceOf(UnknownHostException.class, exception.getCause());
    }

    /** Replays the header blocks passed to {@link Request#headers} so the resulting fields can be asserted on. */
    private static HttpFields headersOf(Request request) {
        ArgumentCaptor<Consumer<HttpFields.Mutable>> captor = ArgumentCaptor.captor();
        verify(request, atLeastOnce()).headers(captor.capture());
        HttpFields.Mutable headers = HttpFields.build();
        captor.getAllValues().forEach(block -> block.accept(headers));
        return headers;
    }
}
