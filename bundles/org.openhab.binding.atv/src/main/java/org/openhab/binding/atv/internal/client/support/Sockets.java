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
package org.openhab.binding.atv.internal.client.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * TCP socket helpers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Sockets {

    /**
     * How long to wait for a TCP connect. Without a limit a sleeping or unreachable device
     * blocks for the OS default, which is well over a minute.
     */
    public static final int CONNECT_TIMEOUT_MS = 10_000;

    private Sockets() {
    }

    /**
     * Opens a TCP connection with {@link #CONNECT_TIMEOUT_MS} and TCP_NODELAY set.
     *
     * @param host device address
     * @param port device port
     * @return the connected socket
     * @throws IOException if the connection cannot be established in time
     */
    public static Socket connect(String host, int port) throws IOException {
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            return socket;
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }
}
