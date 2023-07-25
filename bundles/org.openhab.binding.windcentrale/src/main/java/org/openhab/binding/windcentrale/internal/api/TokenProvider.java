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
package org.openhab.binding.windcentrale.internal.api;

import static org.eclipse.jetty.http.HttpHeader.ACCEPT;
import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.openhab.binding.windcentrale.internal.dto.WindcentraleGson.GSON;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.windcentrale.internal.dto.AuthenticationResultResponse;
import org.openhab.binding.windcentrale.internal.dto.KeyResponse;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the JWT tokens used with the Windcentrale API by using a {@link AuthenticationHelper}.
 * It also resolves the Windcentrale specific Cognito configuration required by the {@link AuthenticationHelper}.
 *
 * A token is obtained by calling {@link #getIdToken()}.
 * The token is cached and returned in subsequent calls to {@link #getIdToken()} until it expires.
 * When tokens expire they are refreshed using the refresh token when available.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class TokenProvider {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String DEFAULT_USER_POOL_ID = "eu-west-1_U7eYBPrBd";
    private static final String DEFAULT_CLIENT_ID = "715j3r0trk7o8dqg3md57il7q0";
    private static final String DEFAULT_REGION = "eu-west-1";

    private static final String APPLICATION_JSON = "application/json";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);
    private static final String KEY_URL = WindcentraleAPI.URL_PREFIX + "/labels/key?domain=mijn.windcentrale.nl";

    private final HttpClientFactory httpClientFactory;

    private final String username;
    private final String password;

    private @Nullable AuthenticationHelper authenticationHelper;

    private String idToken = "";
    private String refreshToken = "";
    private Instant validityEnd = Instant.MIN;

    public TokenProvider(HttpClientFactory httpClientFactory, String username, String password) {
        this.httpClientFactory = httpClientFactory;
        this.username = username;
        this.password = password;
    }

    private AuthenticationHelper createHelper() {
        String userPoolId = DEFAULT_USER_POOL_ID;
        String clientId = DEFAULT_CLIENT_ID;
        String region = DEFAULT_REGION;

        try {
            logger.debug("Getting JSON from: {}", KEY_URL);
            ContentResponse contentResponse = httpClientFactory.getCommonHttpClient().newRequest(KEY_URL) //
                    .method(GET) //
                    .header(ACCEPT, APPLICATION_JSON) //
                    .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS) //
                    .send();

            String response = contentResponse.getContentAsString();
            if (contentResponse.getStatus() >= 400) {
                logger.debug("Could not get Cognito configuration values, using default values. Error (HTTP {}): {}",
                        contentResponse.getStatus(), contentResponse.getReason());
            } else {
                logger.trace("Response: {}", response);
                KeyResponse keyResponse = Objects.requireNonNullElse(GSON.fromJson(response, KeyResponse.class),
                        new KeyResponse());
                if (!keyResponse.userPoolId.isEmpty() && !keyResponse.clientId.isEmpty()
                        && keyResponse.region.isEmpty()) {
                    userPoolId = keyResponse.userPoolId;
                    clientId = keyResponse.clientId;
                    region = keyResponse.region;
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.debug("Could not get Cognito configuration values, using default values", e);
        }

        logger.debug("Creating new AuthenticationHelper (userPoolId={}, clientId={}, region={})", userPoolId, clientId,
                region);
        return new AuthenticationHelper(httpClientFactory, userPoolId, clientId, region);
    }

    private AuthenticationHelper getOrCreateHelper() {
        AuthenticationHelper helper = authenticationHelper;
        if (helper == null) {
            helper = createHelper();
            this.authenticationHelper = helper;
        }
        return helper;
    }

    public String getIdToken() throws InvalidAccessTokenException {
        boolean valid = Instant.now().plusSeconds(30).isBefore(validityEnd);
        if (valid) {
            logger.debug("Reusing existing valid token");
            return idToken;
        }

        AuthenticationResultResponse result = null;
        AuthenticationHelper helper = getOrCreateHelper();

        if (!refreshToken.isBlank()) {
            try {
                logger.debug("Performing token refresh");
                result = helper.performTokenRefresh(refreshToken);
                logger.debug("Successfully performed token refresh");
            } catch (InvalidAccessTokenException e) {
                logger.debug("Token refresh failed", e);
            }
        }

        if (result == null) {
            // there is no refresh token or the refresh failed
            logger.debug("Performing SRP authentication");
            result = helper.performSrpAuthentication(username, password);
            logger.debug("Successfully performed SRP authentication");

            refreshToken = result.getRefreshToken();
        }

        idToken = result.getIdToken();
        validityEnd = Instant.now().plusSeconds(result.getExpiresIn());
        logger.debug("Token is valid until {}", validityEnd);
        return idToken;
    }
}
