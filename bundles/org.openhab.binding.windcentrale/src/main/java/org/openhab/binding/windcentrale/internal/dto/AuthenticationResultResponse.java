/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AuthenticationResultResponse} is returned by Cognito after responding to an SRP challenge by a
 * {@link RespondToAuthChallengeRequest} or when refreshing tokens using an {@link InitiateAuthRequest}.
 *
 * The refresh token is only provided as part of the SRP challenge response and will be empty when it is used to refresh
 * tokens.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class AuthenticationResultResponse {

    private static class AuthenticationResult {
        public String accessToken = "";
        public int expiresIn;
        public String idToken = "";
        public String refreshToken = "";
        public String tokenType = "";
    }

    private AuthenticationResult authenticationResult = new AuthenticationResult();

    public String getAccessToken() {
        return authenticationResult.accessToken;
    }

    public int getExpiresIn() {
        return authenticationResult.expiresIn;
    }

    public String getIdToken() {
        return authenticationResult.idToken;
    }

    public String getRefreshToken() {
        return authenticationResult.refreshToken;
    }

    public String getTokenType() {
        return authenticationResult.tokenType;
    }
}
