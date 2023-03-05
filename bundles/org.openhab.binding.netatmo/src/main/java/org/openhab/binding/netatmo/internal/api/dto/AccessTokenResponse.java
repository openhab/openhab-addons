/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.Scope;

/**
 * This is the Access Token Response, a simple value-object holding the result of an Access Token Request, as
 * provided by Netatmo API.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public final class AccessTokenResponse {

    /**
     * The access token issued by the authorization server. It is used
     * by the client to gain access to a resource.
     *
     */
    private String accessToken;

    /**
     * Number of seconds that this OAuthToken is valid for since the time it was created.
     *
     */
    private long expiresIn;

    /**
     * Refresh token is a string representing the authorization granted to
     * the client by the resource owner. Unlike access tokens, refresh tokens are
     * intended for use only with authorization servers and are never sent
     * to resource servers.
     *
     */
    private String refreshToken;

    private List<Scope> scope;

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
        return "AccessTokenResponse [accessToken=" + accessToken + ", expiresIn=" + expiresIn + ", refreshToken="
                + refreshToken + ", scope=" + scope + "]";
    }
}
