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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAAccessTokenResponse;
import org.openhab.binding.netatmo.internal.config.NetatmoBindingConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.oauth2client.internal.Keyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows access to the AutomowerConnectApi
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AuthenticationApi extends RestManager {
    private static final String OAUTH_BASE = "oauth2/token";
    private static final String GRANT_BASE = "grant_type=%s&client_id=%s&client_secret=%s";
    private static final String TOKEN_REQ = "&username=%s&password=%s&scope=%s";
    private static final String TOKEN_REF = "&refresh_token=%s";

    private final Logger logger = LoggerFactory.getLogger(AuthenticationApi.class);
    private final NetatmoBindingConfiguration configuration;
    private final ScheduledExecutorService scheduler;

    public AuthenticationApi(ApiBridge apiClient, OAuthFactory oAuthFactory, NetatmoBindingConfiguration configuration,
            ScheduledExecutorService scheduler) {
        super(apiClient, Set.of(), OAUTH_BASE);
        this.configuration = configuration;
        this.scheduler = scheduler;
    }

    private String getBaseRequest(String grantType) {
        return String.format(GRANT_BASE, grantType, configuration.clientId, configuration.clientSecret);
    }

    public void authenticate() throws NetatmoException {
        List<String> scopes = new ArrayList<>();
        NetatmoConstants.ALL_SCOPES.forEach(scope -> scopes.add(scope.name().toLowerCase()));

        String req = getBaseRequest(Keyword.PASSWORD);
        req += String.format(TOKEN_REQ, configuration.username, configuration.password, String.join(" ", scopes));

        NAAccessTokenResponse authorization = post(req, NAAccessTokenResponse.class);
        apiHandler.onAccessTokenResponse(authorization.getAccessToken(), authorization.getScope());

        scheduleTokenRefresh(authorization.getRefreshToken(), 5 /* authorization.getExpiresIn() */);
    }

    private void scheduleTokenRefresh(String refreshToken, long delay) {
        scheduler.schedule(() -> {
            String req = getBaseRequest(Keyword.REFRESH_TOKEN);
            req += String.format(TOKEN_REF, refreshToken);
            try {
                NAAccessTokenResponse answer = post(req, NAAccessTokenResponse.class);
                apiHandler.onAccessTokenResponse(answer.getAccessToken(), answer.getScope());
                scheduleTokenRefresh(answer.getRefreshToken(), Math.round(answer.getExpiresIn() * 0.9));
            } catch (NetatmoException e) {
                logger.warn("Unable to refresh access token : {}", e.getMessage());
            }
        }, delay, TimeUnit.SECONDS);
    }
}
