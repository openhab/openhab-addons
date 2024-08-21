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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JsonExchangeTokenResponse} encapsulate the GSON response data of the token exchange
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonExchangeTokenResponse {
    public @Nullable Response response;

    public static class Response {
        public @Nullable Tokens tokens;
    }

    public static class Tokens {
        public @Nullable Map<String, Cookie[]> cookies;
    }

    public static class Cookie {
        @SerializedName("Path")
        public @Nullable String path;
        @SerializedName("Secure")
        public @Nullable Boolean secure;
        @SerializedName("Value")
        public @Nullable String value;
        @SerializedName("Expires")
        public @Nullable String expires;
        @SerializedName("HttpOnly")
        public @Nullable Boolean httpOnly;
        @SerializedName("Name")
        public @Nullable String name;
    }
}
