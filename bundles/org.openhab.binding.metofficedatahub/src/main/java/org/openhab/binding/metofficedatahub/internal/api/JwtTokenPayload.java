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
package org.openhab.binding.metofficedatahub.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JwtTokenPayload} allows the basic decoding of a JWT token header, to allow
 * basic validation before using it.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class JwtTokenPayload {
    @SerializedName("sub")
    private String sub = "";

    @SerializedName("iss")
    private String iss = "";

    @SerializedName("keytype")
    private String keyType = "";

    @SerializedName("token_type")
    private String tokenType = "";

    public boolean isValid() {
        if (sub.isBlank() || iss.isBlank() || keyType.isBlank() || tokenType.isBlank()) {
            return false;
        }

        return "apiKey".contentEquals(tokenType);
    }
}
