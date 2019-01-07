/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
