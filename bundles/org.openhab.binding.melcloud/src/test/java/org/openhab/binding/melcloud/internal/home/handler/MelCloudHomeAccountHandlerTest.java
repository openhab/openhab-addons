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
package org.openhab.binding.melcloud.internal.home.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.THING_TYPE_MELCLOUD_HOME_ACCOUNT;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudHomeAuthException;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeTokenResponse;
import org.openhab.binding.melcloud.internal.home.api.dto.MelCloudHomeUserContext;
import org.openhab.binding.melcloud.internal.mock.CallbackMock;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Unit tests for {@link MelCloudHomeAccountHandler}: authentication outcomes and unit-missing notifications.
 *
 * <p>
 * {@code @SuppressWarnings("null")}: Mockito is not designed with null type annotations in mind, so combining it with
 * this {@code @NonNullByDefault} test class produces "unsafe interpretation" compiler advisories with no null-safety
 * benefit.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
class MelCloudHomeAccountHandlerTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    private final CallbackMock callback = new CallbackMock();

    private MelCloudHomeAuthService authService = mock(MelCloudHomeAuthService.class);
    private MelCloudHomeApiClient apiClient = mock(MelCloudHomeApiClient.class);
    private StorageService storageService = mock(StorageService.class);
    private Storage<MelCloudHomeAuthState> storage = mock(Storage.class);
    private WebSocketFactory webSocketFactory = mock(WebSocketFactory.class);

    private MelCloudHomeAccountHandler handler = createHandler();

    @BeforeEach
    void setUp() {
        authService = mock(MelCloudHomeAuthService.class);
        apiClient = mock(MelCloudHomeApiClient.class);
        storageService = mock(StorageService.class);
        storage = mock(Storage.class);
        webSocketFactory = mock(WebSocketFactory.class);
        when(storageService.getStorage(anyString(), any(ClassLoader.class))).thenReturn((Storage) storage);
        handler = createHandler();
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    private MelCloudHomeAccountHandler createHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_MELCLOUD_HOME_ACCOUNT, "test")
                .withConfiguration(new Configuration(
                        Map.of("username", "user@example.com", "password", "secret", "enableRealtimeUpdates", false)))
                .build();
        MelCloudHomeAccountHandler created = new MelCloudHomeAccountHandler(bridge, authService, apiClient,
                storageService, webSocketFactory);
        created.setCallback(callback);
        return created;
    }

    private static MelCloudHomeTokenResponse tokenResponse() {
        MelCloudHomeTokenResponse response = new MelCloudHomeTokenResponse();
        response.accessToken = ACCESS_TOKEN;
        response.refreshToken = REFRESH_TOKEN;
        response.expiresIn = 3600;
        return response;
    }

    @Test
    void whenLoginSucceedsThenBridgeGoesOnlineAndRefreshTokenIsPersisted() throws Exception {
        // Arrange
        when(authService.login("user@example.com", "secret")).thenReturn(tokenResponse());

        // Act
        handler.initialize();

        // Assert
        callback.waitForOnline();
        verify(storage).put(anyString(), eq(new MelCloudHomeAuthState(REFRESH_TOKEN)));
        assertEquals(ACCESS_TOKEN, handler.getAccessToken());
    }

    @Test
    void whenLoginIsRejectedThenBridgeShowsConfigurationErrorAndHasNoToken() throws Exception {
        // Arrange
        when(authService.login(anyString(), anyString()))
                .thenThrow(new MelCloudHomeAuthException("Invalid credentials"));

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, callback.getStatus().getStatusDetail());
        assertThrows(MelCloudCommException.class, handler::getAccessToken);
    }

    @Test
    void whenLoginFailsTransientlyThenBridgeShowsCommunicationError() throws Exception {
        // Arrange
        when(authService.login(anyString(), anyString())).thenThrow(new MelCloudCommException("Service unavailable"));

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, callback.getStatus().getStatusDetail());
    }

    @Test
    void whenStoredRefreshTokenFailsTransientlyThenNoFullLoginIsAttempted() throws Exception {
        // Arrange
        when(storage.get(anyString())).thenReturn(new MelCloudHomeAuthState(REFRESH_TOKEN));
        when(authService.refreshToken(REFRESH_TOKEN)).thenThrow(new MelCloudCommException("Timeout"));

        // Act
        handler.initialize();

        // Assert
        callback.waitForStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, callback.getStatus().getStatusDetail());
        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    void whenStoredRefreshTokenIsRejectedThenFullLoginIsUsed() throws Exception {
        // Arrange
        when(storage.get(anyString())).thenReturn(new MelCloudHomeAuthState(REFRESH_TOKEN));
        when(authService.refreshToken(REFRESH_TOKEN)).thenThrow(new MelCloudHomeAuthException("expired"));
        when(authService.login("user@example.com", "secret")).thenReturn(tokenResponse());

        // Act
        handler.initialize();

        // Assert
        callback.waitForOnline();
        verify(authService).login("user@example.com", "secret");
    }

    @Test
    void whenRegisteredUnitIsNotInContextThenListenerIsToldItIsMissing() throws Exception {
        // Arrange
        MelCloudHomeAtaUnitListener listener = mock(MelCloudHomeAtaUnitListener.class);
        when(authService.login(anyString(), anyString())).thenReturn(tokenResponse());
        when(apiClient.fetchUserContext(ACCESS_TOKEN)).thenReturn(new MelCloudHomeUserContext());
        handler.registerAtaUnitListener("unit-1", listener);

        // Act
        handler.initialize();

        // Assert
        verify(listener, timeout(5000)).onAtaUnitMissing();
    }
}
