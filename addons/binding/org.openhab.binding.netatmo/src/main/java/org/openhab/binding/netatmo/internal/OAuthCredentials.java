/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

/**
 * This class holds the different credentials necessary for the OAuth2 flow to
 * work. It also provides basic methods to refresh the access token.
 *
 * @author Thomas.Eichstaedt-Engelen
 * @since 1.6.0
 */
public class OAuthCredentials {

    /**
     * The client id to access the Netatmo API. Normally set in
     * <code>openhab.cfg</code>.
     *
     * @see <a href="http://dev.netatmo.com/doc/authentication/usercred">Client
     *      Credentials</a>
     */
    public String clientId;
    /**
     * The client secret to access the Netatmo API. Normally set in
     * <code>openhab.cfg</code>.
     *
     * @see <a href="http://dev.netatmo.com/doc/authentication/usercred">Client
     *      Credentials</a>
     */
    public String clientSecret;
    /**
     * The refresh token to access the Netatmo API. Normally set in
     * <code>openhab.cfg</code>.
     *
     * @see <a
     *      href="http://dev.netatmo.com/doc/authentication/usercred">Client&nbsp;Credentials</a>
     * @see <a
     *      href="http://dev.netatmo.com/doc/authentication/refreshtoken">Refresh&nbsp;Token</a>
     */
    public String refreshToken;
    /**
     * The access token to access the Netatmo API. Automatically renewed from
     * the API using the refresh token.
     *
     * @see <a
     *      href="http://dev.netatmo.com/doc/authentication/refreshtoken">Refresh
     *      Token</a>
     * @see #refreshAccessToken()
     */
    public String accessToken;

    public OAuthCredentials(String clientId, String clientSecret, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public boolean noAccessToken() {
        return this.accessToken == null;
    }
}
