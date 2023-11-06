/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthResponseDTO} represents the response to an authentication request.
 * When authentication is enabled in Zoneminder, this object contains the access and
 * refresh tokens, as well as the number of seconds until the tokens expire.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AuthResponseDTO extends AbstractResponseDTO {

    /**
     * Access token to be used in all API calls
     */
    @SerializedName("access_token")
    public String accessToken;

    /**
     * Number of seconds until the access token expires
     */
    @SerializedName("access_token_expires")
    public String accessTokenExpires;

    /**
     * Refresh token to be used to request a new access token. A new access token
     * should be requested slightly before it is about to expire
     */
    @SerializedName("refresh_token")
    public String refreshToken;

    /**
     * Number of seconds until the refresh token expires
     */
    @SerializedName("refresh_token_expires")
    public String refreshTokenExpires;

    /**
     * Zoneminder version number
     */
    @SerializedName("version")
    public String version;

    /**
     * Zoneminder API version number
     */
    @SerializedName("apiversion")
    public String apiVersion;
}
