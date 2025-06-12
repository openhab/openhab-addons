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
package org.openhab.binding.ring.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TokenTO} encapsulates the OAuth token response
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TokenTO {

    @SerializedName("access_token")
    public String accessToken = "";

    @SerializedName("refresh_token")
    public String refreshToken = "";

    @SerializedName("scope")
    public String scope = "";

    @SerializedName("token_type")
    public String tokenType = "";

    @SerializedName("expires_in")
    public int expiresIn = 14400;
}
