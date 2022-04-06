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
package org.openhab.binding.webexteams.internal.api;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
class AccessTokenRequest {
    @SerializedName("grant_type")
    @Nullable
    private String grantType;
    @SerializedName("client_id")
    @Nullable
    private String clientId;
    @SerializedName("client_secret")
    @Nullable
    private String clientSecret;
    @SerializedName("code")
    @Nullable
    private String code;
    @SerializedName("refresh_token")
    @Nullable
    private String refreshToken;
    @SerializedName("redirect_uri")
    @Nullable
    private URI redirectUri;

    @Nullable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(@Nullable String clientId) {
        this.clientId = clientId;
    }

    @Nullable
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(@Nullable String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Nullable
    public URI getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(URI redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Nullable
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    @Nullable
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@Nullable String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
