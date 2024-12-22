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
package org.openhab.binding.huesync.internal.connection;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Authentication.Result;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncAuthenticationResult implements Result {
    private final String token;
    private final URI uri;

    public HueSyncAuthenticationResult(URI uri, String token) {
        this.uri = uri;
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public void apply(@Nullable Request request) {
        if (request != null && !request.getHeaders().contains(HttpHeader.AUTHORIZATION)) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.token);
        }
    }
}
