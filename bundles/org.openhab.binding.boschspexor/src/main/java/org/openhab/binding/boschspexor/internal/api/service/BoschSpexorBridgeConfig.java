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
package org.openhab.binding.boschspexor.internal.api.service;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Main settings.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class BoschSpexorBridgeConfig {

    /**
     * The host name of the Bosch spexor backend.
     */
    private static final String HOST = "https://api.spexor-bosch.com/";

    /**
     * The OAuth2 client id
     */
    private static final String CLIENTID = "spexor-3rdparty-service-auth";
    /**
     * The OAuth2 scope
     */
    private static final String SCOPE = "3rdparty-service";
    /**
     * Interval (in seconds) at which state updates are polled.
     */
    private int refreshInterval = 60; // Default: Every 1min

    /**
     * The token path to request the OAuth2 token.
     */
    private static final String TOKEN_URL = "api/public/token";
    /**
     * The refresh token path to request the OAuth2 token.
     */
    private static final String REFRESH_URL = "api/public/refresh";
    /**
     * The token path to request the OAuth2 token.
     */
    private static final String AUTHORIZATION_URL = "api/public/auth";

    public String getHost() {
        return HOST;
    }

    public String getClientId() {
        return CLIENTID;
    }

    public String getScope() {
        return SCOPE;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public String getRefreshUrl() {
        return REFRESH_URL;
    }

    public String getTokenUrl() {
        return TOKEN_URL;
    }

    public String getAuthorizationUrl() {
        return AUTHORIZATION_URL;
    }

    public String buildTokenUrl() {
        return getHost() + TOKEN_URL;
    }

    public String buildAuthorizationUrl() {
        return getHost() + AUTHORIZATION_URL;
    }

    public String buildRefreshUrl() {
        return getHost() + REFRESH_URL;
    }
}
