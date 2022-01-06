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
package org.openhab.binding.gardena.internal.model.dto.api;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a Gardena object that is sent via the Gardena API.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PostOAuth2Response {
    // refresh token is valid 10 days
    private transient Instant refreshTokenValidity = Instant.now().plus(10, ChronoUnit.DAYS).minus(1,
            ChronoUnit.MINUTES);
    private transient Instant accessTokenValidity;

    @SerializedName("access_token")
    public String accessToken;

    // The scope of the token (what you are allowed to do)
    public String scope;

    // The expire time in seconds for the access token
    @SerializedName("expires_in")
    public Integer expiresIn;

    @SerializedName("refresh_token")
    public String refreshToken;

    public String provider;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("token_type")
    public String tokenType;

    public void postProcess() {
        accessTokenValidity = Instant.now().plus(expiresIn - 10, ChronoUnit.SECONDS);
    }

    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(accessTokenValidity);
    }

    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshTokenValidity);
    }

    @Override
    public String toString() {
        return "Token expiration: accessToken: " + ZonedDateTime.ofInstant(accessTokenValidity, ZoneId.systemDefault())
                + ", refreshToken: " + ZonedDateTime.ofInstant(refreshTokenValidity, ZoneId.systemDefault());
    }
}
