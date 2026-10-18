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
package org.openhab.binding.tesla.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tesla.internal.protocol.dto.sso.TokenResponse;

/**
 * Tests for {@link TeslaSSOHandler}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class TeslaSSOHandlerTest {

    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ContentResponse response;
    private @NonNullByDefault({}) Request request;
    private @NonNullByDefault({}) TeslaSSOHandler ssoHandler;

    @BeforeEach
    public void setUp() {
        request = mock(Request.class, RETURNS_SELF);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        ssoHandler = new TeslaSSOHandler(httpClient);
    }

    @Test
    public void returnsAccessToken() throws Exception {
        respondWith(200, "{\"access_token\":\"new-access-token\",\"expires_in\":28800}");

        TokenResponse token = ssoHandler.getAccessToken("refresh-token");

        assertNotNull(token);
        assertEquals("new-access-token", token.accessToken);
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403 })
    public void returnsNullIfRefreshTokenIsRejected(int status) throws Exception {
        respondWith(status, "{\"error\":\"invalid_grant\"}");

        assertNull(ssoHandler.getAccessToken("refresh-token"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 429, 500, 503 })
    public void throwsIfServiceIsUnavailable(int status) throws Exception {
        respondWith(status, "");

        assertThrows(IOException.class, () -> ssoHandler.getAccessToken("refresh-token"));
    }

    @Test
    public void throwsIfResponseContainsNoAccessToken() throws Exception {
        respondWith(200, "{}");

        assertThrows(IOException.class, () -> ssoHandler.getAccessToken("refresh-token"));
    }

    @Test
    public void throwsIfRequestFails() throws Exception {
        when(request.send()).thenThrow(new ExecutionException(new EOFException("connection closed")));

        assertThrows(IOException.class, () -> ssoHandler.getAccessToken("refresh-token"));
    }

    private void respondWith(int status, String content) throws Exception {
        when(response.getStatus()).thenReturn(status);
        when(response.getContentAsString()).thenReturn(content);
        when(request.send()).thenReturn(response);
    }
}
