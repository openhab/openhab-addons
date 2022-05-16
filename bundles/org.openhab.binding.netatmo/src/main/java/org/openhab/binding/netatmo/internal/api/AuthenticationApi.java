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

import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PATH_OAUTH;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.binding.netatmo.internal.api.dto.AccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.ApiHandlerConfiguration.Credentials;
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
    private static final URI OAUTH_URI = getApiBaseBuilder().path(PATH_OAUTH).build();

    private final ScheduledExecutorService scheduler;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);

    private @Nullable ScheduledFuture<?> refreshTokenJob;
    private Optional<AccessTokenResponse> tokenResponse = Optional.empty();
    private String scope = "";

    public AuthenticationApi(ApiBridgeHandler bridge, ScheduledExecutorService scheduler) {
        super(bridge, FeatureArea.NONE);
        this.scheduler = scheduler;
    }

    public void authenticate(Credentials credentials, Set<FeatureArea> features) throws NetatmoException {
        Set<FeatureArea> requestedFeatures = !features.isEmpty() ? features : FeatureArea.AS_SET;
        scope = FeatureArea.toScopeString(requestedFeatures);
        requestToken(credentials.clientId, credentials.clientSecret,
                Map.of(USERNAME, credentials.username, PASSWORD, credentials.password, SCOPE, scope));
    }

    private void requestToken(String id, String secret, Map<String, String> entries) throws NetatmoException {
        Map<String, String> payload = new HashMap<>(entries);
        payload.putAll(Map.of(GRANT_TYPE, entries.keySet().contains(PASSWORD) ? PASSWORD : REFRESH_TOKEN, CLIENT_ID, id,
                CLIENT_SECRET, secret));
        disconnect();
        AccessTokenResponse response = post(OAUTH_URI, AccessTokenResponse.class, payload);
        refreshTokenJob = scheduler.schedule(() -> {
            try {
                requestToken(id, secret, Map.of(REFRESH_TOKEN, response.getRefreshToken()));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}", e.getMessage());
            }
        }, Math.round(response.getExpiresIn() * 0.8), TimeUnit.SECONDS);
        tokenResponse = Optional.of(response);
    }

    public void disconnect() {
        tokenResponse = Optional.empty();
    }

    public void dispose() {
        ScheduledFuture<?> job = refreshTokenJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshTokenJob = null;
    }

    public @Nullable String getAuthorization() {
        return tokenResponse.map(at -> String.format("Bearer %s", at.getAccessToken())).orElse(null);
    }

    public boolean matchesScopes(Set<Scope> requiredScopes) {
        // either we do not require any scope, either connected and all scopes available
        return requiredScopes.isEmpty()
                || (isConnected() && tokenResponse.map(at -> at.getScope().containsAll(requiredScopes)).orElse(false));
    }

    public boolean isConnected() {
        return !tokenResponse.isEmpty();
    }
}
