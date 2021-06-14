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
package org.openhab.binding.netatmo.internal.api.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.openhab.binding.netatmo.internal.api.NetatmoConstants.Scope;

/**
 * This is the Access Token Response, a simple value-object that holds the result of the
 * from an Access Token Request, as provided by Netatmo API.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public final class NAAccessTokenResponse implements Serializable, Cloneable {

    /**
     * For Serializable
     */
    private static final long serialVersionUID = 5512401378281693003L;

    /**
     * The access token issued by the authorization server. It is used
     * by the client to gain access to a resource.
     *
     * <p>
     * This token must be confidential in transit and storage.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.4">rfc6749 section-1.4</a>
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-10.3">rfc6749 section-10.3</a>
     */
    private String accessToken;

    /**
     * Number of seconds that this OAuthToken is valid for since the time it was created.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.2.2">rfc6749 section-4.2.2</a>
     */
    private long expiresIn;

    /**
     * Refresh token is a string representing the authorization granted to
     * the client by the resource owner. Unlike access tokens, refresh tokens are
     * intended for use only with authorization servers and are never sent
     * to resource servers.
     *
     * <p>
     * This token must be confidential in transit and storage.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.5">rfc6749 section-1.5</a>
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-10.4">rfc6749 section-10.4</a>
     */
    private String refreshToken;

    private List<Scope> scope;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public List<Scope> getScope() {
        return scope;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("not possible", e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, expiresIn, refreshToken, scope);
    }

    @Override
    public boolean equals(Object thatAuthTokenObj) {
        if (this == thatAuthTokenObj) {
            return true;
        }

        // Exact match since class is final
        if (thatAuthTokenObj == null || !this.getClass().equals(thatAuthTokenObj.getClass())) {
            return false;
        }

        NAAccessTokenResponse that = (NAAccessTokenResponse) thatAuthTokenObj;

        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.expiresIn, that.expiresIn)
                && Objects.equals(this.refreshToken, that.refreshToken) && Objects.equals(this.scope, that.scope);
    }

    @Override
    public String toString() {
        return "NAAccessTokenResponse [accessToken=" + accessToken + ", expiresIn=" + expiresIn + ", refreshToken="
                + refreshToken + ", scope=" + scope + "]";
    }
}
