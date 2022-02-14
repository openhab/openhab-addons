/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.livisismarthome.internal.client.entity.auth;

import java.time.LocalDateTime;

import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the structure of the access token response body.
 *
 * @author Sven Strohschein - Initial contribution
 */
public class LivisiAccessTokenResponse {

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("expires_in")
    private long expiresIn;
    @SerializedName("token_type")
    private String tokenType;

    public AccessTokenResponse createAccessTokenResponse() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setCreatedOn(LocalDateTime.now());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setTokenType(tokenType);
        return response;
    }
}
