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
 * The {@link JwtTokenHeader} allows the basic decoding of a JWT token header, to allow
 * basic validation before using it.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class JwtTokenHeader {
    @SerializedName("x5t")
    private String x5t = "";

    @SerializedName("kid")
    private String kid = "";

    @SerializedName("typ")
    private String typ = "";

    @SerializedName("alg")
    private String alg = "";

    public boolean isValid() {
        if (x5t.isBlank() || kid.isBlank() || typ.isBlank() || alg.isBlank()) {
            return false;
        }

        return "JWT".contentEquals(typ);
    }
}
