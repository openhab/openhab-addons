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
package org.openhab.binding.homeconnect.internal.client.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * HTTP response model.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class HomeConnectResponse {
    private final int code;
    private final Map<String, String> header;
    private final @Nullable String body;

    public HomeConnectResponse(int code, Map<String, String> header, @Nullable String body) {
        this.code = code;
        this.header = header;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public @Nullable String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Response [code=" + code + ", header=" + header + ", body=" + body + "]";
    }
}
