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
package org.openhab.binding.mielecloud.internal.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles refreshing of OAuth2 tokens managed by the openHAB runtime.
 *
 * @author Björn Lange - Initial contribution
 */
@Component
@NonNullByDefault
public final class OpenHabOAuthTokenRefresher implements OAuthTokenRefresher {
    private final Logger logger = LoggerFactory.getLogger(OpenHabOAuthTokenRefresher.class);

    private final OAuthFactory oauthFactory;
    private Map<String, @Nullable AccessTokenRefreshListener> listenerByServiceHandle = new HashMap<>();

    @Activate
    public OpenHabOAuthTokenRefresher(@Reference OAuthFactory oauthFactory) {
        this.oauthFactory = oauthFactory;
    }

    @Override
    public void setRefreshListener(OAuthTokenRefreshListener listener, String serviceHandle) {
        final AccessTokenRefreshListener refreshListener = tokenResponse -> {
            final String accessToken = tokenResponse.getAccessToken();
            if (accessToken == null) {
                // Fail without exception to ensure that the OAuthClientService notifies all listeners.
                logger.warn("Ignoring access token response without access token.");
            } else {
                listener.onNewAccessToken(accessToken);
            }
        };

        OAuthClientService clientService = getOAuthClientService(serviceHandle);
        clientService.addAccessTokenRefreshListener(refreshListener);
        listenerByServiceHandle.put(serviceHandle, refreshListener);
    }

    @Override
    public void unsetRefreshListener(String serviceHandle) {
        final AccessTokenRefreshListener refreshListener = listenerByServiceHandle.get(serviceHandle);
        if (refreshListener != null) {
            try {
                OAuthClientService clientService = getOAuthClientService(serviceHandle);
                clientService.removeAccessTokenRefreshListener(refreshListener);
                oauthFactory.ungetOAuthService(serviceHandle);
            } catch (OAuthException e) {
                logger.warn("Failed to remove refresh listener: OAuth client service is unavailable. Cause: {}",
                        e.getMessage());
            }
        }
        listenerByServiceHandle.remove(serviceHandle);
    }

    @Override
    public void refreshToken(String serviceHandle) {
        if (listenerByServiceHandle.get(serviceHandle) == null) {
            logger.warn("Token refreshing was requested but there is no token refresh listener registered!");
            return;
        }

        OAuthClientService clientService = getOAuthClientService(serviceHandle);
        refreshAccessToken(clientService);
    }

    private OAuthClientService getOAuthClientService(String serviceHandle) {
        final OAuthClientService clientService = oauthFactory.getOAuthClientService(serviceHandle);
        if (clientService == null) {
            throw new OAuthException("OAuth client service is not available.");
        }
        return clientService;
    }

    private void refreshAccessToken(OAuthClientService clientService) {
        try {
            final AccessTokenResponse accessTokenResponse = clientService.refreshToken();
            final String accessToken = accessTokenResponse.getAccessToken();
            if (accessToken == null) {
                throw new OAuthException("Access token is not available.");
            }
        } catch (org.openhab.core.auth.client.oauth2.OAuthException e) {
            throw new OAuthException("An error occurred during token refresh: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OAuthException("A network error occurred during token refresh: " + e.getMessage(), e);
        } catch (OAuthResponseException e) {
            throw new OAuthException("Miele cloud service returned an illegal response: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<String> getAccessTokenFromStorage(String serviceHandle) {
        try {
            AccessTokenResponse tokenResponse = getOAuthClientService(serviceHandle).getAccessTokenResponse();
            if (tokenResponse == null) {
                return Optional.empty();
            } else {
                return Optional.of(tokenResponse.getAccessToken());
            }
        } catch (OAuthException | org.openhab.core.auth.client.oauth2.OAuthException | IOException
                | OAuthResponseException e) {
            logger.debug("Cannot obtain access token from persistent storage.", e);
            return Optional.empty();
        }
    }

    @Override
    public void removeTokensFromStorage(String serviceHandle) {
        oauthFactory.deleteServiceAndAccessToken(serviceHandle);
    }
}
