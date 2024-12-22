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
package org.openhab.binding.tesla.internal.protocol.dto.sso;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TokenResponse} is a datastructure to capture
 * authentication response from Tesla Remote Service
 *
 * @author Nicolai Grødum - Initial contribution
 */
public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("token_type")
    public String tokenType;
    @SerializedName("expires_in")
    public Long expiresIn;
    @SerializedName("created_at")
    public Long createdAt;
    @SerializedName("refresh_token")
    public String refreshToken;

    public TokenResponse() {
    }
}
