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

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.ALL_SCOPES;
import static org.openhab.core.auth.oauth2client.internal.Keyword.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.netatmo.internal.api.dto.NAAccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows access to the AutomowerConnectApi
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class AuthenticationApi extends RestManager {
    private static final String SCOPES = ALL_SCOPES.stream().map(s -> s.name().toLowerCase())
            .collect(Collectors.joining(" "));

    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);
    private final NetatmoBindingConfiguration configuration;
    private final ScheduledExecutorService scheduler;

    AuthenticationApi(ApiBridge apiClient, OAuthFactory oAuthFactory, NetatmoBindingConfiguration configuration,
            ScheduledExecutorService scheduler) {
        super(apiClient);
        this.configuration = configuration;
        this.scheduler = scheduler;
    }

    void authenticate() throws NetatmoException {
        Map<String, @Nullable String> payload = new HashMap<>();
        payload.put(PASSWORD, configuration.password);
        payload.put(USERNAME, configuration.username);
        payload.put(SCOPE, SCOPES);
        requestToken(getPayload(PASSWORD, payload));
    }

    private void requestToken(String tokenRequest) throws NetatmoException {
        NAAccessTokenResponse answer = apiHandler.executeUri(getOAuthUri(), HttpMethod.POST,
                NAAccessTokenResponse.class, tokenRequest);
        apiHandler.onAccessTokenResponse(answer.getAccessToken(), answer.getScope());
        scheduler.schedule(() -> {
            try {
                requestToken(getPayload(REFRESH_TOKEN, Map.of(REFRESH_TOKEN, answer.getRefreshToken())));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}, trying to reopen connection.", e.getMessage());
                apiHandler.openConnection();
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
}
