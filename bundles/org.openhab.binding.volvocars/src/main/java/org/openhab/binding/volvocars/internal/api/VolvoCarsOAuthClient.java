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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * OAuth 2.0 authorization code flow with PKCE (RFC 7636) against Volvo ID.
 * <p>
 * The core {@code OAuthClientService} is not used because it cannot send a per-authorization
 * {@code code_verifier} with the token request only. The refresh token is persisted in the
 * given {@link Storage}, the short-lived access token is kept in memory only.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsOAuthClient {
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String CLIENT_ID = "clientId";
    private static final Duration EXPIRY_MARGIN = Duration.ofSeconds(60);
    private static final Duration PENDING_AUTHORIZATION_VALIDITY = Duration.ofMinutes(15);
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(VolvoCarsOAuthClient.class);
    private final SecureRandom random = new SecureRandom();
    private final Gson gson = new Gson();

    private final HttpClient httpClient;
    private final Storage<String> storage;
    private final String storagePrefix;
    private final AccountConfiguration config;
    private final String scopes;
    private final Clock clock;

    private String accessToken = "";
    private Instant accessTokenExpiry = Instant.MIN;
    private @Nullable PendingAuthorization pendingAuthorization;

    private record PendingAuthorization(String state, String codeVerifier, Instant expiry) {
    }

    public VolvoCarsOAuthClient(HttpClient httpClient, Storage<String> storage, String storagePrefix,
            AccountConfiguration config) {
        this(httpClient, storage, storagePrefix, config, Clock.systemUTC());
    }

    VolvoCarsOAuthClient(HttpClient httpClient, Storage<String> storage, String storagePrefix,
            AccountConfiguration config, Clock clock) {
        this.httpClient = httpClient;
        this.storage = storage;
        this.storagePrefix = storagePrefix + ".";
        this.config = config;
        this.scopes = config.scopes.isBlank() ? DEFAULT_SCOPES : config.scopes.trim();
        this.clock = clock;
    }

    /**
     * @return true if a refresh token obtained with the currently configured client id is available
     */
    public synchronized boolean isAuthorized() {
        if (value(REFRESH_TOKEN).isEmpty()) {
            return false;
        }
        if (!config.clientId.equals(value(CLIENT_ID))) {
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
     * Exchanges the authorization code returned to the redirect URI for tokens.
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
        requestToken(fields, false);
        storage.put(storagePrefix + CLIENT_ID, config.clientId);
    }

    /**
     * @return a valid access token, refreshing it if required
     * @throws VolvoCarsException if no token could be obtained
     */
    public synchronized String getAccessToken() throws VolvoCarsException {
        if (!accessToken.isEmpty() && clock.instant().plus(EXPIRY_MARGIN).isBefore(accessTokenExpiry)) {
            return accessToken;
        }
        if (!isAuthorized()) {
            throw new VolvoCarsException("Authorization required", 0, true);
        }
        Fields fields = new Fields();
        fields.put("grant_type", "refresh_token");
        fields.put("refresh_token", value(REFRESH_TOKEN));
        return requestToken(fields, true);
    }

    /**
     * Forces the next {@link #getAccessToken()} call to refresh the access token.
     */
    public synchronized void invalidateAccessToken() {
        accessToken = "";
        accessTokenExpiry = Instant.MIN;
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
        invalidateAccessToken();
        storage.remove(storagePrefix + REFRESH_TOKEN);
        storage.remove(storagePrefix + CLIENT_ID);
    }

    private String requestToken(Fields fields, boolean isRefresh) throws VolvoCarsException {
        String credentials = Base64.getEncoder()
                .encodeToString((config.clientId + ":" + config.clientSecret).getBytes(StandardCharsets.UTF_8));
        try {
            ContentResponse response = httpClient.newRequest(TOKEN_URL).method(HttpMethod.POST)
                    .header(HttpHeader.AUTHORIZATION, "Basic " + credentials)
                    .header(HttpHeader.ACCEPT, "application/json").content(new FormContentProvider(fields))
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int status = response.getStatus();
            if (status != HttpStatus.OK_200) {
                // Volvo ID answers with 400 for an expired or revoked refresh token
                boolean authorizationRequired = isRefresh
                        && (status == HttpStatus.BAD_REQUEST_400 || status == HttpStatus.UNAUTHORIZED_401);
                if (authorizationRequired) {
                    clearTokens();
                }
                throw new VolvoCarsException("Volvo ID token endpoint returned HTTP " + status, status,
                        authorizationRequired);
            }
            TokenResponse token = gson.fromJson(response.getContentAsString(), TokenResponse.class);
            String newAccessToken = token != null ? token.accessToken : null;
            if (token == null || newAccessToken == null || newAccessToken.isBlank()) {
                throw new VolvoCarsException("Volvo ID returned an incomplete token response");
            }
            String newRefreshToken = token.refreshToken;
            if (newRefreshToken != null && !newRefreshToken.isBlank()) {
                storage.put(storagePrefix + REFRESH_TOKEN, newRefreshToken);
            } else if (!isRefresh) {
                throw new VolvoCarsException("Volvo ID did not return a refresh token");
            }
            accessToken = newAccessToken;
            accessTokenExpiry = clock.instant().plusSeconds(token.expiresIn);
            logger.debug("Obtained Volvo access token, valid until {}", accessTokenExpiry);
            return newAccessToken;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VolvoCarsException("Token request was interrupted", e);
        } catch (ExecutionException | TimeoutException | JsonSyntaxException e) {
            throw new VolvoCarsException("Token request failed: " + e.getMessage(), e);
        }
    }

    private String value(String key) {
        String value = storage.get(storagePrefix + key);
        return value != null ? value : "";
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
