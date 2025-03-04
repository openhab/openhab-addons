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
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.tado.internal.auth.oauth.DeviceCodeGrantFlowService;
import org.openhab.binding.tado.internal.handler.TadoHomeHandler;
import org.openhab.binding.tado.swagger.codegen.api.ApiException;
import org.openhab.binding.tado.swagger.codegen.api.auth.Authorizer;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a new {@link Authorizer} that is mandated by Tado after March 15 2025.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class AuthorizerV2 implements Authorizer, AutoCloseable {

    private static final String DEVICE_URL = "https://login.tado.com/oauth2/device_authorize";
    private static final String TOKEN_URL = "https://login.tado.com/oauth2/token";
    private static final String CLIENT_ID = "1bb50063-6b0c-4d11-bd99-387f4a91cc46";
    private static final String SCOPE = "offline_access";

    private final Logger logger = LoggerFactory.getLogger(AuthorizerV2.class);
    private final DeviceCodeGrantFlowService oAuthService;

    public AuthorizerV2(ScheduledExecutorService scheduler, HttpClient httpClient, OAuthFactory oAuthFactory,
            TadoHomeHandler handler) {
        String handle = handler.getThing().getUID().toString();
        oAuthService = new DeviceCodeGrantFlowService(scheduler, httpClient, oAuthFactory, handler, handle, TOKEN_URL,
                DEVICE_URL, CLIENT_ID, SCOPE);
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

    public @Nullable String beginAuthenticationAndGetUserUri() {
        return oAuthService.beginAuthenticationAndGetUserUri();
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
