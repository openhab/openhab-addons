/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.rest;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Inserts Authorization and Cache-Control headers for requests on the streaming WWN REST API.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Replace polling with REST streaming
 */
@NonNullByDefault
public class WWNStreamingRequestFilter implements ClientRequestFilter {
    private final String accessToken;

    public WWNStreamingRequestFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            headers.putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.putSingle(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
    }
}
