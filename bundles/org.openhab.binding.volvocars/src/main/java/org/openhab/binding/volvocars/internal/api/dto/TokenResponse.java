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
package org.openhab.binding.volvocars.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Token response of the Volvo ID OAuth 2.0 token endpoint.
 *
 * @author Michael Weger - Initial contribution
 */
public class TokenResponse {
    @SerializedName("access_token")
    public @Nullable String accessToken;
    @SerializedName("refresh_token")
    public @Nullable String refreshToken;
    @SerializedName("expires_in")
    public long expiresIn;
}
