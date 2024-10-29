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
package org.openhab.binding.iaqualink.internal.v2.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Access credentials.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public class UserPoolOAuth {

    @SerializedName("AccessToken")
    String AccessToken;

    @SerializedName("ExpiresIn")
    int ExpiresIn;

    @SerializedName("IdToken")
    String IdToken;

    @SerializedName("RefreshToken")
    String RefreshToken;

    @SerializedName("TokenType")
    String TokenType;

    public void setAccessToken(String AccessToken) {
        this.AccessToken = AccessToken;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public void setExpiresIn(int ExpiresIn) {
        this.ExpiresIn = ExpiresIn;
    }

    public int getExpiresIn() {
        return ExpiresIn;
    }

    public void setIdToken(String IdToken) {
        this.IdToken = IdToken;
    }

    public String getIdToken() {
        return IdToken;
    }

    public void setRefreshToken(String RefreshToken) {
        this.RefreshToken = RefreshToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public void setTokenType(String TokenType) {
        this.TokenType = TokenType;
    }

    public String getTokenType() {
        return TokenType;
    }
}
