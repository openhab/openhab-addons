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
package org.openhab.binding.netatmo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.Scope;

/**
 * This is the Access Token Response, a simple value-object that holds the result of the
 * from an Access Token Request, as listed in RFC 6749:
 * 4.1.4 - Authorization Code grant - Access Token Response,
 * 4.2.2 - Implicit Grant - Access Token Response,
 * 4.3.3 - Resource Owner Password Credentials Grant - Access Token Response
 * 4.4.3 - Client Credentials Grant - Access Token Response
 *
 * @author Michael Bock - Initial contribution
 * @author Gary Tse - Adaptation for Eclipse SmartHome
 * @author Gaël L'hopital - Adapted core implementation for Netatmo API
 */
public final class NAAccessTokenResponse implements Serializable, Cloneable {

    /**
     * For Serializable
     */
    private static final long serialVersionUID = 4837164195629364014L;

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
     * Token type. e.g. Bearer, MAC
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-7.1">rfc6749 section-7.1</a>
     */
    private String tokenType;

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

    /**
     * A space-delimited case-sensitive un-ordered string. This may be used
     * by the authorization server to inform the client of the scope of the access token
     * issued.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-3.3">rfc6749 section-3.3</a>
     */
    private List<Scope> scope;

    /**
     * If the {@code state} parameter was present in the access token request.
     * The exact value should be returned as-is from the authorization provider.
     *
     * <a href="https://tools.ietf.org/html/rfc6749#section-4.2.2">rfc6749 section-4.2.2</a>
     */
    private String state;

    /**
     * Created datetime of this access token. This is generated locally
     * by the OAUTH client as at the time the access token is received.
     *
     * This should be slightly later than the actual time the access token
     * is produced at the server.
     */
    private LocalDateTime createdOn;

    /**
     * Calculate if the token is expired against the given time.
     * It also returns true even if the token is not initialized (i.e. object newly created).
     *
     * @param givenTime To calculate if the token is expired against the givenTime.
     * @param tokenExpiresInBuffer A positive integer in seconds to act as additional buffer to the calculation.
     *            This causes the OAuthToken to expire earlier then the stated expiry-time given
     *            by the authorization server.
     * @return true if object is not-initialized, or expired, or expired early due to buffer
     */
    public boolean isExpired(@NonNull LocalDateTime givenTime, int tokenExpiresInBuffer) {
        return createdOn == null
                || createdOn.plusSeconds(expiresIn).minusSeconds(tokenExpiresInBuffer).isBefore(givenTime);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
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

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<Scope> getScope() {
        return scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
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
        return Objects.hash(accessToken, tokenType, expiresIn, refreshToken, scope, state, createdOn);
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

        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.tokenType, that.tokenType)
                && Objects.equals(this.expiresIn, that.expiresIn)
                && Objects.equals(this.refreshToken, that.refreshToken) && Objects.equals(this.scope, that.scope)
                && Objects.equals(this.state, that.state) && Objects.equals(this.createdOn, that.createdOn);
    }

    @Override
    public String toString() {
        return "AccessTokenResponse [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn="
                + expiresIn + ", refreshToken=" + refreshToken + ", scope=" + scope + ", state=" + state
                + ", createdOn=" + createdOn + "]";
    }
}
