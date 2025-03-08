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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
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
public class OAuthorizerV2 implements Authorizer, AutoCloseable {

    private static final String DEVICE_URL = "https://login.tado.com/oauth2/device_authorize";
    private static final String TOKEN_URL = "https://login.tado.com/oauth2/token";
    private static final String CLIENT_ID = "1bb50063-6b0c-4d11-bd99-387f4a91cc46";
    private static final String SCOPE = "offline_access";

    private final Logger logger = LoggerFactory.getLogger(OAuthorizerV2.class);
    private final OAuthClientService oAuthService;

    public OAuthorizerV2(OAuthFactory oAuthFactory, String handle) {
        oAuthService = oAuthFactory.createOAuthRfc8628ClientService(handle, TOKEN_URL, DEVICE_URL, CLIENT_ID, SCOPE);
    }

    @Override
    public void addAuthorization(Request request) throws ApiException, IOException {
        try {
            AccessTokenResponse token = oAuthService.getAccessTokenResponse();
            if (token != null) {
                logger.trace("addAuthorization():  accessToken:{}", token);
                request.header(HttpHeader.AUTHORIZATION,
                        String.format("%s %s", token.getTokenType(), token.getAccessToken()));
            }
            throw new ApiException("addAuthorization(): access token is null");
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new ApiException("addAuthorization(): Exception", e);
        }
    }

    public @Nullable String getRfc8628AuthenticationUserUri() {
        try {
            return oAuthService.getUserAuthenticationUri();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.warn("addAuthorization()  error:{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        oAuthService.close();
    }

    public @Nullable AccessTokenResponse getAccessTokenResponse()
            throws OAuthException, IOException, OAuthResponseException {
        return oAuthService.getAccessTokenResponse();
    }
}
