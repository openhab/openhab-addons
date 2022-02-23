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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.SERVICE_PID;
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
import org.openhab.binding.netatmo.internal.api.dto.NAAccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration.NACredentials;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthenticationApi} handles oAuth2 authentication and token refreshing
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class AuthenticationApi extends RestManager {
    private static final URI OAUTH_URI = getApiBaseBuilder().path(PATH_OAUTH).build();

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(SERVICE_PID);
    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);

    private Optional<ScheduledFuture<?>> refreshTokenJob = Optional.empty();
    private Optional<NAAccessTokenResponse> answer = Optional.empty();

    AuthenticationApi(ApiBridge bridge) {
        super(bridge, FeatureArea.NONE);
    }

    void authenticate(NACredentials credentials) throws NetatmoException {
        requestToken(credentials.clientId, credentials.clientSecret,
                Map.of(SCOPE, FeatureArea.ALL_SCOPES, PASSWORD, credentials.password, USERNAME, credentials.username));
    }

    private void requestToken(String clientId, String clientSecret, Map<String, String> entries)
            throws NetatmoException {
        freeTokenJob();

        Map<String, String> payload = new HashMap<>(entries);
        payload.putAll(Map.of(CLIENT_ID, clientId, CLIENT_SECRET, clientSecret, GRANT_TYPE,
                entries.keySet().contains(PASSWORD) ? PASSWORD : REFRESH_TOKEN));

        answer = Optional.empty();
        NAAccessTokenResponse response = post(OAUTH_URI, NAAccessTokenResponse.class, payload);
        refreshTokenJob = Optional.of(scheduler.schedule(() -> {
            try {
                requestToken(clientId, clientSecret, Map.of(REFRESH_TOKEN, response.getRefreshToken()));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}", e.getMessage());
            }
        }, Math.round(response.getExpiresIn() * 0.8), TimeUnit.SECONDS));
        answer = Optional.of(response);
    }

    public void dispose() {
        freeTokenJob();
    }

    private void freeTokenJob() {
        refreshTokenJob.ifPresent(j -> j.cancel(true));
        refreshTokenJob = Optional.empty();
    }

    public boolean hasScopes(Set<Scope> requiredScopes) {
        return answer.map(at -> at.getScope().containsAll(requiredScopes)).orElse(false);
    }

    public @Nullable String getAuthorization() {
        return answer.map(at -> String.format("Bearer %s", at.getAccessToken())).orElse(null);
    }
}
