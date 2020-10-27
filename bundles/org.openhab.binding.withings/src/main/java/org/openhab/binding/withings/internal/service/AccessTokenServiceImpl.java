/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.service;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.auth.client.oauth2.*;
import org.openhab.binding.withings.internal.config.WithingsBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class AccessTokenServiceImpl implements AccessTokenInitializableService {

    private static final String API_URL_TOKEN = "https://wbsapi.withings.net/v2/oauth2";

    private final Logger logger = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

    private final OAuthFactory oAuthFactory;
    @Nullable
    private OAuthClientService oAuthService;

    public AccessTokenServiceImpl(OAuthFactory oAuthFactory) {
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public void init(String bridgeUID, WithingsBridgeConfiguration bridgeConfiguration) {
        oAuthService = oAuthFactory.createOAuthClientService(bridgeUID, API_URL_TOKEN, API_URL_TOKEN,
                bridgeConfiguration.clientId, bridgeConfiguration.clientSecret, null, false);
    }

    @Override
    public void importAccessToken(AccessTokenResponse accessTokenResponse) throws OAuthException {
        if (oAuthService == null) {
            throw new RuntimeException("The AccessTokenService isn't initialized!");
        }
        oAuthService.importAccessTokenResponse(accessTokenResponse);
    }

    @Override
    public Optional<String> getAccessToken() {
        return readToken(AccessTokenResponse::getAccessToken);
    }

    @Override
    public Optional<String> getRefreshToken() {
        return readToken(AccessTokenResponse::getRefreshToken);
    }

    @Override
    public Optional<String> getUserId() {
        return readToken(AccessTokenResponse::getState);
    }

    private Optional<String> readToken(Function<AccessTokenResponse, String> tokenReadFunction) {
        if (oAuthService != null) {
            try {
                @Nullable
                AccessTokenResponse accessTokenResponse = oAuthService.getAccessTokenResponse();
                if (accessTokenResponse != null) {
                    return Optional.ofNullable(tokenReadFunction.apply(accessTokenResponse));
                }
            } catch (OAuthException | OAuthResponseException | IOException e) {
                logger.warn("Error on reading access token! Message: {}", e.getMessage());
                logger.debug("Error on reading access token!", e);
            }
        }
        return Optional.empty();
    }
}
