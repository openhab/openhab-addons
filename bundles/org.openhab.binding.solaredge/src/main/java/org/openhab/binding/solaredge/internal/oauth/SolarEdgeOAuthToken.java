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
package org.openhab.binding.solaredge.internal.oauth;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Token response returned by the SolarEdge OAuth API.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeOAuthToken {
    @SerializedName("access_token")
    public String accessToken = "";
    @SerializedName("refresh_token")
    public String refreshToken = "";
    @SerializedName("token_type")
    public String tokenType = "";
    @SerializedName("expires_in")
    public long expiresIn;
}
