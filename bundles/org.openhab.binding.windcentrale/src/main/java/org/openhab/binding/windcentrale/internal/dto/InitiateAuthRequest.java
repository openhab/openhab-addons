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
package org.openhab.binding.windcentrale.internal.dto;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InitiateAuthRequest} can be used to start a Cognito user SRP authentication challenge or to refresh
 * expired tokens using a refresh token.
 *
 * When starting user SRP authentication Cognito will respond with a {@link ChallengeResponse}.
 * When refreshing expired tokens Cognito grants the new tokens in a {@link AuthenticationResultResponse}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class InitiateAuthRequest {

    public String authFlow = "";

    public String clientId = "";

    public Map<String, String> authParameters = new TreeMap<>();

    InitiateAuthRequest(String authFlow, String clientId, Map<String, String> authParameters) {
        this.authFlow = authFlow;
        this.clientId = clientId;
        this.authParameters.putAll(authParameters);
    }

    public static InitiateAuthRequest userSrpAuth(String clientId, String username, String srpA) {
        return new InitiateAuthRequest("USER_SRP_AUTH", clientId, Map.of("USERNAME", username, "SRP_A", srpA));
    }

    public static InitiateAuthRequest refreshTokenAuth(String clientId, String refreshToken) {
        return new InitiateAuthRequest("REFRESH_TOKEN_AUTH", clientId, Map.of("REFRESH_TOKEN", refreshToken));
    }
}
