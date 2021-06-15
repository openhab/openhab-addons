/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.rest;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inserts Authorization and Cache-Control headers for requests on the streaming REST API.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Replace polling with REST streaming
 */
@NonNullByDefault
public class NestStreamingRequestFilter implements ClientRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(NestStreamingRequestFilter.class);

    private final ConcurrentHashMap<String, String> accessTokens = new ConcurrentHashMap<>();

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            String accessToken = accessTokens.get(requestContext.getUri().toString());
            if (accessToken != null) {
                headers.putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            } else {
                logger.warn("No access token set! uri={}", requestContext.getUri());
            }
            headers.putSingle(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
    }

    public void setAccessToken(String target, String token) {
        logger.debug("Set access token. target={}, token={}", target, token);
        accessTokens.put(target, token);
    }
}
