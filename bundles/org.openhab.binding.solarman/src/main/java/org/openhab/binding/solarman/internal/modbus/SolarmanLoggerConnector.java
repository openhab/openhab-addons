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
package org.openhab.binding.solarman.internal.modbus;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;

/**
 * Registers sockets before connecting, allowing disposal to cancel pending I/O.
 *
 * @author Catalin Sanda - Initial contribution
 * @author Ricardo Costa - Advanced profiles and shared compatibility corrections
 */
@NonNullByDefault
public class SolarmanLoggerConnector implements AutoCloseable {
    private final SolarmanLoggerConfiguration configuration;
    private final Set<SolarmanLoggerConnection> connections = new HashSet<>();
    private boolean closed;

    public SolarmanLoggerConnector(SolarmanLoggerConfiguration configuration) {
        this.configuration = configuration;
    }

    public SolarmanLoggerConnection createConnection() {
        SolarmanLoggerConnection connection = new SolarmanLoggerConnection();
        synchronized (this) {
            connections.removeIf(c -> !c.isOpen());
            if (closed) {
                connection.close();
                return connection;
            }
            connections.add(connection);
        }
        connection.connect(configuration.getHostname(), configuration.getPort());
        return connection;
    }

    @Override
    public synchronized void close() {
        closed = true;
        connections.forEach(SolarmanLoggerConnection::close);
        connections.clear();
    }
}
