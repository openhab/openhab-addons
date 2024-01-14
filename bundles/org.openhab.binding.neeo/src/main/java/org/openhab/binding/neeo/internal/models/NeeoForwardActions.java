/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing a forward actions request (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoForwardActions {
    /** The host to forward actions to */
    @Nullable
    private final String host;

    /** The port to use */
    private final int port;

    /** The path the actions should go to */
    @Nullable
    private final String path;

    /**
     * Creates the forward actions from the given parms
     *
     * @param host the host name
     * @param port the port
     * @param path the path
     */
    public NeeoForwardActions(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    /**
     * Returns the host name to forward actions to
     *
     * @return the hostname
     */
    @Nullable
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the path to use
     *
     * @return the path
     */
    @Nullable
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "NeeoForwardActions [host=" + host + ", port=" + port + ", path=" + path + "]";
    }
}
