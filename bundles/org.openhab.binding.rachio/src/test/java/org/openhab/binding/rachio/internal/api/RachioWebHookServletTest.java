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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;

/**
 * Tests servlet-level Rachio webhook duplicate handling.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioWebHookServletTest {
    private static final String VALID_SIGNATURE = "valid-signature";
    private static final String INVALID_SIGNATURE = "invalid-signature";

    @Test
    void duplicateWebhookEventIsAcknowledgedButRoutedOnce() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse firstResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), firstResponse);
        HttpServletResponse duplicateResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), duplicateResponse);

        verify(handlerFactory, times(1)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(firstResponse).setStatus(HttpServletResponse.SC_OK);
        verify(duplicateResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void failedWebhookProcessingLeavesEventRetryable() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class)))
                .thenThrow(new RuntimeException("simulated failure")).thenReturn(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse failedResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), failedResponse);
        HttpServletResponse retryResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), retryResponse);

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(failedResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(retryResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void unroutedWebhookProcessingLeavesEventRetryable() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class))).thenReturn(false, true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("abc");

        HttpServletResponse failedResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), failedResponse);
        HttpServletResponse retryResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), retryResponse);

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
        verify(failedResponse).setStatus(HttpServletResponse.SC_OK);
        verify(retryResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void webhookEventWithoutEventIdIsAlwaysRouted() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJsonWithoutEventId();

        servlet.service(request(body, VALID_SIGNATURE), response());
        servlet.service(request(body, VALID_SIGNATURE), response());

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void differentWebhookEventIdsAreRoutedNormally() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(true);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        servlet.service(request(eventJson("event-1"), VALID_SIGNATURE), response());
        servlet.service(request(eventJson("event-2"), VALID_SIGNATURE), response());

        verify(handlerFactory, times(2)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void invalidSignatureDoesNotPoisonDuplicateCache() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        when(handlerFactory.isValidWebHookSignature(anyString(), any(byte[].class))).thenReturn(false, true);
        RachioWebHookServlet servlet = servlet(handlerFactory);
        String body = eventJson("event-1");

        HttpServletResponse invalidResponse = response();
        servlet.service(request(body, INVALID_SIGNATURE), invalidResponse);
        HttpServletResponse validResponse = response();
        servlet.service(request(body, VALID_SIGNATURE), validResponse);

        verify(invalidResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verify(validResponse).setStatus(HttpServletResponse.SC_OK);
        verify(handlerFactory, times(1)).webHookEvent(eq("127.0.0.1"), any(RachioEventGsonDTO.class));
    }

    @Test
    void duplicateDetectionRunsAfterSignatureValidation() throws Exception {
        RachioHandlerFactory handlerFactory = mockHandlerFactory(false);
        RachioWebHookServlet servlet = servlet(handlerFactory);

        servlet.service(request(eventJson("event-1"), INVALID_SIGNATURE), response());

        verify(handlerFactory, never()).webHookEvent(anyString(), any(RachioEventGsonDTO.class));
    }

    private RachioWebHookServlet servlet(RachioHandlerFactory handlerFactory) throws Exception {
        return new RachioWebHookServlet(handlerFactory);
    }

    private RachioHandlerFactory mockHandlerFactory(boolean validSignature) {
        RachioHandlerFactory handlerFactory = Mockito.mock(RachioHandlerFactory.class);
        when(handlerFactory.isValidWebHookSignature(anyString(), any(byte[].class))).thenReturn(validSignature);
        when(handlerFactory.webHookEvent(anyString(), any(RachioEventGsonDTO.class))).thenReturn(true);
        return handlerFactory;
    }

    private HttpServletRequest request(String body, String signature) throws IOException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(SERVLET_WEBHOOK_PATH);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(443);
        when(request.getRemoteHost()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8443);
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getHeader("x-signature")).thenReturn(signature);
        when(request.getInputStream())
                .thenReturn(new ByteArrayServletInputStream(body.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    private HttpServletResponse response() throws IOException {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        return response;
    }

    private String eventJson(String eventId) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "DEVICE_ZONE_RUN_STARTED_EVENT",
                  "resourceType": "IRRIGATION_CONTROLLER",
                  "resourceId": "controller-id",
                  "externalId": "external-id",
                  "payload": {
                    "zoneNumber": "1",
                    "zoneName": "Front Yard",
                    "durationSeconds": "60"
                  }
                }
                """.formatted(eventId);
    }

    private String eventJsonWithoutEventId() {
        return """
                {
                  "eventType": "DEVICE_ZONE_RUN_STARTED_EVENT",
                  "resourceType": "IRRIGATION_CONTROLLER",
                  "resourceId": "controller-id",
                  "externalId": "external-id",
                  "payload": {
                    "zoneNumber": "1",
                    "zoneName": "Front Yard",
                    "durationSeconds": "60"
                  }
                }
                """;
    }

    private static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        ByteArrayServletInputStream(byte[] body) {
            inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(@Nullable ReadListener readListener) {
        }
    }
}
