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

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;

/**
 * The {@link FroniusDigestSession} holds the authentication parameters of a successful login to a Fronius inverter's
 * <code>/config</code> HTTP endpoints. Its nonce can be reused to authenticate subsequent requests until it expires on
 * the inverter.
 * <p>
 * Instances are not thread-safe: creating an authentication header increments the nonce count, and the inverter
 * expects the nonce counts to arrive in ascending order. Callers therefore have to create the header and send the
 * request while holding a lock, which is what {@link FroniusConfigApiClient} does.
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
@NonNullByDefault
class FroniusDigestSession {
    private final FroniusConfigApiEndpoint endpoint;
    private final String nonce;
    private final String realm;
    private final String qop;
    private final String cnonce;
    private final Instant createdAt;
    private int nc;

    FroniusDigestSession(FroniusConfigApiEndpoint endpoint, String nonce, String realm, String qop, String cnonce,
            Instant createdAt, int nc) {
        this.endpoint = endpoint;
        this.nonce = nonce;
        this.realm = realm;
        this.qop = qop;
        this.cnonce = cnonce;
        this.createdAt = createdAt;
        this.nc = nc;
    }

    /**
     * Checks whether this session can be reused for a request to the given endpoint.
     *
     * @param endpoint the endpoint the request is sent to
     * @param ttl how long a session may be reused after it has been established
     * @return true when this session was established for the given endpoint and has not exceeded the given TTL
     */
    boolean isReusableFor(FroniusConfigApiEndpoint endpoint, Duration ttl) {
        return this.endpoint.equals(endpoint) && Duration.between(createdAt, Instant.now()).compareTo(ttl) < 0;
    }

    /**
     * Creates the authentication header for the next request, incrementing the nonce count of this session.
     *
     * @param method the {@link HttpMethod} to be used by the request
     * @param relativeUrl the relative URL to be accessed with the request
     * @return the authentication header for the request
     * @throws FroniusCommunicationException when the authentication header could not be created
     */
    String createAuthHeader(HttpMethod method, String relativeUrl) throws FroniusCommunicationException {
        nc++;
        try {
            return FroniusConfigAuthUtil.createDigestHeader(endpoint, relativeUrl, method, nonce, realm, qop, nc,
                    cnonce);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            throw new FroniusCommunicationException("Failed to create digest authentication header for request", e);
        }
    }
}
