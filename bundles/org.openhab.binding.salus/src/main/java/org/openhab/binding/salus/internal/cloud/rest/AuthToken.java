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
package org.openhab.binding.salus.internal.cloud.rest;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public record AuthToken(@SerializedName("access_token") String accessToken,
        @SerializedName("refresh_token") String refreshToken, @SerializedName("expires_in") Long expiresIn,
        @SerializedName("role") String role) {
    public AuthToken {
        Objects.requireNonNull(accessToken, "accessToken cannot be null!");
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null!");
    }

    @Override
    public String toString() {
        return "AuthToken{" + "accessToken='<SECRET>'" + ", refreshToken='<SECRET>'" + ", expiresIn=" + expiresIn
                + ", role='" + role + '\'' + '}';
    }
}
