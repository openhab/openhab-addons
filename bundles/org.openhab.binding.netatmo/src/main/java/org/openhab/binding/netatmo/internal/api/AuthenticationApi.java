/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.PATH_OAUTH;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAAccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthenticationApi} is handling oAuth2 authentication as well as token refreshing
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class AuthenticationApi extends RestManager {
    protected static final URI OAUTH_URI = API_BASE_BUILDER.clone().path(PATH_OAUTH).build();

    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);
    private final NetatmoBindingConfiguration configuration;
    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> refreshTokenJob;

    AuthenticationApi(ApiBridge apiBridge, NetatmoBindingConfiguration configuration,
            ScheduledExecutorService scheduler) {
        super(apiBridge, FeatureArea.NONE);
        this.configuration = configuration;
        this.scheduler = scheduler;
    }

    void authenticate() throws NetatmoException {
        Map<String, @Nullable String> payload = new HashMap<>(Map.of(SCOPE, FeatureArea.ALL_SCOPES));
        payload.put(PASSWORD, configuration.password);
        payload.put(USERNAME, configuration.username);
        requestToken(getPayload(PASSWORD, payload));
    }

    private void requestToken(String tokenRequest) throws NetatmoException {
        NAAccessTokenResponse answer = apiBridge.executeUri(OAUTH_URI, POST, NAAccessTokenResponse.class, tokenRequest);
        apiBridge.onAccessTokenResponse(answer.getAccessToken(), answer.getScope());
        freeTokenJob();
        refreshTokenJob = scheduler.schedule(() -> {
            try {
                requestToken(getPayload(REFRESH_TOKEN, Map.of(REFRESH_TOKEN, answer.getRefreshToken())));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}", e.getMessage());
            }
        }, Math.round(answer.getExpiresIn() * 0.8), TimeUnit.SECONDS);
    }

    private String getPayload(String grantType, Map<String, @Nullable String> entries) {
        Map<String, @Nullable String> payload = new HashMap<>(entries);
        payload.put(GRANT_TYPE, grantType);
        payload.put(CLIENT_ID, configuration.clientId);
        payload.put(CLIENT_SECRET, configuration.clientSecret);
        return payload.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    public void dispose() {
        freeTokenJob();
    }

    private void freeTokenJob() {
        ScheduledFuture<?> job = refreshTokenJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshTokenJob = null;
    }
}
