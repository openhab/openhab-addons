/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tado.internal.auth;

import java.io.IOException;
import java.time.Instant;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a new {@link Authorizer} that is mandated by Tado after March 15 2025.
 *
 * @see <a href="https://support.tado.com/en/articles/8565472-how-do-i-authenticate-to-access-the-rest-api">Tado Support
 *      Article</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8628">RFC-8628</a>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class OAuthorizerV2 implements Authorizer {

    private final Logger logger = LoggerFactory.getLogger(OAuthorizerV2.class);

    private final OAuthClientService oAuthService;

    private @Nullable AccessTokenResponse cachedAccessTokenResponse;

    public OAuthorizerV2(OAuthClientService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Override
    public void addAuthorization(Request request) {
        AccessTokenResponse token = getCachedAccessTokenResponse();
        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION,
                    String.format("%s %s", token.getTokenType(), token.getAccessToken()));
        }
    }

    /**
     * Use the cached {@link AccessTokenResponse} if available and not close to expiring. Otherwise fetch the
     * token from the OAUTH service. The method is synchronized to prevent multiple concurrent HTTP token
     * refresh calls.
     */
    private synchronized @Nullable AccessTokenResponse getCachedAccessTokenResponse() {
        AccessTokenResponse token = this.cachedAccessTokenResponse;
        if (token == null || token.isExpired(Instant.now(), 5)) {
            try {
                token = oAuthService.getAccessTokenResponse();
                this.cachedAccessTokenResponse = token;
            } catch (OAuthException | IOException | OAuthResponseException e) {
                logger.debug("getAccessTokenResponse() error: {}", e.getMessage(), e);
                this.cachedAccessTokenResponse = null;
            }
        }
        return token;
    }
}
