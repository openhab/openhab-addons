/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.IOException;
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
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tesla.internal.protocol.dto.sso.RefreshTokenRequest;
import org.openhab.binding.tesla.internal.protocol.dto.sso.TokenResponse;
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

    /**
     * Exchanges the refresh token for an access token.
     *
     * @return the access token, or {@code null} if the SSO service rejected the refresh token
     * @throws IOException if the SSO service could not be reached or did not return an access token
     */
    @Nullable
    public TokenResponse getAccessToken(String refreshToken) throws IOException {
        logger.debug("Exchanging SSO refresh token for API access token");

        // get a new access token for the owner API token endpoint
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        String refreshTokenPayload = gson.toJson(refreshRequest);

        final org.eclipse.jetty.client.api.Request request = httpClient.newRequest(URI_SSO + "/" + PATH_TOKEN);
        request.content(new StringContentProvider(refreshTokenPayload));
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.method(HttpMethod.POST);

        ContentResponse refreshResponse = executeHttpRequest(request);
        int status = refreshResponse.getStatus();

        if (status == HttpStatus.OK_200) {
            String refreshTokenResponse = refreshResponse.getContentAsString();
            TokenResponse tokenResponse = gson.fromJson(refreshTokenResponse.trim(), TokenResponse.class);

            if (tokenResponse != null && tokenResponse.accessToken != null && !tokenResponse.accessToken.isEmpty()) {
                tokenResponse.createdAt = Instant.now().getEpochSecond();
                logger.debug("Access token expires in {} seconds at {}", tokenResponse.expiresIn, DATE_FORMATTER
                        .format(Instant.ofEpochMilli((tokenResponse.createdAt + tokenResponse.expiresIn) * 1000)));
                return tokenResponse;
            }
            throw new IOException("The SSO service did not return an access token");
        } else if (status == HttpStatus.BAD_REQUEST_400 || status == HttpStatus.UNAUTHORIZED_401
                || status == HttpStatus.FORBIDDEN_403) {
            logger.debug("The SSO service rejected the refresh token: {}", status);
            return null;
        }
        // e.g. 429 or 5xx, the refresh token itself may still be valid
        throw new IOException("Unexpected response from the SSO service: " + status);
    }

    private ContentResponse executeHttpRequest(org.eclipse.jetty.client.api.Request request) throws IOException {
        request.timeout(10, TimeUnit.SECONDS);

        try {
            return request.send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while requesting an access token", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
