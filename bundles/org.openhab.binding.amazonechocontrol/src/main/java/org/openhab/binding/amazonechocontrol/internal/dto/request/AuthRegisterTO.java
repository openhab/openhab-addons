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
package org.openhab.binding.amazonechocontrol.internal.dto.request;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterTO} encapsulate the app registration request
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterTO {
    @SerializedName("requested_extensions")
    public List<String> requestedExtensions = List.of("device_info", "customer_info");
    public AuthRegisterCookiesTO cookies = new AuthRegisterCookiesTO();
    @SerializedName("registration_data")
    public AuthRegisterRegistrationTO registrationData = new AuthRegisterRegistrationTO();
    @SerializedName("auth_data")
    public AuthRegisterAuthTO authData = new AuthRegisterAuthTO();
    @SerializedName("user_context_map")
    public Map<String, String> userContextMap = Map.of();
    @SerializedName("requested_token_type")
    public List<String> requestedTokenType = List.of("bearer", "mac_dms", "website_cookies");

    @Override
    public @NonNull String toString() {
        return "AuthRegisterTO{requestedExtensions=" + requestedExtensions + ", cookies=" + cookies
                + ", registrationData=" + registrationData + ", authData=" + authData + ", userContextMap="
                + userContextMap + ", requestedTokenType=" + requestedTokenType + "}";
    }
}
