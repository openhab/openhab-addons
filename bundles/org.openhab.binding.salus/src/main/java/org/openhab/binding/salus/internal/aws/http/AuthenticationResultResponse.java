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
package org.openhab.binding.salus.internal.aws.http;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Copied from org.openhab.binding.windcentrale.internal.dto.AuthenticationResultResponse
 * 
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class AuthenticationResultResponse {

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
