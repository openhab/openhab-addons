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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link APIdata} is the internal class for a Sun Synk Connect
 * Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class APIdata {
    @SerializedName("static_access_token")
    public static String staticAccessToken = "";
    @SerializedName("access_token")
    private String accessToken = "";
    @SerializedName("refresh_token")
    private String refreshToken = "";
    @SerializedName("token_type")
    private String tokenType = "";
    @SerializedName("expires_in")
    private Long expiresIn = 0000L;
    private String scope = "";
    private Long issuedAt = Instant.now().getEpochSecond();

    public Long getExpiresIn() {
        return this.expiresIn;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public String getScope() {
        return this.scope;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public Long getIssuedAt() {
        return this.issuedAt;
    }

    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return "[access token: " + this.accessToken + ", refresh_token: " + this.refreshToken + ", expires_in: "
                + this.expiresIn + ", issued_at: " + this.issuedAt + "]";
    }
}
