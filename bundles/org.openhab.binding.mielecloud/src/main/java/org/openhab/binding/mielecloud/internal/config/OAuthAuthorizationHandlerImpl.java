/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.config.exception.OngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.webservice.DefaultMieleWebservice;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.ThingUID;

/**
 * {@link OAuthAuthorizationHandler} implementation handling the OAuth 2 authorization via openHAB services.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class OAuthAuthorizationHandlerImpl implements OAuthAuthorizationHandler {
    private static final String TOKEN_URL = DefaultMieleWebservice.THIRD_PARTY_ENDPOINTS_BASENAME + "/token";
    private static final String AUTHORIZATION_URL = DefaultMieleWebservice.THIRD_PARTY_ENDPOINTS_BASENAME + "/login";

    private static final long AUTHORIZATION_TIMEOUT_IN_MINUTES = 5;

    private final OAuthFactory oauthFactory;
    private final ScheduledExecutorService scheduler;

    @Nullable
    private OAuthClientService oauthClientService;
    @Nullable
    private ThingUID bridgeUid;
    @Nullable
    private String email;
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
    public synchronized void beginAuthorization(String clientId, String clientSecret, ThingUID bridgeUid,
            String email) {
        if (this.oauthClientService != null) {
            throw new OngoingAuthorizationException("There is already an ongoing authorization!", timerExpiryTimestamp);
        }

        this.oauthClientService = oauthFactory.createOAuthClientService(email, TOKEN_URL, AUTHORIZATION_URL, clientId,
                clientSecret, null, false);
        this.bridgeUid = bridgeUid;
        this.email = email;
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
            return oauthClientService.getAuthorizationUrl(redirectUri, null, null);
        } catch (org.openhab.core.auth.client.oauth2.OAuthException e) {
            abortTimer();
            cancelAuthorization();
            throw new OAuthException("Failed to determine authorization URL: " + e.getMessage(), e);
        }
    }

    @Override
    public ThingUID getBridgeUid() {
        final ThingUID bridgeUid = this.bridgeUid;
        if (bridgeUid == null) {
            throw new NoOngoingAuthorizationException("There is no ongoing authorization.");
        }
        return bridgeUid;
    }

    @Override
    public String getEmail() {
        final String email = this.email;
        if (email == null) {
            throw new NoOngoingAuthorizationException("There is no ongoing authorization.");
        }
        return email;
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
            throw new OAuthException("Error while processing Miele service response: " + e.getMessage(), e);
        } finally {
            this.oauthClientService = null;
            this.bridgeUid = null;
            this.email = null;
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
        bridgeUid = null;
        email = null;
        redirectUri = null;
        final ScheduledFuture<?> timer = this.timer;
        if (timer != null) {
            timer.cancel(false);
            this.timer = null;
            timerExpiryTimestamp = null;
        }
    }

    @Override
    public String getAccessToken(String email) {
        OAuthClientService clientService = oauthFactory.getOAuthClientService(email);
        if (clientService == null) {
            throw new OAuthException("There is no access token registered for '" + email + "'");
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
