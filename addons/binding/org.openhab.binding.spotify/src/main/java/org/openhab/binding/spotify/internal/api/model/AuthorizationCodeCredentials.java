/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal.api.model;

/**
 * This class and its inner classes represents the Spotify Web API response of an authorization request
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class AuthorizationCodeCredentials {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private int expiresIn;

    /**
     *
     */
    private String user;

    public AuthorizationCodeCredentials() {
    }

    public AuthorizationCodeCredentials(String user, String refreshToken) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.accessToken = "";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "AuthorizationCodeCredentials [accessToken=" + accessToken + ", refreshToken=" + refreshToken
                + ", tokenType=" + tokenType + ", scope=" + scope + ", expiresIn=" + expiresIn + ", user=" + user + "]";
    }
}
