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

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.PUBLIC_DATA_API_V2_TOKEN_URL;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Handles SolarEdge's JSON-based OAuth token exchange and rotating refresh tokens.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeOAuthClient {
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String EXPIRES_AT = "expiresAt";
    private static final long EXPIRY_MARGIN_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeOAuthClient.class);

    private final HttpClient httpClient;
    private final Storage<String> storage;
    private final Clock clock;
    private final Gson gson = new Gson();

    public SolarEdgeOAuthClient(HttpClient httpClient, Storage<String> storage) {
        this(httpClient, storage, Clock.systemUTC());
    }

    SolarEdgeOAuthClient(HttpClient httpClient, Storage<String> storage, Clock clock) {
        this.httpClient = httpClient;
        this.storage = storage;
        this.clock = clock;
    }

    public synchronized boolean hasRefreshToken() {
        return !value(REFRESH_TOKEN).isBlank();
    }

    public synchronized String getAccessToken(SolarEdgeConfiguration config) throws SolarEdgeOAuthException {
        String accessToken = value(ACCESS_TOKEN);
        if (!accessToken.isBlank() && getExpiry() > clock.instant().plusSeconds(EXPIRY_MARGIN_SECONDS).toEpochMilli()) {
            return accessToken;
        }
        String refreshToken = value(REFRESH_TOKEN);
        if (refreshToken.isBlank()) {
            throw new SolarEdgeOAuthException("SolarEdge authorization is required");
        }
        return requestToken(Map.of("grant_type", "refresh_token", "refresh_token", refreshToken, "client_id",
                config.getOAuthClientId(), "client_secret", config.getOAuthClientSecret()));
    }

    public synchronized String exchangeAuthorizationCode(SolarEdgeConfiguration config, String code)
            throws SolarEdgeOAuthException {
        return requestToken(Map.of("grant_type", "authorization_code", "code", code, "client_id",
                config.getOAuthClientId(), "client_secret", config.getOAuthClientSecret()));
    }

    public synchronized void invalidateAccessToken() {
        storage.remove(ACCESS_TOKEN);
        storage.remove(EXPIRES_AT);
    }

    private String requestToken(Map<String, String> payload) throws SolarEdgeOAuthException {
        String grantType = payload.getOrDefault("grant_type", "unknown");
        logger.debug("Requesting SolarEdge OAuth token using {} grant", grantType);
        try {
            ContentResponse response = httpClient.newRequest(PUBLIC_DATA_API_V2_TOKEN_URL).method(HttpMethod.POST)
                    .content(
                            new StringContentProvider("application/json", gson.toJson(payload), StandardCharsets.UTF_8))
                    .send();
            if (response.getStatus() != HttpStatus.OK_200) {
                boolean authorizationRequired = "refresh_token".equals(grantType)
                        && (response.getStatus() == HttpStatus.BAD_REQUEST_400
                                || response.getStatus() == HttpStatus.UNAUTHORIZED_401);
                if (authorizationRequired) {
                    clearTokens();
                }
                throw new SolarEdgeOAuthException("SolarEdge token endpoint returned HTTP " + response.getStatus(),
                        authorizationRequired);
            }
            SolarEdgeOAuthToken token = gson.fromJson(response.getContentAsString(), SolarEdgeOAuthToken.class);
            if (token == null || token.accessToken.isBlank() || token.refreshToken.isBlank()) {
                throw new SolarEdgeOAuthException("SolarEdge returned an incomplete token response");
            }
            storage.put(ACCESS_TOKEN, token.accessToken);
            storage.put(REFRESH_TOKEN, token.refreshToken);
            Instant expiresAt = clock.instant().plusSeconds(token.expiresIn);
            storage.put(EXPIRES_AT, Long.toString(expiresAt.toEpochMilli()));
            logger.debug("SolarEdge OAuth token acquired using {} grant; expires at {} and refresh token rotated",
                    grantType, expiresAt);
            return token.accessToken;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SolarEdgeOAuthException("SolarEdge token request was interrupted", e);
        } catch (ExecutionException | TimeoutException | JsonSyntaxException e) {
            throw new SolarEdgeOAuthException("SolarEdge token request failed", e);
        }
    }

    private long getExpiry() {
        try {
            return Long.parseLong(value(EXPIRES_AT));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void clearTokens() {
        storage.remove(ACCESS_TOKEN);
        storage.remove(REFRESH_TOKEN);
        storage.remove(EXPIRES_AT);
    }

    private String value(String key) {
        @Nullable
        String value = storage.get(key);
        return value == null ? "" : value;
    }
}
