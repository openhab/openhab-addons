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
package org.openhab.binding.amazonechocontrol.internal.dto;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link CookieTO} encapsulates a cookie
 *
 * @author Jan N. Klug - Initial contribution
 */
public class CookieTO {
    @SerializedName("Path")
    public String path;
    @SerializedName("Secure")
    public String secure;
    @SerializedName("Value")
    public String value;
    @SerializedName("Expires")
    public String expires;
    @SerializedName("HttpOnly")
    public String httpOnly;
    @SerializedName("Name")
    public String name;

    @Override
    public @NonNull String toString() {
        return "CookieTO{path='" + path + "', secure=" + secure + ", value='" + value + "', expires='" + expires
                + "', httpOnly=" + httpOnly + ", name='" + name + "'}";
    }
}
