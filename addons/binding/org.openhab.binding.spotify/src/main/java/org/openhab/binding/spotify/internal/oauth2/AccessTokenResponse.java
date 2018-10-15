/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.oauth2;

/**
 * This class and its inner classes represents the Spotify Web API response of an authorization request
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class AccessTokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private int expiresIn;

    public AccessTokenResponse() {
    }

    public AccessTokenResponse(String refreshToken) {
        this.refreshToken = refreshToken;
        this.accessToken = "";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        return "AuthorizationCodeCredentials [accessToken=" + accessToken + ", refreshToken=" + refreshToken
                + ", tokenType=" + tokenType + ", scope=" + scope + ", expiresIn=" + expiresIn + "]";
    }
}
