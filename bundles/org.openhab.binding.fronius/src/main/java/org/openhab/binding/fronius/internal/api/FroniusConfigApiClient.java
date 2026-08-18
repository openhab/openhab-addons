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
package org.openhab.binding.fronius.internal.api;

import static org.openhab.binding.fronius.internal.FroniusBindingConstants.API_TIMEOUT;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FroniusConfigApiClient} performs authenticated requests against the <code>/config</code> HTTP endpoints of
 * a Fronius inverter, using {@link FroniusConfigAuthUtil} to log in.
 * <br>
 * The digest session established by a login is kept and reused for subsequent requests, which avoids the costly login
 * handshake for every single request. Because the inverter expects the nonce counts of a session to arrive in
 * ascending order, the authentication header is created and the request is sent while holding the client's lock.
 * <br>
 * An instance of this class is held by the {@link org.openhab.binding.fronius.internal.handler.FroniusBridgeHandler},
 * so that all requests to an inverter share the same session.
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
@NonNullByDefault
public class FroniusConfigApiClient {
    /**
     * How long a digest session (nonce) is reused before performing a fresh login.
     * Kept short to avoid running into server-side nonce expiry, while still avoiding repeated logins for bursts of
     * requests, e.g. a read-modify-write sequence triggered by a channel command.
     */
    private static final Duration SESSION_TTL = Duration.ofMinutes(1);

    /**
     * How often a request is attempted before giving up. The inverter's nginx proxy answers with gateway errors when
     * requests arrive in quick succession, e.g. for a read-modify-write sequence right after another request.
     */
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration GATEWAY_ERROR_RETRY_DELAY = Duration.ofMillis(500);

    private final Logger logger = LoggerFactory.getLogger(FroniusConfigApiClient.class);
    private final HttpClient httpClient;

    private @Nullable FroniusDigestSession session;

    public FroniusConfigApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Performs an authenticated request against the config API.
     *
     * @param endpoint the endpoint to send the request to
     * @param method the HTTP method to use
     * @param uri the URI to request
     * @param body the JSON request body for POST requests, or null for GET requests
     * @return the response body
     * @throws FroniusCommunicationException when an error occurs during communication with the inverter
     * @throws FroniusUnauthorizedException when the login fails due to invalid credentials
     */
    public synchronized String executeRequest(FroniusConfigApiEndpoint endpoint, HttpMethod method, URI uri,
            @Nullable String body) throws FroniusCommunicationException, FroniusUnauthorizedException {
        FroniusDigestSession establishedSession = session;
        if (establishedSession != null && establishedSession.isReusableFor(endpoint, SESSION_TTL)) {
            try {
                return send(establishedSession, method, uri, body);
            } catch (FroniusCommunicationException e) {
                // The session may have expired on the inverter, so fall through to a fresh login and retry once
                logger.debug("Request to {} using the established digest session failed, retrying with a fresh login.",
                        uri, e);
            }
        }
        return send(login(endpoint), method, uri, body);
    }

    private FroniusDigestSession login(FroniusConfigApiEndpoint endpoint)
            throws FroniusCommunicationException, FroniusUnauthorizedException {
        session = null;
        FroniusDigestSession newSession = FroniusConfigAuthUtil.login(httpClient, endpoint, API_TIMEOUT);
        session = newSession;
        return newSession;
    }

    private String send(FroniusDigestSession session, HttpMethod method, URI uri, @Nullable String body)
            throws FroniusCommunicationException {
        for (int attempt = 1; true; attempt++) {
            // The authentication header must be created freshly for every attempt, as the inverter expects the nonce
            // counts of a session to arrive in ascending order
            Request request = httpClient.newRequest(uri).method(method)
                    .header(HttpHeader.AUTHORIZATION, session.createAuthHeader(method, uri.getPath()))
                    .timeout(API_TIMEOUT, TimeUnit.MILLISECONDS);
            if (body != null) {
                request.content(new StringContentProvider("application/json", body, StandardCharsets.UTF_8));
            }
            ContentResponse response;
            try {
                response = request.send();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FroniusCommunicationException("Request interrupted", e);
            } catch (TimeoutException | ExecutionException e) {
                throw new FroniusCommunicationException("Failed to send request", e);
            }
            int status = response.getStatus();
            if (HttpStatus.isSuccess(status)) {
                return response.getContentAsString();
            }
            if (!isGatewayError(status) || attempt >= MAX_ATTEMPTS) {
                throw new FroniusCommunicationException("Request failed with HTTP status " + status);
            }
            logger.debug("Request to {} failed with HTTP {}, retrying (attempt {}/{})", uri, status, attempt,
                    MAX_ATTEMPTS);
            try {
                Thread.sleep(GATEWAY_ERROR_RETRY_DELAY.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FroniusCommunicationException("Request interrupted", e);
            }
        }
    }

    /**
     * @return whether the status code is a gateway error, which the inverter's nginx proxy answers with when the
     *         backend does not keep up with quickly successive requests. Worth a retry after a short delay.
     */
    private static boolean isGatewayError(int status) {
        return status == HttpStatus.BAD_GATEWAY_502 || status == HttpStatus.SERVICE_UNAVAILABLE_503
                || status == HttpStatus.GATEWAY_TIMEOUT_504;
    }
}
