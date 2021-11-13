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
package org.openhab.binding.cloudrain.internal.api.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Token} class represents a Cloudrain API access token.
 * It contains an access and a refresh token and their validity.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class Token {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private transient Instant refreshTokenValidity;
    private transient Instant accessTokenValidity;

    @SerializedName("token_type")
    private @Nullable String tokenType;

    @SerializedName("access_token")
    private @Nullable String accessToken;

    @SerializedName("refresh_token")
    private @Nullable String refreshToken;

    @SerializedName("expires_in")
    private @Nullable Integer expiresIn;

    /**
     * Creates a Token with the required attributes. Useful for test implementations. Typically objects of
     * this type will be created through reflection by the GSON library when parsing the JSON response of the API
     *
     * @param tokenType the type of the token (e.g. Bearer)
     * @param accessToken the access token String
     * @param refreshToken the refresh token String
     * @param expiresIn the amount of seconds in which the access token expires
     */
    public Token(String tokenType, String accessToken, String refreshToken, Integer expiresIn) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        accessTokenValidity = refreshTokenValidity = Instant.now();
    }

    public @Nullable String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Returns the token type received from the API of a default value "Bearer"
     *
     * @return the token type received from the API of a default value "Bearer"
     */
    public String getTokenType() {
        String result = this.tokenType;
        if (result != null) {
            return result;
        }
        return TOKEN_TYPE_BEARER;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public @Nullable String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public @Nullable Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * Initializes this Token by resetting the token expiration timestamps
     * This method should be called for a newly received tokens in order to correctly calculate the token validity
     */
    public void initialize() {
        Integer expiresInLocal = getExpiresIn();
        int expires = expiresInLocal != null ? expiresInLocal : 0;
        accessTokenValidity = Instant.now().plus(expires - 10, ChronoUnit.SECONDS);
        refreshTokenValidity = Instant.now().plus(10, ChronoUnit.DAYS).minus(1, ChronoUnit.MINUTES);
    }

    /**
     * Checks whether the access token is still valid or expired
     *
     * @return true if the access token is still valid. False if expired
     */
    public boolean isAccessTokenValid() {
        String accessTokenLocal = this.accessToken;
        return accessTokenLocal != null && !accessTokenLocal.isBlank() && Instant.now().isBefore(accessTokenValidity);
    }

    /**
     * Checks whether the refresh token is still valid or expired
     *
     * @return true if the refresh token is still valid. False if expired
     */
    public boolean isRefreshTokenValid() {
        String accessTokenLocal = this.accessToken;
        return accessTokenLocal != null && !accessTokenLocal.isBlank() && Instant.now().isBefore(refreshTokenValidity);
    }

    /**
     * Checks the consistency of the entire token including all fields and expiration times
     *
     * @return true if the entire token is valid. False otherwise.
     */
    public boolean isTokenValid() {
        Integer expiresInLocal = this.expiresIn;
        boolean attributesValid = isValid(accessToken) && isValid(refreshToken);
        boolean isExpiresValid = expiresInLocal != null && expiresInLocal.intValue() > 0;
        return attributesValid && isExpiresValid && isAccessTokenValid() && isRefreshTokenValid();
    }

    private boolean isValid(@Nullable String attr) {
        return attr != null && !attr.isEmpty() && !attr.isBlank();
    }
}
