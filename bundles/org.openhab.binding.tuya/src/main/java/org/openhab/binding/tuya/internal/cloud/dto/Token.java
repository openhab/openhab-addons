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
package org.openhab.binding.tuya.internal.cloud.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Token} encapsulates the Access Tokens
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class Token {
    @SerializedName("access_token")
    public final String accessToken;
    @SerializedName("refresh_token")
    public final String refreshToken;
    public final String uid;
    @SerializedName("expire_time")
    public final long expire;

    public transient long expireTimestamp = 0;

    public Token() {
        this("", "", "", 0);
    }

    public Token(String accessToken, String refreshToken, String uid, long expire) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.uid = uid;
        this.expire = expire;
    }

    @Override
    public String toString() {
        return "Token{accessToken='" + accessToken + "', refreshToken='" + refreshToken + "', uid='" + uid
                + "', expire=" + expire + "', expireTimestamp=" + expireTimestamp + "}";
    }
}
