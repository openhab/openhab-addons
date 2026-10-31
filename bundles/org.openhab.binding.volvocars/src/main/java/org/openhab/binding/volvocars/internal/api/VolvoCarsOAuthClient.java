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
package org.openhab.binding.volvocars.internal.api;

import static org.openhab.binding.volvocars.internal.VolvoCarsBindingConstants.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.volvocars.internal.api.dto.TokenResponse;
import org.openhab.binding.volvocars.internal.config.AccountConfiguration;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * OAuth 2.0 authorization code flow with PKCE (RFC 7636) against Volvo ID.
 * <p>
 * The binding exchanges the authorization code itself, as the core {@link OAuthClientService} cannot send a
 * per-authorization {@code code_verifier} with the token request. The resulting tokens are imported into the
 * {@link OAuthClientService}, which stores them encrypted and refreshes them. The given {@link Storage} only keeps
 * the client id the tokens were obtained with.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsOAuthClient {
    private static final String CLIENT_ID = "clientId";
    private static final Duration PENDING_AUTHORIZATION_VALIDITY = Duration.ofMinutes(15);
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(VolvoCarsOAuthClient.class);
    private final SecureRandom random = new SecureRandom();
    private final Gson gson = new Gson();

    private final HttpClient httpClient;
    private final OAuthClientService oAuthService;
    private final Storage<String> storage;
    private final String storagePrefix;
    private final AccountConfiguration config;
    private final String scopes;
    private final Clock clock;

    private boolean refreshRequired;
    private @Nullable PendingAuthorization pendingAuthorization;

    private record PendingAuthorization(String state, String codeVerifier, Instant expiry) {
    }

    public VolvoCarsOAuthClient(HttpClient httpClient, OAuthClientService oAuthService, Storage<String> storage,
            String storagePrefix, AccountConfiguration config) {
        this(httpClient, oAuthService, storage, storagePrefix, config, Clock.systemUTC());
    }

    VolvoCarsOAuthClient(HttpClient httpClient, OAuthClientService oAuthService, Storage<String> storage,
            String storagePrefix, AccountConfiguration config, Clock clock) {
        this.httpClient = httpClient;
        this.oAuthService = oAuthService;
        this.storage = storage;
        this.storagePrefix = storagePrefix + ".";
        this.config = config;
        this.scopes = config.scopes.isBlank() ? DEFAULT_SCOPES : config.scopes.trim();
        this.clock = clock;
    }

    /**
     * @return true if the account was authorized with the currently configured client id
     */
    public synchronized boolean isAuthorized() {
        String authorizedClientId = storage.get(storagePrefix + CLIENT_ID);
        if (authorizedClientId == null) {
            return false;
        }
        if (!config.clientId.equals(authorizedClientId)) {
            logger.debug("Client id changed since the last authorization, discarding stored tokens");
            clearTokens();
            return false;
        }
        return true;
    }

    /**
     * Starts a new authorization and returns the Volvo ID URL the user has to open. Any previously started but
     * not yet completed authorization becomes invalid.
     */
    public synchronized String createAuthorizationUrl() {
        String codeVerifier = randomUrlSafeString(64);
        String state = randomUrlSafeString(24);
        pendingAuthorization = new PendingAuthorization(state, codeVerifier,
                clock.instant().plus(PENDING_AUTHORIZATION_VALIDITY));
        return AUTHORIZE_URL + "?response_type=code&client_id=" + encode(config.clientId) + "&redirect_uri="
                + encode(config.redirectUri) + "&scope=" + encode(scopes) + "&state=" + encode(state)
                + "&code_challenge=" + encode(computeCodeChallenge(codeVerifier)) + "&code_challenge_method=S256";
    }

    public synchronized boolean isPendingState(String state) {
        PendingAuthorization pending = pendingAuthorization;
        return pending != null && MessageDigest.isEqual(pending.state().getBytes(StandardCharsets.US_ASCII),
                state.getBytes(StandardCharsets.US_ASCII)) && clock.instant().isBefore(pending.expiry());
    }

    /**
     * Exchanges the authorization code returned to the redirect URI for tokens and hands them to the
     * {@link OAuthClientService}.
     */
    public synchronized void authorize(String code, String state) throws VolvoCarsException {
        PendingAuthorization pending = pendingAuthorization;
        if (pending == null || !isPendingState(state)) {
            throw new VolvoCarsException("Unknown or expired authorization request");
        }
        pendingAuthorization = null;

        Fields fields = new Fields();
        fields.put("grant_type", "authorization_code");
        fields.put("code", code);
        fields.put("redirect_uri", config.redirectUri);
        fields.put("code_verifier", pending.codeVerifier());
        TokenResponse token = requestToken(fields);
        String refreshToken = token.refreshToken;
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new VolvoCarsException("Volvo ID did not return a refresh token");
        }
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(token.accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(token.expiresIn);
        response.setScope(scopes);
        response.setCreatedOn(clock.instant());
        try {
            oAuthService.importAccessTokenResponse(response);
        } catch (OAuthException e) {
            // the service has been closed, the handler is being disposed
            throw new VolvoCarsException("Could not store the tokens: " + e.getMessage(), e);
        }
        refreshRequired = false;
        storage.put(storagePrefix + CLIENT_ID, config.clientId);
        logger.debug("Obtained Volvo tokens");
    }

    /**
     * @return a valid access token, refreshed by the {@link OAuthClientService} if it has expired
     * @throws VolvoCarsException if no token could be obtained
     */
    public synchronized String getAccessToken() throws VolvoCarsException {
        if (!isAuthorized()) {
            throw new VolvoCarsException("Authorization required", 0, true);
        }
        try {
            AccessTokenResponse token = oAuthService.getAccessTokenResponse();
            if (token == null || token.getRefreshToken() == null) {
                clearTokens();
                throw new VolvoCarsException("Authorization required", 0, true);
            }
            if (refreshRequired) {
                token = oAuthService.refreshToken();
                refreshRequired = false;
            }
            String accessToken = token.getAccessToken();
            if (accessToken == null || accessToken.isBlank()) {
                throw new VolvoCarsException("Volvo ID returned an incomplete token response");
            }
            return accessToken;
        } catch (OAuthResponseException e) {
            // Volvo ID answers with 400 for an expired or revoked refresh token, 401 for rejected client credentials
            clearTokens();
            throw new VolvoCarsException("Volvo ID rejected the token refresh: " + Objects.toString(e.getError(), ""),
                    0, true);
        } catch (OAuthException | IOException e) {
            throw new VolvoCarsException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    /**
     * Forces the next {@link #getAccessToken()} call to refresh the access token.
     */
    public synchronized void invalidateAccessToken() {
        refreshRequired = true;
    }

    /**
     * Refreshes the tokens even if the access token is still valid. Volvo ID rotates the refresh token on every
     * refresh and invalidates a refresh token that is not used within 7 days, so the grant has to be renewed
     * periodically even if no API calls are made.
     */
    public synchronized void renewTokens() throws VolvoCarsException {
        invalidateAccessToken();
        getAccessToken();
    }

    public synchronized void clearTokens() {
        refreshRequired = false;
        storage.remove(storagePrefix + CLIENT_ID);
        try {
            // keeps the service for the import after a new authorization
            oAuthService.importAccessTokenResponse(new AccessTokenResponse());
        } catch (OAuthException e) {
            // the service has been closed, the handler is being disposed
            logger.debug("Could not discard the tokens: {}", e.getMessage());
        }
    }

    private TokenResponse requestToken(Fields fields) throws VolvoCarsException {
        String credentials = Base64.getEncoder()
                .encodeToString((config.clientId + ":" + config.clientSecret).getBytes(StandardCharsets.UTF_8));
        try {
            ContentResponse response = httpClient.newRequest(TOKEN_URL).method(HttpMethod.POST)
                    .header(HttpHeader.AUTHORIZATION, "Basic " + credentials)
                    .header(HttpHeader.ACCEPT, "application/json").content(new FormContentProvider(fields))
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int status = response.getStatus();
            if (status != HttpStatus.OK_200) {
                throw new VolvoCarsException("Volvo ID token endpoint returned HTTP " + status, status, false);
            }
            TokenResponse token = gson.fromJson(response.getContentAsString(), TokenResponse.class);
            String accessToken = token != null ? token.accessToken : null;
            if (token == null || accessToken == null || accessToken.isBlank()) {
                throw new VolvoCarsException("Volvo ID returned an incomplete token response");
            }
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VolvoCarsException("Token request was interrupted", e);
        } catch (ExecutionException | TimeoutException | JsonSyntaxException e) {
            throw new VolvoCarsException("Token request failed: " + e.getMessage(), e);
        }
    }

    private String randomUrlSafeString(int numBytes) {
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String computeCodeChallenge(String codeVerifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // every Java platform is required to support SHA-256
            throw new IllegalStateException(e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
