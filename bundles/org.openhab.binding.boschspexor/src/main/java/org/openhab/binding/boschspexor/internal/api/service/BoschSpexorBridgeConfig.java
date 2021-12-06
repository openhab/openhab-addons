/* Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * @author Marc Fischer - Initial contribution.
 */
@NonNullByDefault
public class BoschSpexorBridgeConfig {

    /**
     * The host name of the Bosch spexor backend.
     */
    private String host = "https://api.bosch-spexor.com/";
    private String hostQA = "https://api-q.bosch-spexor.com/";

    private boolean testing = false;
    /**
     * The OAuth2 client id
     */
    private String clientId = "spexor-3rdparty-service-auth";
    /**
     * The OAuth2 scope
     */
    private String scope = "3rdparty-service";
    /**
     * Interval (in seconds) at which state updates are polled.
     */
    private int refreshInterval = 60; // Default: Every 1min

    /**
     * The token path to request the OAuth2 token.
     */
    private String tokenUrl = "api/public/token";
    /**
     * The refresh token path to request the OAuth2 token.
     */
    private String refreshUrl = "api/public/refresh";
    /**
     * The token path to request the OAuth2 token.
     */
    private String authorizationUrl = "api/public/auth";

    public String getHost() {
        return testing ? hostQA : host;
    }

    public String getClientId() {
        return clientId;
    }

    public String getScope() {
        return scope;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public String buildTokenUrl() {
        return String.join("", getHost(), tokenUrl);
    }

    public String buildAuthorizationUrl() {
        return String.join("", getHost(), authorizationUrl);
    }

    public String buildRefreshUrl() {
        return String.join("", getHost(), refreshUrl);
    }

    public boolean isTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        this.testing = testing;
    }
}
