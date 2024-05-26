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

/**
 * The {@link APIdata} is the internal class for a Sunsynk Connect
 * Account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class APIdata {
    public static String static_access_token = "";
    private String access_token = "";
    private String refresh_token = "";
    private String token_type = "";
    private Long expires_in = 0000L;
    private String scope = "";
    private Long issued_at = Instant.now().getEpochSecond();

    public Long getExpiresIn() {
        return this.expires_in;
    }

    public String getRefreshToken() {
        return this.refresh_token;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public String getScope() {
        return this.scope;
    }

    public String getAccessToken() {
        return this.access_token;
    }

    public Long getIssuedAt() {
        return this.issued_at;
    }

    public void setIssuedAt(Long issued_at) {
        this.issued_at = issued_at;
    }

    @Override
    public String toString() {
        return "[access token: " + this.access_token + ", refresh_token: " + this.refresh_token + ", expires_in: "
                + this.expires_in + ", issued_at: " + this.issued_at + "]";
    }
}
