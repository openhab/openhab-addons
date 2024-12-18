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
 * The {@link AuthorizationCodeExchangeResponse} is a datastructure to capture
 * the response of an {@link AuthorizationCodeExchangeRequest}
 *
 * @author Christian Güdel - Initial contribution
 */
public class AuthorizationCodeExchangeResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("expires_in")
    public String expiresIn;
    public String state;
    @SerializedName("token_type")
    public String tokenType;
}
