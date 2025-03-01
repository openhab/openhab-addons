/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronAccessTokenData} class defines the data object for access token
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronAccessTokenData {
    @SerializedName("access-token")
    private String accessToken = "";

    @SerializedName("access-token-expiration")
    private String accessTokenExpiration = "";

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
