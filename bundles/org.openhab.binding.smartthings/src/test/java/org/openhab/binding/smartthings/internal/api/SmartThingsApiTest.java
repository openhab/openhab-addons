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
package org.openhab.binding.smartthings.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

import com.google.gson.JsonObject;

/**
 * Tests for {@link SmartThingsApi}.
 */
@NonNullByDefault
class SmartThingsApiTest {

    @Test
    void createAppUsesBrowserReachableOAuthRedirectUri() {
        AppRequest request = SmartThingsApi.createAppRequest("openhab123",
                "https://openhab.example.org/smartthings/account/cb",
                "https://openhab.example.org/smartthings/account");

        assertEquals("https://openhab.example.org/smartthings/account", request.oauth.redirectUris[0]);
        assertEquals("https://openhab.example.org/smartthings/account/cb", request.apiOnly.targetUrl());
    }

    @Test
    void createAppOmitsEventTargetUrlWhenCallbackUriIsNotHttps() {
        AppRequest request = SmartThingsApi.createAppRequest("openhab123",
                "http://openhab.example.org/smartthings/account/cb?return=https://example.org",
                "http://localhost:61973/finish");

        assertEquals("http://localhost:61973/finish", request.oauth.redirectUris[0]);
        assertNull(request.apiOnly);
    }

    @Test
    void updateAppUsesPutWithCallbackAndOauthRedirectUri() throws SmartThingsException {
        SmartThingsBridgeHandler bridgeHandler = mock(SmartThingsBridgeHandler.class);
        AccessTokenResponse tokenResponse = mock(AccessTokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("token");
        when(bridgeHandler.getAccessTokenResponse()).thenReturn(tokenResponse);

        SmartThingsNetworkConnector networkConnector = mock(SmartThingsNetworkConnector.class);
        when(networkConnector.doRequest(eq(JsonObject.class), eq("https://api.smartthings.com/v1/apps/app-123"),
                isNull(), eq("token"), org.mockito.ArgumentMatchers.any(), eq(HttpMethod.PUT)))
                .thenReturn(new JsonObject());

        SmartThingsApi api = new SmartThingsApi(mock(HttpClientFactory.class), bridgeHandler, networkConnector,
                mock(ClientBuilder.class), mock(SseEventSourceFactory.class));

        api.updateApp("app-123", "openhab123", "https://openhab.example.org/smartthings/account/cb",
                "https://openhab.example.org/smartthings/account");

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(networkConnector).doRequest(eq(JsonObject.class), eq("https://api.smartthings.com/v1/apps/app-123"),
                isNull(), eq("token"), body.capture(), eq(HttpMethod.PUT));
        assertTrue(body.getValue().contains("\"targetUrl\":\"https://openhab.example.org/smartthings/account/cb\""));
        assertTrue(body.getValue().contains("\"redirectUris\":[\"https://openhab.example.org/smartthings/account\"]"));
    }

    @Test
    void sendCommandExceptionIncludesSmartThingsResponseError() throws SmartThingsException {
        SmartThingsBridgeHandler bridgeHandler = mock(SmartThingsBridgeHandler.class);
        AccessTokenResponse tokenResponse = mock(AccessTokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("token");
        when(bridgeHandler.getAccessTokenResponse()).thenReturn(tokenResponse);

        SmartThingsNetworkConnector networkConnector = mock(SmartThingsNetworkConnector.class);
        when(networkConnector.doRequest(eq(JsonObject.class),
                eq("https://api.smartthings.com/v1/devices/device-123/commands"), isNull(), eq("token"),
                eq("{\"commands\":[]}"), eq(HttpMethod.POST)))
                .thenThrow(new SmartThingsException("Unexpected return code: 422"));

        SmartThingsApi api = new SmartThingsApi(mock(HttpClientFactory.class), bridgeHandler, networkConnector,
                mock(ClientBuilder.class), mock(SseEventSourceFactory.class));

        SmartThingsException exception = assertThrows(SmartThingsException.class,
                () -> api.sendCommand("device-123", "{\"commands\":[]}"));

        assertTrue(exception.getMessage().contains("Unexpected return code: 422"));
    }
}
