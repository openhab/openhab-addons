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
package org.openhab.binding.homeconnect.internal.logger;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * HTTP request log model.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class Request {
    private String url;
    private String method;
    private HashMap<String, String> header;
    private @Nullable String body;

    public Request(String url, String method, HashMap<String, String> header, @Nullable String body) {
        this.url = url;
        this.method = method;
        this.header = header;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public HashMap<String, String> getHeader() {
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
