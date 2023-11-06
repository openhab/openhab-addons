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
package org.openhab.binding.homeconnect.internal.client.model;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Token model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Token {

    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpirationInSeconds;

    public Token(String accessToken, String refreshToken, long accessTokenExpirationInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpirationInSeconds = accessTokenExpirationInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpirationInSeconds;
    }

    @Override
    public String toString() {
        return "Token [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", accessTokenExpiration="
                + new Date(accessTokenExpirationInSeconds) + "]";
    }
}
