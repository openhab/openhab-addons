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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.List;
import java.util.StringJoiner;

import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.annotations.SerializedName;

/**
 * This is the Access Token Response, a simple value-object holding the result of an Access Token Request, as
 * provided by Netatmo API.
 * 
 * This is different from {@link AccessTokenResponse} because it violates RFC 6749 by having {@link #scope}
 * defined as an array of strings.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public final class NetatmoAccessTokenResponse {

    /**
     * The access token issued by the authorization server. It is used
     * by the client to gain access to a resource.
     */
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    /**
     * Number of seconds that this OAuthToken is valid for since the time it was created.
     */
    @SerializedName("expires_in")
    private long expiresIn;

    /**
     * Refresh token is a string representing the authorization granted to
     * the client by the resource owner. Unlike access tokens, refresh tokens are
     * intended for use only with authorization servers and are never sent
     * to resource servers.
     *
     */
    @SerializedName("refresh_token")
    private String refreshToken;

    /**
     * A list of scopes. This is not compliant with RFC 6749 which defines scope
     * as a list of space-delimited case-sensitive strings.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-3.3">rfc6749 section-3.3</a>
     */
    private List<Scope> scope;

    /**
     * State from prior access token request (if present).
     */
    private String state;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public List<Scope> getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "AccessTokenResponse [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn="
                + expiresIn + ", refreshToken=" + refreshToken + ", scope=" + scope + ", state=" + state + "]";
    }

    /**
     * Convert Netatmo-specific DTO to standard DTO in core resembling RFC 6749.
     * 
     * @return response converted into {@link AccessTokenResponse}
     */
    public AccessTokenResponse toStandard() {
        var standardResponse = new AccessTokenResponse();

        standardResponse.setAccessToken(accessToken);
        standardResponse.setTokenType(tokenType);
        standardResponse.setExpiresIn(expiresIn);
        standardResponse.setRefreshToken(refreshToken);

        StringJoiner stringJoiner = new StringJoiner(" ");
        scope.forEach(s -> stringJoiner.add(s.name().toLowerCase()));
        standardResponse.setScope(stringJoiner.toString());
        standardResponse.setState(state);

        return standardResponse;
    }
}
