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
package org.openhab.binding.homeconnect.internal.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inserts Authorization header for requests on the streaming REST API.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class HomeConnectStreamingRequestFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final String TEXT_EVENT_STREAM = "text/event-stream";

    private final Logger logger = LoggerFactory.getLogger(HomeConnectStreamingRequestFilter.class);
    private final ConcurrentHashMap<String, String> authorizationHeaders = new ConcurrentHashMap<>();

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            String authorizationHeader = authorizationHeaders.get(requestContext.getUri().toString());
            if (authorizationHeader != null) {
                headers.putSingle(HttpHeaders.AUTHORIZATION, authorizationHeader);
            } else {
                logger.warn("No authorization header set! uri={}", requestContext.getUri());
            }
            headers.putSingle(HttpHeaders.CACHE_CONTROL, "no-cache");
            headers.putSingle(HttpHeaders.ACCEPT, TEXT_EVENT_STREAM);
        }
    }

    @Override
    public void filter(@Nullable ClientRequestContext requestContext, @Nullable ClientResponseContext responseContext)
            throws IOException {
        if (logger.isDebugEnabled() && requestContext != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("SSE connection: ");
            sb.append(requestContext.getUri()).append("\n");
            requestContext.getHeaders()
                    .forEach((name, value) -> sb.append("> ").append(name).append(": ").append(value).append("\n"));

            if (responseContext != null) {
                responseContext.getHeaders()
                        .forEach((name, value) -> sb.append("< ").append(name).append(": ").append(value).append("\n"));
            }

            logger.debug("{}", sb);
        }
    }

    public void setAuthorizationHeader(String target, String header) {
        logger.debug("Set authorization header. target={}, header={}", target, header);
        authorizationHeaders.put(target, header);
    }
}
