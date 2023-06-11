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
package org.openhab.binding.homematic.internal.communicator.client;

import java.net.Socket;

/**
 * Info class which holds some infos for caching a socket.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class SocketInfo {
    private Socket socket;
    private long created;

    public SocketInfo(Socket socket) {
        this.socket = socket;
        this.created = System.currentTimeMillis();
    }

    /**
     * Returns the socket.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the timestamp when the socket has been created.
     */
    public long getCreated() {
        return created;
    }
}
