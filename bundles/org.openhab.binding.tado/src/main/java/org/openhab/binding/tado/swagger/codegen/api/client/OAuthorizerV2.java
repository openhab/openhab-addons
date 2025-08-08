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
package org.openhab.binding.tado.swagger.codegen.api.client;

import java.io.IOException;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a new authorizer that was mandated by Tado after March 15 2025.
 * <p>
 * Based on static imported copy of class created by Swagger Codegen
 *
 * @see <a href="https://support.tado.com/en/articles/8565472-how-do-i-authenticate-to-access-the-rest-api">Tado Support
 *      Article</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8628">RFC-8628</a>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class OAuthorizerV2 {

    private final Logger logger = LoggerFactory.getLogger(OAuthorizerV2.class);

    private final OAuthClientService oAuthService;

    public OAuthorizerV2(OAuthClientService oAuthService) {
        this.oAuthService = oAuthService;
    }

    public void addAuthorization(Request request) {
        try {
            AccessTokenResponse token = oAuthService.getAccessTokenResponse();
            if (token != null) {
                request.header(HttpHeader.AUTHORIZATION,
                        String.format("%s %s", token.getTokenType(), token.getAccessToken()));
                return;
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.debug("addAuthorization() => getAccessTokenResponse() error: {}", e.getMessage(), e);
        }
    }
}
