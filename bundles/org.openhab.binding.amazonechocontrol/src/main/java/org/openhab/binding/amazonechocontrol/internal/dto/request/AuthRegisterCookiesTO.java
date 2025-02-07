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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.amazonechocontrol.internal.dto.CookieTO;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AuthRegisterCookiesTO} encapsulates the cookie information for a given domain
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AuthRegisterCookiesTO {
    @SerializedName("website_cookies")
    public List<CookieTO> webSiteCookies = List.of();
    public String domain = ".amazon.com";

    @Override
    public @NonNull String toString() {
        return "AuthRegisterCookiesTO{webSiteCookies=" + webSiteCookies + ", domain='" + domain + "'}";
    }
}
