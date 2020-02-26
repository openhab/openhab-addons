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

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.exception.*;
import org.openhab.binding.hive.internal.client.repository.NodeRepository;
import org.openhab.binding.hive.internal.client.repository.SessionRepository;

/**
 * The default implementation of {@link HiveClient}
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
final class DefaultHiveClient implements HiveClient {
    private static final int MAX_REAUTHENTICATION_ATTEMPTS = 1;

    private final SessionAuthenticationManager authenticationManager;

    private final String username;
    private final String password;

    private final SessionRepository sessionRepository;
    private final NodeRepository nodeRepository;

    private @Nullable Session session;

    /**
     *
     *
     * @throws HiveApiAuthenticationException
     *      If we failed to authenticate with the provided username and password.
     *
     * @throws HiveApiUnknownException
     *      If something unexpected happens while communicating with the Hive
     *      API.
     *
     * @throws HiveClientResponseException
     *      If we don't understand the response the Hive API gave us.
     */
    public DefaultHiveClient(
            final SessionAuthenticationManager authenticationManager,
            final String username,
            final String password,
            final SessionRepository sessionRepository,
            final NodeRepository nodeRepository
    ) throws HiveException {
        Objects.requireNonNull(authenticationManager);
        Objects.requireNonNull(sessionRepository);
        Objects.requireNonNull(nodeRepository);

        this.authenticationManager = authenticationManager;

        this.username = username;
        this.password = password;

        this.sessionRepository = sessionRepository;
        this.nodeRepository = nodeRepository;

        authenticate();
    }

    @Override
    public void close() {
        // No cleanup required.
    }

    /**
     * Try to authenticate with the Hive API.
     *
     * @throws HiveApiAuthenticationException
     *      If we failed to authenticate with the stored username and password.
     *
     * @throws HiveApiUnknownException
     *      If something unexpected happens while communicating with the Hive
     *      API.
     */
    private void authenticate() throws HiveException {
        this.authenticationManager.clearSession();

        final Session session = this.sessionRepository.createSession(
                this.username,
                this.password
        );

        this.session = session;
        this.authenticationManager.setSession(session);
    }

    private <T> T makeAuthenticatedApiCall(final ApiCall<T> apiCall) throws HiveException {
        // If we get a valid result return it.
        // If we are not authorised check if session has expired, reauthenticate and try again.
        // Otherwise let other exceptions bubble up.
        int reauthenticationCount = 0;
        while (reauthenticationCount <= MAX_REAUTHENTICATION_ATTEMPTS) {
            try {
                return apiCall.call();
            } catch (final HiveApiNotAuthorisedException ex) {
                if (reauthenticationCount == MAX_REAUTHENTICATION_ATTEMPTS
                        || this.sessionRepository.isValidSession(this.authenticationManager.getSession())) {
                    // We are either not authorised for this resource or
                    // something has gone wrong with the client logic.
                    // Pass on the exception.
                    throw ex;
                } else {
                    // Session seems to no longer be valid.
                    // Reauthenticate and try again.
                    authenticate();
                    reauthenticationCount++;
                }
            }
        }

        throw new IllegalStateException("Authentication failed and somehow I've escaped my trap.");
    }

    @Override
    public UserId getUserId() {
        final @Nullable Session session = this.session;
        if (session == null) {
            throw new IllegalStateException("Session is unexpectedly null.");
        }

        return session.getUserId();
    }

    @Override
    public Set<Node> getAllNodes() throws HiveException {
        return makeAuthenticatedApiCall(this.nodeRepository::getAllNodes);
    }

    @Override
    public String getAllNodesJson() throws HiveException {
        return makeAuthenticatedApiCall(this.nodeRepository::getAllNodesJson);
    }

    @Override
    public @Nullable Node getNode(final NodeId nodeId) throws HiveException {
        Objects.requireNonNull(nodeId);

        // N.B. Type parameter because Checker Framework needs a little help.
        return this.<@Nullable Node>makeAuthenticatedApiCall(() -> this.nodeRepository.getNode(nodeId));
    }

    @Override
    public @Nullable Node updateNode(final Node node) throws HiveException {
        Objects.requireNonNull(node);

        // N.B. Type parameter because Checker Framework needs a little help.
        return this.<@Nullable Node>makeAuthenticatedApiCall(() -> this.nodeRepository.updateNode(node));
    }
    
    @FunctionalInterface
    private interface ApiCall<T> {
        T call() throws HiveException;
    }
}
