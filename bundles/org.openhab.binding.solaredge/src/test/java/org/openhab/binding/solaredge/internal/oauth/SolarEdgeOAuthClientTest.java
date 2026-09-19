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
package org.openhab.binding.solaredge.internal.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.core.storage.Storage;

/**
 * Tests SolarEdge OAuth token reuse, refresh, and rotation.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeOAuthClientTest {
    private static final Instant NOW = Instant.parse("2026-08-13T18:00:00Z");

    private final Map<String, String> values = new HashMap<>();
    private final HttpClient httpClient = mock(HttpClient.class);
    private final Request request = mock(Request.class);
    private final ContentResponse response = mock(ContentResponse.class);
    private final Storage<String> storage = createStorage();
    private final SolarEdgeConfiguration config = createConfiguration();
    private final SolarEdgeOAuthClient client = new SolarEdgeOAuthClient(httpClient, storage,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    public void setUpRequest() throws Exception {
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.method(HttpMethod.POST)).thenReturn(request);
        when(request.content(any())).thenReturn(request);
        when(request.send()).thenReturn(response);
    }

    @Test
    public void reusesUnexpiredAccessToken() throws Exception {
        values.put("accessToken", "current-access-token");
        values.put("refreshToken", "current-refresh-token");
        values.put("expiresAt", Long.toString(NOW.plusSeconds(3600).toEpochMilli()));

        assertEquals("current-access-token", client.getAccessToken(config));
        verify(httpClient, never()).newRequest(anyString());
    }

    @Test
    public void refreshesExpiredTokenAndStoresRotation() throws Exception {
        values.put("accessToken", "expired-access-token");
        values.put("refreshToken", "old-refresh-token");
        values.put("expiresAt", Long.toString(NOW.minusSeconds(1).toEpochMilli()));
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {"access_token":"new-access-token","refresh_token":"new-refresh-token",
                 "token_type":"Bearer","expires_in":7200}
                """);

        assertEquals("new-access-token", client.getAccessToken(config));
        assertEquals("new-access-token", values.get("accessToken"));
        assertEquals("new-refresh-token", values.get("refreshToken"));
        assertEquals(Long.toString(NOW.plusSeconds(7200).toEpochMilli()), values.get("expiresAt"));
    }

    @Test
    public void invalidationForcesRefresh() throws Exception {
        values.put("accessToken", "rejected-access-token");
        values.put("refreshToken", "refresh-token");
        values.put("expiresAt", Long.toString(NOW.plusSeconds(3600).toEpochMilli()));
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {"access_token":"replacement-access-token","refresh_token":"replacement-refresh-token",
                 "token_type":"Bearer","expires_in":7200}
                """);

        client.invalidateAccessToken();

        assertEquals("replacement-access-token", client.getAccessToken(config));
    }

    @Test
    public void rejectsIncompleteTokenResponseWithoutReplacingRefreshToken() throws Exception {
        values.put("refreshToken", "existing-refresh-token");
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"access_token\":\"incomplete\"}");

        assertThrows(SolarEdgeOAuthException.class, () -> client.getAccessToken(config));
        assertEquals("existing-refresh-token", values.get("refreshToken"));
    }

    @Test
    public void rejectedRefreshRequiresNewAuthorizationAndClearsTokens() throws Exception {
        values.put("accessToken", "expired-access-token");
        values.put("refreshToken", "rejected-refresh-token");
        values.put("expiresAt", Long.toString(NOW.minusSeconds(1).toEpochMilli()));
        when(response.getStatus()).thenReturn(400);

        SolarEdgeOAuthException exception = assertThrows(SolarEdgeOAuthException.class,
                () -> client.getAccessToken(config));

        assertTrue(exception.isAuthorizationRequired());
        assertTrue(values.isEmpty());
    }

    @Test
    public void temporaryRefreshFailureKeepsAuthorization() throws Exception {
        values.put("refreshToken", "existing-refresh-token");
        when(response.getStatus()).thenReturn(503);

        SolarEdgeOAuthException exception = assertThrows(SolarEdgeOAuthException.class,
                () -> client.getAccessToken(config));

        assertFalse(exception.isAuthorizationRequired());
        assertEquals("existing-refresh-token", values.get("refreshToken"));
    }

    @SuppressWarnings("unchecked")
    private Storage<String> createStorage() {
        Storage<String> result = mock(Storage.class);
        when(result.get(anyString())).thenAnswer(invocation -> values.get(invocation.getArgument(0)));
        when(result.put(anyString(), anyString()))
                .thenAnswer(invocation -> values.put(invocation.getArgument(0), invocation.getArgument(1)));
        when(result.remove(anyString())).thenAnswer(invocation -> values.remove(invocation.getArgument(0)));
        return result;
    }

    private SolarEdgeConfiguration createConfiguration() {
        SolarEdgeConfiguration result = new SolarEdgeConfiguration();
        result.setOAuthClientId("client-id");
        result.setOAuthClientSecret("client-secret");
        return result;
    }
}
