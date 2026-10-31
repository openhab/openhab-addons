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
package org.openhab.binding.amazonechocontrol.internal.dto.request;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterAuthTO} encapsulates the auth information of an app registration request
 *
 * @author Jan N. Klug - Initial contribution
 * @author Jens Erat - Authorization code + PKCE flow
 */
public class AuthRegisterAuthTO {
    @SerializedName("access_token")
    public @Nullable String accessToken;
    @SerializedName("client_id")
    public @Nullable String clientId;
    @SerializedName("authorization_code")
    public @Nullable String authorizationCode;
    @SerializedName("code_verifier")
    public @Nullable String codeVerifier;
    @SerializedName("code_algorithm")
    public @Nullable String codeAlgorithm;
    @SerializedName("client_domain")
    public @Nullable String clientDomain;

    @Override
    public @NonNull String toString() {
        // accessToken, authorizationCode and codeVerifier are secrets and must not be exposed
        return "AuthRegisterAuthTO{clientId='" + clientId + "', codeAlgorithm='" + codeAlgorithm + "', clientDomain='"
                + clientDomain + "'}";
    }
}
