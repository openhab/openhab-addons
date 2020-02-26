/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.Request;

/**
 * An implementation of {@link HiveApiRequestFactory} that uses Jetty's
 * {@link HttpClient}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
final class JettyHiveApiRequestFactory implements HiveApiRequestFactory, SessionAuthenticationManager {
    private static final String ACCESS_TOKEN_HEADER = "X-Omnia-Access-Token";
    private static final String CLIENT_ID_HEADER = "X-Omnia-Client";

    private static final long TIMEOUT_VALUE = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final HttpClient httpClient;
    private final URI apiBasePath;
    private final JsonService jsonService;
    private final String clientId;

    private @Nullable Session session = null;

    /**
     * Create a new {@link JettyHiveApiRequestFactory}.
     *
     * @param httpClient
     *      The {@link HttpClient} that should be used to make requests to the
     *      Hive API. N.B. This should not be reused elsewhere.
     *
     * @param apiBasePath
     *      The base path the Hive API.
     *
     * @param jsonService
     *      The {@link JsonService} to use.
     *
     * @param clientId
     *      The Client ID to identify as to the Hive API.
     */
    public JettyHiveApiRequestFactory(
            final HttpClient httpClient,
            final URI apiBasePath,
            final JsonService jsonService,
            final String clientId
    ) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.apiBasePath = Objects.requireNonNull(apiBasePath);
        this.jsonService = Objects.requireNonNull(jsonService);
        this.clientId = Objects.requireNonNull(clientId);

        if (!apiBasePath.isAbsolute()) {
            throw new IllegalArgumentException("API base path must be absolute");
        }

        // httpClient needs to be started or modifying the protocol handlers
        // will not work correctly.
        if (!httpClient.isStarted()) {
            throw new IllegalArgumentException("The provided HttpClient is not started!");
        }

        // Remove jetty's default authentication handler as it will be confused
        // by the Hive API's lack of "WWW-Authenticate" header and will mess
        // up manual authentication.
        this.httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
    }

    @Override
    public void clearSession() {
        this.session = null;
    }

    @Override
    public @Nullable Session getSession() {
        return this.session;
    }

    @Override
    public void setSession(final Session session) {
        Objects.requireNonNull(session);

        this.session = session;
    }

    @Override
    public HiveApiRequest newRequest(final URI endpointPath) {
        final URI target = this.apiBasePath.resolve(endpointPath);

        final Request request = this.httpClient.newRequest(target);

        // Add X-Omnia-Client header
        request.header(CLIENT_ID_HEADER, this.clientId);

        // If we have a session ID add X-Omnia-Access-Token header.
        final @Nullable Session session = this.session;
        if (session != null) {
            request.header(ACCESS_TOKEN_HEADER, session.getSessionId().toString());
        }

        // Set a short timeout as the Hive API should respond very quickly.
        // If a response is taking a long time something has gone wrong so we
        // should abort and back off until things are back to normal.
        request.timeout(TIMEOUT_VALUE, TIMEOUT_UNIT);

        return new JettyHiveApiRequest(
                this.jsonService,
                request
        );
    }
}
