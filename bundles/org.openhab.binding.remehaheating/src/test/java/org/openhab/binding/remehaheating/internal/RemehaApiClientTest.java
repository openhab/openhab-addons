/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link RemehaApiClient}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class RemehaApiClientTest {

    @Mock
    private HttpClientFactory httpClientFactory;
    @Mock
    private HttpClient httpClient;
    @Mock
    private Request request;
    @Mock
    private ContentResponse response;
    private RemehaApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new RemehaApiClient(httpClient);
    }

    @Test
    public void testConstructor() {
        assertNotNull(apiClient);
        verify(httpClient).setRequestBufferSize(16384);
        verify(httpClient).setResponseBufferSize(16384);
    }

    @Test
    public void testGetDashboardWithoutToken() {
        JsonObject result = apiClient.getDashboard();
        assertNull(result);
    }

    @Test
    public void testSetTemperature() {
        boolean result = apiClient.setTemperature("zone123", 21.5);
        assertFalse(result); // Should return false without access token
    }

    @Test
    public void testSetDhwMode() {
        boolean result = apiClient.setDhwMode("zone456", "schedule");
        assertFalse(result); // Should return false without access token
    }
}
