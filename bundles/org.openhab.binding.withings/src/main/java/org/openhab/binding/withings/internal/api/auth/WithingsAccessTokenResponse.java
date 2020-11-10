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

import java.time.LocalDateTime;

import org.openhab.binding.withings.internal.api.BaseResponse;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.annotations.SerializedName;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class WithingsAccessTokenResponse extends BaseResponse {

    private WithingsAccessTokenResponseBody body;

    public AccessTokenResponse createAccessTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(body.accessToken);
        response.setRefreshToken(body.refreshToken);
        response.setScope(body.scope);
        response.setTokenType(body.tokenType);
        response.setCreatedOn(LocalDateTime.now());
        response.setExpiresIn(body.expiresIn);
        response.setState(body.userId);
        return response;
    }

    class WithingsAccessTokenResponseBody {

        @SerializedName("userid")
        private String userId;
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("refresh_token")
        private String refreshToken;
        private String scope;
        @SerializedName("expires_in")
        private long expiresIn;
        @SerializedName("token_type")
        private String tokenType;
    }
}
