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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
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

    private final Logger logger = LoggerFactory.getLogger(FroniusConfigApiClient.class);
    private final FroniusHttpUtil httpUtil;
    private final HttpClient httpClient;

    private @Nullable FroniusDigestSession session;

    public FroniusConfigApiClient(FroniusHttpUtil httpUtil, HttpClient httpClient) {
        this.httpUtil = httpUtil;
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
        Properties headers = new Properties();
        headers.put(HttpHeader.AUTHORIZATION.asString(), session.createAuthHeader(method, uri.getPath()));
        ByteArrayInputStream content = body == null ? null
                : new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        String contentType = body == null ? null : "application/json";
        return httpUtil.executeUrl(method, uri.toString(), headers, content, contentType, API_TIMEOUT);
    }
}
