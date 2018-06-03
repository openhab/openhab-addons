/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.rest;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Inserts Authorization and Cache-Control headers for requests on the streaming REST API.
 *
 * @author Wouter Born - Replace polling with REST streaming
 */
@NonNullByDefault
public class NestStreamingRequestFilter implements ClientRequestFilter {
    private final String accessToken;

    public NestStreamingRequestFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
    }
}
