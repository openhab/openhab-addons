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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TokenRefresh} is the internal class for for reconnection
 * to a Sun Synk Connect Account.
 * Login via Username and Refresh Token
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class TokenRefresh {
    // {"grant_type":"refresh_token", "username":"xxx", "refresh_token":"xxx", "client_id\":"csp-web\"}
    @SerializedName("grant_type")
    private String grantType = "refresh_token";
    @SerializedName("username")
    private String userName = "";
    @SerializedName("refresh_token")
    private String refreshToken = "";
    @SerializedName("client_id")
    private String clientId = "csp-web";

    public TokenRefresh(String UserName, String RefreshToken) {
        this.userName = UserName;
        this.refreshToken = RefreshToken;
    }
}
