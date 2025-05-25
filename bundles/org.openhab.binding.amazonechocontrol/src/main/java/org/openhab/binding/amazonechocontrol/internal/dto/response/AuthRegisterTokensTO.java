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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.amazonechocontrol.internal.dto.CookieTO;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterTokensTO} encapsulates all tokens for a connection registration response
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterTokensTO {
    @SerializedName("website_cookies")
    public List<CookieTO> websiteCookies = List.of();
    @SerializedName("mac_dms")
    public AuthRegisterMacDmsTokenTO macDms = new AuthRegisterMacDmsTokenTO();
    public AuthRegisterBearerTokenTO bearer = new AuthRegisterBearerTokenTO();

    @Override
    public @NonNull String toString() {
        return "AuthRegisterTokensTO{websiteCookies=" + websiteCookies + ", macDms=" + macDms + ", bearer=" + bearer
                + "}";
    }
}
