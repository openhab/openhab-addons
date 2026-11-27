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
package org.openhab.binding.groheondus.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Token response of the login and refresh endpoints.
 *
 * @author Arne Seime - Initial contribution
 */
public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("expires_in")
    public int expiresIn;

    @Override
    public String toString() {
        // Never expose tokens
        return "TokenResponse[expiresIn=" + expiresIn + "]";
    }
}
