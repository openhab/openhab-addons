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
package org.openhab.binding.pleos.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Data model for the token response of the Pleos API.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosAuthModels {

    // What we send to Pleos
    public record TokenRequest(String clientId, String clientSecret) {
    }

    // What Pleos sends back: {"data": {"accessToken": "...", "expiresIn": 3600}}
    public record TokenResponse(@Nullable TokenData data) {
        public record TokenData(@SerializedName(value = "accessToken", alternate = {
                "access_token" }) @Nullable String accessToken,
                @SerializedName(value = "expiresIn", alternate = { "expires_in" }) @Nullable Long expiresIn) {
        }
    }
}
