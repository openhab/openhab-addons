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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthTokenTO} encapsulates the response of a request to /auth/token
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthTokenTO {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("token_type")
    public String tokenType;
    @SerializedName("expires_in")
    public long expiresIn;

    @Override
    public @NonNull String toString() {
        return "AuthTokenTO{accessToken='" + accessToken + "', tokenType='" + tokenType + "', expiresIn=" + expiresIn
                + "}";
    }
}
