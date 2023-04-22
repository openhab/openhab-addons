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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tesla.internal.protocol.sso.RefreshTokenRequest;
import org.openhab.binding.tesla.internal.protocol.sso.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TeslaSSOHandler} is responsible for authenticating with the Tesla SSO service.
 *
 * @author Christian Güdel - Initial contribution
 */
@NonNullByDefault
public class TeslaSSOHandler {

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(TeslaSSOHandler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public TeslaSSOHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nullable
    public TokenResponse getAccessToken(String refreshToken) {
        logger.debug("Exchanging SSO refresh token for API access token");

        // get a new access token for the owner API token endpoint
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        String refreshTokenPayload = gson.toJson(refreshRequest);

        final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(URI_SSO + "/" + PATH_TOKEN);
        request.content(new StringContentProvider(refreshTokenPayload));
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.method(HttpMethod.POST);

        ContentResponse refreshResponse = executeHttpRequest(request);

        if (refreshResponse != null && refreshResponse.getStatus() == 200) {
            String refreshTokenResponse = refreshResponse.getContentAsString();
            TokenResponse tokenResponse = gson.fromJson(refreshTokenResponse.trim(), TokenResponse.class);

            if (tokenResponse != null && tokenResponse.access_token != null && !tokenResponse.access_token.isEmpty()) {
                tokenResponse.created_at = Instant.now().getEpochSecond();
                logger.debug("Access token expires in {} seconds at {}", tokenResponse.expires_in, DATE_FORMATTER
                        .format(Instant.ofEpochMilli((tokenResponse.created_at + tokenResponse.expires_in) * 1000)));
                return tokenResponse;
            } else {
                logger.debug("An error occurred while exchanging SSO auth token for API access token.");
            }
        } else {
            logger.debug("An error occurred during refresh of SSO token: {}",
                    (refreshResponse != null ? refreshResponse.getStatus() : "no response"));
        }

        return null;
    }

    @Nullable
    private ContentResponse executeHttpRequest(org.eclipse.jetty.client.api.Request request) {
        request.timeout(10, TimeUnit.SECONDS);

        ContentResponse response;
        try {
            response = request.send();
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("An exception occurred while invoking a HTTP request: '{}'", e.getMessage());
            return null;
        }
    }
}
