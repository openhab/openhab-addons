/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.*;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.api.dto.AccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.ApiHandlerConfiguration;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthenticationApi} handles oAuth2 authentication and token refreshing
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AuthenticationApi extends RestManager {
    private static final UriBuilder OAUTH_BUILDER = getApiBaseBuilder().path(PATH_OAUTH);
    private static final UriBuilder AUTH_BUILDER = OAUTH_BUILDER.clone().path(SUB_PATH_AUTHORIZE);
    private static final URI TOKEN_URI = OAUTH_BUILDER.clone().path(SUB_PATH_TOKEN).build();

    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);
    private final ScheduledExecutorService scheduler;

    private Optional<ScheduledFuture<?>> refreshTokenJob = Optional.empty();
    private Optional<AccessTokenResponse> tokenResponse = Optional.empty();

    public AuthenticationApi(ApiBridgeHandler bridge, ScheduledExecutorService scheduler) {
        super(bridge, FeatureArea.NONE);
        this.scheduler = scheduler;
    }

    public String authorize(ApiHandlerConfiguration credentials, @Nullable String code, @Nullable String redirectUri)
            throws NetatmoException {
        if (!(credentials.clientId.isBlank() || credentials.clientSecret.isBlank())) {
            Map<String, String> params = new HashMap<>(Map.of(SCOPE, FeatureArea.ALL_SCOPES));
            String refreshToken = credentials.refreshToken;
            if (!refreshToken.isBlank()) {
                params.put(REFRESH_TOKEN, refreshToken);
            } else {
                if (code != null && redirectUri != null) {
                    params.putAll(Map.of(REDIRECT_URI, redirectUri, CODE, code));
                }
            }
            if (params.size() > 1) {
                return requestToken(credentials.clientId, credentials.clientSecret, params);
            }
        }
        throw new IllegalArgumentException("Inconsistent configuration state, please file a bug report.");
    }

    private String requestToken(String id, String secret, Map<String, String> entries) throws NetatmoException {
        Map<String, String> payload = new HashMap<>(entries);
        payload.put(GRANT_TYPE, payload.keySet().contains(CODE) ? AUTHORIZATION_CODE : REFRESH_TOKEN);
        payload.putAll(Map.of(CLIENT_ID, id, CLIENT_SECRET, secret));
        disconnect();
        AccessTokenResponse response = post(TOKEN_URI, AccessTokenResponse.class, payload);
        refreshTokenJob = Optional.of(scheduler.schedule(() -> {
            try {
                requestToken(id, secret, Map.of(REFRESH_TOKEN, response.getRefreshToken()));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}", e.getMessage());
            }
        }, Math.round(response.getExpiresIn() * 0.8), TimeUnit.SECONDS));
        tokenResponse = Optional.of(response);
        return response.getRefreshToken();
    }

    public void disconnect() {
        tokenResponse = Optional.empty();
    }

    public void dispose() {
        refreshTokenJob.ifPresent(job -> job.cancel(true));
        refreshTokenJob = Optional.empty();
    }

    public @Nullable String getAuthorization() {
        return tokenResponse.map(at -> String.format("Bearer %s", at.getAccessToken())).orElse(null);
    }

    public boolean matchesScopes(Set<Scope> requiredScopes) {
        return requiredScopes.isEmpty() // either we do not require any scope, either connected and all scopes available
                || (isConnected() && tokenResponse.map(at -> at.getScope().containsAll(requiredScopes)).orElse(false));
    }

    public boolean isConnected() {
        return tokenResponse.isPresent();
    }

    public static UriBuilder getAuthorizationBuilder(String clientId) {
        return AUTH_BUILDER.clone().queryParam(CLIENT_ID, clientId).queryParam(SCOPE, FeatureArea.ALL_SCOPES)
                .queryParam(STATE, clientId);
    }
}
