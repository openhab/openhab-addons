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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.EOFException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeMigrationService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests the renewal of the access token in {@link TeslaAccountHandler}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class TeslaAccountHandlerTest {

    private static final String TOKEN_RESPONSE = "{\"access_token\":\"new-access-token\",\"expires_in\":28800}";

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ContentResponse response;
    private @Mock @NonNullByDefault({}) ThingTypeMigrationService thingTypeMigrationService;
    private @NonNullByDefault({}) Request request;
    private @NonNullByDefault({}) TeslaAccountHandler handler;

    @BeforeEach
    public void setUp() {
        request = mock(Request.class, RETURNS_SELF);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(bridge.getUID()).thenReturn(new ThingUID(THING_TYPE_ACCOUNT, "account"));
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of(CONFIG_REFRESHTOKEN, "refresh-token")));

        handler = new TeslaAccountHandler(bridge, mock(Client.class, RETURNS_DEEP_STUBS), httpClientFactory,
                thingTypeMigrationService);
        handler.setCallback(callback);
    }

    @Test
    public void failedRenewalIsRetriableCommunicationError() throws Exception {
        when(request.send()).thenThrow(new ExecutionException(new EOFException("connection closed")));

        handler.reauthenticate();

        verify(callback).statusUpdated(eq(bridge),
                argThat(status -> hasStatus(status, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR)));
        assertNull(handler.getAuthHeader());
    }

    @Test
    public void renewalSucceedsAfterFailedAttempt() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(TOKEN_RESPONSE);
        when(request.send()).thenThrow(new ExecutionException(new EOFException("connection closed")))
                .thenReturn(response);

        handler.reauthenticate();
        handler.reauthenticate();

        verify(callback).statusUpdated(eq(bridge),
                argThat(status -> hasStatus(status, ThingStatus.ONLINE, ThingStatusDetail.NONE)));
        assertEquals("Bearer new-access-token", handler.getAuthHeader());
    }

    @Test
    public void rejectedRefreshTokenIsConfigurationError() throws Exception {
        when(response.getStatus()).thenReturn(401);
        when(response.getContentAsString()).thenReturn("{\"error\":\"login_required\"}");
        when(request.send()).thenReturn(response);

        handler.reauthenticate();

        verify(callback).statusUpdated(eq(bridge),
                argThat(status -> hasStatus(status, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR)));
        assertNull(handler.getAuthHeader());
    }

    private static boolean hasStatus(ThingStatusInfo info, ThingStatus status, ThingStatusDetail detail) {
        return info.getStatus() == status && info.getStatusDetail() == detail;
    }
}
