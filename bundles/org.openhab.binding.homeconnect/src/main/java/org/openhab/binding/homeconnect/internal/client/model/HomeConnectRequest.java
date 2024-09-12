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

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.API_BASE_URL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.API_SIMULATOR_BASE_URL;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * HTTP request model.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class HomeConnectRequest {
    private final String url;
    private final String method;
    private final Map<String, String> header;
    private @Nullable final String body;

    public HomeConnectRequest(String url, String method, Map<String, String> header, @Nullable String body) {
        this.url = url;
        this.method = method;
        this.header = header;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public String getShortUrl() {
        return url.replace(API_BASE_URL, "").replace(API_SIMULATOR_BASE_URL, "");
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public @Nullable String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request [url=" + url + ", method=" + method + ", header=" + header + ", body=" + body + "]";
    }
}
