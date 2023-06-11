/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.remoteopenhab.internal.rest;

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
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RemoteopenhabStreamingRequestFilter implements ClientRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabStreamingRequestFilter.class);

    private final ConcurrentHashMap<String, String> credentialTokens = new ConcurrentHashMap<>();

    @Override
    public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
        if (requestContext != null) {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            String credentialToken = credentialTokens.get(requestContext.getUri().toString());
            if (credentialToken != null) {
                if (!credentialToken.isEmpty()) {
                    headers.putSingle(HttpHeaders.AUTHORIZATION, "Basic " + credentialToken);
                }
            } else {
                logger.warn("No credential token set! uri={}", requestContext.getUri());
            }
            headers.putSingle(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
    }

    public void setCredentialToken(String target, String token) {
        logger.debug("Set credential token. target={}, token={}", target, token);
        credentialTokens.put(target, token);
    }
}
