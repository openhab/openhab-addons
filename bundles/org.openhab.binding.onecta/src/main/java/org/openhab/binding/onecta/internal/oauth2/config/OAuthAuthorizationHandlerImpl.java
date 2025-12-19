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
package org.openhab.binding.onecta.internal.oauth2.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthException;
import org.openhab.binding.onecta.internal.oauth2.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.onecta.internal.oauth2.config.exception.OngoingAuthorizationException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * {@link OAuthAuthorizationHandler} implementation handling the OAuth 2 authorization via openHAB services.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class OAuthAuthorizationHandlerImpl implements OAuthAuthorizationHandler {
    private static final String TOKEN_URL = OnectaBridgeConstants.THIRD_PARTY_ENDPOINTS_BASENAME + "/token";
    private static final String AUTHORIZATION_URL = OnectaBridgeConstants.THIRD_PARTY_ENDPOINTS_BASENAME + "/authorize";

    private static final long AUTHORIZATION_TIMEOUT_IN_MINUTES = 5;

    private final OAuthFactory oauthFactory;
    private final ScheduledExecutorService scheduler;

    @Nullable
    private OAuthClientService oauthClientService;
    @Nullable
    private String handle;
    @Nullable
    private String redirectUri;
    @Nullable
    private ScheduledFuture<?> timer;
    @Nullable
    private LocalDateTime timerExpiryTimestamp;

    /**
     * Creates a new {@link OAuthAuthorizationHandlerImpl}.
     *
     * @param oauthFactory Factory for accessing the {@link OAuthClientService}.
     * @param scheduler System-wide scheduler.
     */
    public OAuthAuthorizationHandlerImpl(OAuthFactory oauthFactory, ScheduledExecutorService scheduler) {
        this.oauthFactory = oauthFactory;
        this.scheduler = scheduler;
    }

    @Override
    public synchronized void beginAuthorization(String clientId, String clientSecret, String handle) {
        if (this.oauthClientService != null) {
            throw new OngoingAuthorizationException("There is already an ongoing authorization!", timerExpiryTimestamp);
        }

        this.oauthClientService = oauthFactory.createOAuthClientService(handle, TOKEN_URL, AUTHORIZATION_URL, clientId,
                clientSecret, null, false);
        this.handle = handle;
        redirectUri = null;
        timer = null;
        timerExpiryTimestamp = null;
    }

    @Override
    public synchronized String getAuthorizationUrl(String redirectUri) {
        final OAuthClientService oauthClientService = this.oauthClientService;
        if (oauthClientService == null) {
            throw new NoOngoingAuthorizationException("There is no ongoing authorization!");
        }

        this.redirectUri = redirectUri;
        try {
            timer = scheduler.schedule(this::cancelAuthorization, AUTHORIZATION_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
            timerExpiryTimestamp = LocalDateTime.now().plusMinutes(AUTHORIZATION_TIMEOUT_IN_MINUTES);
            return oauthClientService.getAuthorizationUrl(redirectUri, "openid onecta:basic.integration", null);
        } catch (org.openhab.core.auth.client.oauth2.OAuthException e) {
            abortTimer();
            cancelAuthorization();
            throw new OAuthException("Failed to determine authorization URL: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void completeAuthorization(String redirectUrlWithParameters) {
        abortTimer();

        final OAuthClientService oauthClientService = this.oauthClientService;
        if (oauthClientService == null) {
            throw new NoOngoingAuthorizationException("There is no ongoing authorization.");
        }

        try {
            String authorizationCode = oauthClientService.extractAuthCodeFromAuthResponse(redirectUrlWithParameters);

            // Although this method is called "get" it actually fetches and stores the token response as a side effect.
            oauthClientService.getAccessTokenResponseByAuthorizationCode(authorizationCode, redirectUri);
        } catch (IOException e) {
            throw new OAuthException("Network error while retrieving token response: " + e.getMessage(), e);
        } catch (OAuthResponseException e) {
            throw new OAuthException("Failed to retrieve token response: " + e.getMessage(), e);
        } catch (org.openhab.core.auth.client.oauth2.OAuthException e) {
            throw new OAuthException("Error while processing Onecta service response: " + e.getMessage(), e);
        } finally {
            this.oauthClientService = null;
            this.handle = null;
            this.redirectUri = null;
        }
    }

    /**
     * Aborts the timer.
     *
     * Note: All calls to this method must be {@code synchronized} to ensure thread-safety. Also note that
     * {@link #cancelAuthorization()} is {@code synchronized} so the execution of this method and
     * {@link #cancelAuthorization()} cannot overlap. Therefore, this method is an atomic operation from the timer's
     * perspective.
     */
    private void abortTimer() {
        final ScheduledFuture<?> timer = this.timer;
        if (timer == null) {
            return;
        }

        if (!timer.isDone()) {
            timer.cancel(false);
        }
        this.timer = null;
        timerExpiryTimestamp = null;
    }

    private synchronized void cancelAuthorization() {
        oauthClientService = null;
        handle = null;
        redirectUri = null;
        final ScheduledFuture<?> timer = this.timer;
        if (timer != null) {
            timer.cancel(false);
            this.timer = null;
            timerExpiryTimestamp = null;
        }
    }

    @Override
    public String getAccessToken(String handle) {
        OAuthClientService clientService = oauthFactory.getOAuthClientService(handle);
        if (clientService == null) {
            throw new OAuthException("There is no access token registered for '" + handle + "'");
        }

        try {
            AccessTokenResponse response = clientService.getAccessTokenResponse();
            if (response == null) {
                throw new OAuthException(
                        "There is no access token in the persistent storage or it already expired and could not be refreshed");
            } else {
                return response.getAccessToken();
            }
        } catch (org.openhab.core.auth.client.oauth2.OAuthException e) {
            throw new OAuthException("Failed to read access token from persistent storage: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OAuthException(
                    "Network error during token refresh or error while reading from persistent storage: "
                            + e.getMessage(),
                    e);
        } catch (OAuthResponseException e) {
            throw new OAuthException("Failed to retrieve token response: " + e.getMessage(), e);
        }
    }
}
