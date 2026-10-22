/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bmwcardata.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Response of the GCDM token endpoint, either a successful token response or an OAuth error response.
 * Besides the standard OAuth fields, BMW returns the {@code gcid} which identifies the customer on the streaming
 * broker.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class TokenResponse {
    @SerializedName("access_token")
    public @Nullable String accessToken;
    @SerializedName("refresh_token")
    public @Nullable String refreshToken;
    @SerializedName("id_token")
    public @Nullable String idToken;
    public @Nullable String gcid;
    @SerializedName("expires_in")
    public long expiresIn;
    public @Nullable String error;
    @SerializedName("error_description")
    public @Nullable String errorDescription;

    @Override
    public String toString() {
        // tokens are deliberately omitted
        return "TokenResponse [expiresIn=" + expiresIn + ", error=" + error + ", errorDescription=" + errorDescription
                + "]";
    }
}
