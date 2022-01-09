/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.dto.oauth;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TokenResponseDTO} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
public class TokenResponseDTO extends AbstractAuthResponseDTO {

    /*
     * Access token to be used in future API requests.
     */
    @SerializedName("access_token")
    public String accessToken;

    /*
     * Contains the string "Bearer"
     */
    @SerializedName("token_type")
    public String tokenType;

    /*
     * Number of seconds until the access token will expire.
     */
    @SerializedName("expires_in")
    public Integer expiresIn;

    /*
     * Token used to request a new access token.
     */
    @SerializedName("refresh_token")
    public String refreshToken;

    /*
     * Matches the scope included in the token request.
     */
    @SerializedName("scope")
    public String scope;
}
