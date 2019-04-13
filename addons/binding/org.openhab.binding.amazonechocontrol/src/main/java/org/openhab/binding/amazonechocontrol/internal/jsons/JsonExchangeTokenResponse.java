/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * The {@link JsonExchangeTokenResponse} encapsulate the GSON response data of the token exchange
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonExchangeTokenResponse {
    public @Nullable Response response;

    public class Response {
        public @Nullable Tokens tokens;
    }

    public class Tokens {
        public @Nullable Map<String, Cookie[]> cookies;
    }

    public class Cookie {
        public @Nullable String Path;
        public @Nullable Boolean Secure;
        public @Nullable String Value;
        public @Nullable String Expires;
        public @Nullable Boolean HttpOnly;
        public @Nullable String Name;

    }
}
