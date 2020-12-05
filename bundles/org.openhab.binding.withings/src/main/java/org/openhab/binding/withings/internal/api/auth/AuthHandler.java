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
package org.openhab.binding.withings.internal.api.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.api.AbstractAPIHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class AuthHandler extends AbstractAPIHandler {

    private static final String API_URL_TOKEN = "https://wbsapi.withings.net/v2/oauth2";
    private static final String REDIRECT_URI = "https://myopenhab.org";

    public AuthHandler(AccessTokenService accessTokenService, HttpClient httpClient) {
        super(accessTokenService, httpClient);
    }

    public Optional<WithingsAccessTokenResponseDTO> redeemAuthCode(String clientId, String clientSecret,
            String authCode) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);
        parameters.put("grant_type", "authorization_code");
        parameters.put("code", authCode);
        parameters.put("redirect_uri", REDIRECT_URI);

        return executeAuthPOSTRequest(API_URL_TOKEN, "requesttoken", parameters, WithingsAccessTokenResponseDTO.class);
    }

    public Optional<WithingsAccessTokenResponseDTO> refreshAccessToken(String clientId, String clientSecret,
            String refreshToken) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);
        parameters.put("grant_type", "refresh_token");
        parameters.put("refresh_token", refreshToken);

        return executeAuthPOSTRequest(API_URL_TOKEN, "requesttoken", parameters, WithingsAccessTokenResponseDTO.class);
    }
}
