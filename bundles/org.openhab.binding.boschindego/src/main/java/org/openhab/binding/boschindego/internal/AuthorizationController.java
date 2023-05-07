/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschindego.internal;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AuthorizationController} acts as a bridge between
 * {@link OAuthClientService} and {@link IndegoController}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class AuthorizationController implements AuthorizationProvider {

    private static final String BEARER = "Bearer ";

    private final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    private OAuthClientService oAuthClientService;

    public AuthorizationController(OAuthClientService oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
    }

    public void setOAuthClientService(OAuthClientService oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
    }

    public String getAuthorizationHeader() throws IndegoException {
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = getAccessToken();
        } catch (OAuthException | OAuthResponseException e) {
            logger.debug("Error fetching access token: {}", e.getMessage(), e);
            throw new IndegoAuthenticationException(
                    "Error fetching access token. Invalid authcode? Please generate a new one -> "
                            + getAuthorizationUrl(),
                    e);
        } catch (IOException e) {
            throw new IndegoException("An unexpected IOException occurred: " + e.getMessage(), e);
        }
        if (accessTokenResponse.getAccessToken() == null || accessTokenResponse.getAccessToken().isEmpty()) {
            throw new IndegoAuthenticationException(
                    "No access token. Is this thing authorized? -> " + getAuthorizationUrl());
        }
        if (accessTokenResponse.getRefreshToken() == null || accessTokenResponse.getRefreshToken().isEmpty()) {
            throw new IndegoAuthenticationException("No refresh token. Please reauthorize -> " + getAuthorizationUrl());
        }

        return BEARER + accessTokenResponse.getAccessToken();
    }

    public AccessTokenResponse getAccessToken() throws OAuthException, OAuthResponseException, IOException {
        AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
        if (accessTokenResponse == null) {
            throw new OAuthException("No access token response");
        }

        return accessTokenResponse;
    }

    private String getAuthorizationUrl() {
        try {
            return oAuthClientService.getAuthorizationUrl(BSK_REDIRECT_URI, BSK_SCOPE, null);
        } catch (OAuthException e) {
            return "";
        }
    }
}
