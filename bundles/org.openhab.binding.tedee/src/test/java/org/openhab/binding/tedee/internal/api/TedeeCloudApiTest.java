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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;

/**
 * Tests the Tedee Cloud API client.
 *
 * @author Alex Goll - Initial contribution
 */
class TedeeCloudApiTest {

    private static final int DEVICE_ID = 12345;
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    private final HttpClient client = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ContentResponse response = mock(ContentResponse.class);
    private final TedeeCloudApi api = new TedeeCloudApi(client, "personal-key");

    @Test
    void getLockUsesRequestTimeoutAndParsesResponse()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {
                  "result": {
                    "id": 12345,
                    "name": "Front door",
                    "serialNumber": "ABC123",
                    "deviceState": {
                      "state": 2,
                      "batteryLevel": 87
                    }
                  }
                }
                """);

        TedeeLock lock = api.getLock(DEVICE_ID);

        assertEquals(DEVICE_ID, lock.id);
        assertEquals("Front door", lock.name);
        assertEquals(87, lock.batteryLevel);
        verify(client).newRequest("https://api.tedee.com/api/v37/my/lock/12345");
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void unlockUsesRequestTimeoutAndSendsCommand()
            throws TedeeApiException, InterruptedException, ExecutionException, TimeoutException {
        when(client.newRequest(anyString())).thenReturn(request);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        api.unlock(DEVICE_ID);

        verify(client).newRequest("https://api.tedee.com/api/v37/my/lock/12345/operation/unlock");
        verify(request).timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
