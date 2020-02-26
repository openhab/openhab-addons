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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hive.internal.client.exception.HiveException;
import org.openhab.binding.hive.internal.client.repository.DefaultNodeRepository;
import org.openhab.binding.hive.internal.client.repository.DefaultSessionRepository;
import org.openhab.binding.hive.internal.client.repository.NodeRepository;
import org.openhab.binding.hive.internal.client.repository.SessionRepository;

/**
 * The default implementation of {@link HiveClientFactory}.
 *
 * Returns instances of {@link DefaultHiveClient}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class DefaultHiveClientFactory implements HiveClientFactory {
    private static final String CLIENT_ID = "openHAB";

    private final JettyHiveApiRequestFactory requestFactory;

    public DefaultHiveClientFactory(final HttpClient httpClient) {
        Objects.requireNonNull(httpClient);

        this.requestFactory = new JettyHiveApiRequestFactory(
                httpClient,
                HiveApiConstants.DEFAULT_BASE_PATH,
                new GsonJsonService(),
                CLIENT_ID
        );
    }

    public HiveClient newClient(
            final String username,
            final String password
    ) throws HiveException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        final SessionRepository sessionRepository = new DefaultSessionRepository(
                this.requestFactory
        );

        final NodeRepository nodeRepository = new DefaultNodeRepository(
                this.requestFactory
        );

        return new DefaultHiveClient(
                this.requestFactory,
                username,
                password,
                sessionRepository,
                nodeRepository
        );
    }
}
