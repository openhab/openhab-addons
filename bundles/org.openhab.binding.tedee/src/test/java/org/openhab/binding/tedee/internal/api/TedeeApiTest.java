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

package org.openhab.binding.tedee.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the local Tedee API client.
 *
 * @author Alex Goll - Initial contribution
 */
class TedeeApiTest {

    private static final int DEVICE_ID = 12345;
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final HttpClient client = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ContentResponse response = mock(ContentResponse.class);
    private final TedeeApi api = new TedeeApi(client, "192.0.2.10", 80, "token");

    @Test
    void getLockUsesRequestTimeoutAndParsesResponse()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"id\":12345,\"name\":\"Front door\"}");

        TedeeLock lock = api.getLock(DEVICE_ID);

        assertEquals(DEVICE_ID, lock.id);
        assertEquals("Front door", lock.name);
        verify(client).newRequest("http://192.0.2.10:80/v1.0/lock/12345");
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        verify(request).header("Accept", "application/json");
    }

    @Test
    void getLockRetriesTransportFailure()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenThrow(new ExecutionException(new EOFException("closed"))).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"id\":12345}");

        TedeeLock lock = api.getLock(DEVICE_ID);

        assertEquals(DEVICE_ID, lock.id);
        verify(client, times(2)).newRequest("http://192.0.2.10:80/v1.0/lock/12345");
    }

    @Test
    void lockUsesRequestTimeoutAndSendsCommand()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        api.lock(DEVICE_ID);

        verify(client).newRequest("http://192.0.2.10:80/v1.0/lock/12345/lock");
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        verify(request).header(eq("Content-Length"), eq("0"));
    }

    @Test
    @SuppressWarnings("null")
    void addCallbackIncludesWebhookSecretHeader()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"id\":1}");

        api.addCallback("http://192.0.2.20:8080/tedee/webhook", "secret");

        ArgumentCaptor<ContentProvider> contentCaptor = ArgumentCaptor.forClass(ContentProvider.class);
        verify(request).content(contentCaptor.capture());
        ByteBuffer content = contentCaptor.getValue().iterator().next();
        JsonObject callback = JsonParser.parseString(StandardCharsets.UTF_8.decode(content).toString())
                .getAsJsonObject();
        JsonObject header = callback.getAsJsonArray("headers").get(0).getAsJsonObject();

        assertEquals(TedeeBindingConstants.WEBHOOK_SECRET_HEADER, header.get("header_name").getAsString());
        assertEquals("secret", header.get("header_value").getAsString());
        assertTrue(callback.get("headers").isJsonArray());
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        verify(request).header("Content-Type", "application/json");
    }

    @Test
    void updateCallbackUsesPutAndTimeout()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        api.updateCallback("1", "http://192.0.2.20:8080/tedee/webhook", "secret");

        verify(client).newRequest("http://192.0.2.10:80/v1.0/callback/1");
        verify(request).method(HttpMethod.PUT);
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
