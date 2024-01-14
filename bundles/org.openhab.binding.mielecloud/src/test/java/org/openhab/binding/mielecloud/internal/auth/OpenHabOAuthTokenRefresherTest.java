/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingTestConstants;
import org.openhab.binding.mielecloud.internal.util.ReflectionUtil;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class OpenHabOAuthTokenRefresherTest {
    private static final String ACCESS_TOKEN = "DE_0123456789abcdef0123456789abcdef";

    private boolean hasAccessTokenRefreshListenerForServiceHandle(OpenHabOAuthTokenRefresher refresher,
            String serviceHandle)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return ReflectionUtil
                .<Map<String, @Nullable AccessTokenRefreshListener>> getPrivate(refresher, "listenerByServiceHandle")
                .get(MieleCloudBindingTestConstants.SERVICE_HANDLE) != null;
    }

    private AccessTokenRefreshListener getAccessTokenRefreshListenerByServiceHandle(
            OpenHabOAuthTokenRefresher refresher, String serviceHandle)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        AccessTokenRefreshListener listener = ReflectionUtil
                .<Map<String, @Nullable AccessTokenRefreshListener>> getPrivate(refresher, "listenerByServiceHandle")
                .get(MieleCloudBindingTestConstants.SERVICE_HANDLE);
        assertNotNull(listener);
        return Objects.requireNonNull(listener);
    }

    @Test
    public void whenTheAccountWasNotConfiguredPriorToTheThingInitializingThenNoRefreshListenerCanBeRegistered() {
        // given:
        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);
        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);

        // when:
        assertThrows(OAuthException.class, () -> {
            refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);
        });
    }

    @Test
    public void whenARefreshListenerIsRegisteredThenAListenerIsRegisteredAtTheClientService()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);

        // when:
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        verify(oauthClientService).addAccessTokenRefreshListener(any());
        assertNotNull(
                getAccessTokenRefreshListenerByServiceHandle(refresher, MieleCloudBindingTestConstants.SERVICE_HANDLE));
    }

    @Test
    public void whenTokenIsRefreshedThenTheListenerIsCalledWithTheNewAccessToken()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(ACCESS_TOKEN);

        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);
        when(oauthClientService.refreshToken()).thenAnswer(new Answer<@Nullable AccessTokenResponse>() {
            @Override
            @Nullable
            public AccessTokenResponse answer(@Nullable InvocationOnMock invocation) throws Throwable {
                getAccessTokenRefreshListenerByServiceHandle(refresher, MieleCloudBindingTestConstants.SERVICE_HANDLE)
                        .onAccessTokenResponse(accessTokenResponse);
                return accessTokenResponse;
            }
        });

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        refresher.refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        verify(listener).onNewAccessToken(ACCESS_TOKEN);
    }

    @Test
    public void whenTokenIsRefreshedAndNoAccessTokenIsProvidedThenTheListenerIsNotNotified()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();

        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);
        when(oauthClientService.refreshToken()).thenAnswer(new Answer<@Nullable AccessTokenResponse>() {
            @Override
            @Nullable
            public AccessTokenResponse answer(@Nullable InvocationOnMock invocation) throws Throwable {
                getAccessTokenRefreshListenerByServiceHandle(refresher, MieleCloudBindingTestConstants.SERVICE_HANDLE)
                        .onAccessTokenResponse(accessTokenResponse);
                return accessTokenResponse;
            }
        });

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                refresher.refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
            } catch (OAuthException e) {
                verifyNoInteractions(listener);
                throw e;
            }
        });
    }

    @Test
    public void whenTokenRefreshFailsWithOAuthExceptionThenTheListenerIsNotNotified()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);
        when(oauthClientService.refreshToken()).thenThrow(new org.openhab.core.auth.client.oauth2.OAuthException());

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                refresher.refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
            } catch (OAuthException e) {
                verifyNoInteractions(listener);
                throw e;
            }
        });
    }

    @Test
    public void whenTokenRefreshFailsDueToNetworkErrorThenTheListenerIsNotNotified()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);
        when(oauthClientService.refreshToken()).thenThrow(new IOException());

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                refresher.refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
            } catch (OAuthException e) {
                verifyNoInteractions(listener);
                throw e;
            }
        });
    }

    @Test
    public void whenTokenRefreshFailsDueToAnIllegalResponseThenTheListenerIsNotNotified()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);
        when(oauthClientService.refreshToken()).thenThrow(new OAuthResponseException());

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                refresher.refreshToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
            } catch (OAuthException e) {
                verifyNoInteractions(listener);
                throw e;
            }
        });
    }

    @Test
    public void whenTheRefreshListenerIsUnsetAndWasNotRegisteredBeforeThenNothingHappens()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        // when:
        refresher.unsetRefreshListener(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        assertFalse(hasAccessTokenRefreshListenerForServiceHandle(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE));
    }

    @Test
    public void whenTheRefreshListenerIsUnsetAndTheClientServiceIsNotAvailableThenTheListenerIsCleared()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);

        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        refresher.unsetRefreshListener(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        assertFalse(hasAccessTokenRefreshListenerForServiceHandle(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE));
    }

    @Test
    public void whenTheRefreshListenerIsUnsetThenTheListenerIsClearedAndRemovedFromTheClientService()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);

        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        refresher.unsetRefreshListener(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        verify(oauthClientService).removeAccessTokenRefreshListener(any());
        assertFalse(hasAccessTokenRefreshListenerForServiceHandle(refresher,
                MieleCloudBindingTestConstants.SERVICE_HANDLE));
    }

    @Test
    public void whenTokensAreRemovedThenTheRuntimeIsRequestedToDeleteServiceAndAccessToken()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IOException, OAuthResponseException {
        // given:
        OAuthClientService oauthClientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.getOAuthClientService(MieleCloudBindingTestConstants.SERVICE_HANDLE))
                .thenReturn(oauthClientService);

        OpenHabOAuthTokenRefresher refresher = new OpenHabOAuthTokenRefresher(oauthFactory);

        OAuthTokenRefreshListener listener = mock(OAuthTokenRefreshListener.class);
        refresher.setRefreshListener(listener, MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // when:
        refresher.removeTokensFromStorage(MieleCloudBindingTestConstants.SERVICE_HANDLE);

        // then:
        verify(oauthFactory).deleteServiceAndAccessToken(MieleCloudBindingTestConstants.SERVICE_HANDLE);
    }
}
