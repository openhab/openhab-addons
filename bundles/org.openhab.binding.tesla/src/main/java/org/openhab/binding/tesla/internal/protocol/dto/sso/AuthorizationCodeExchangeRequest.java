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

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthorizationCodeExchangeRequest} is a datastructure to exchange
 * the authorization code for an access token on the SSO endpoint
 *
 * @author Christian Güdel - Initial contribution
 */
@SuppressWarnings("unused") // Unused fields must not be removed since they are used for serialization to JSON
public class AuthorizationCodeExchangeRequest {
    @SerializedName("grant_type")
    private String grantType = "authorization_code";
    @SerializedName("client_id")
    private String clientId = CLIENT_ID;
    private String code;
    @SerializedName("code_verifier")
    private String codeVerifier;
    @SerializedName("redirect_uri")
    private String redirectUri = URI_CALLBACK;

    public AuthorizationCodeExchangeRequest(String code, String codeVerifier) {
        this.code = code;
        this.codeVerifier = codeVerifier;
    }
}
