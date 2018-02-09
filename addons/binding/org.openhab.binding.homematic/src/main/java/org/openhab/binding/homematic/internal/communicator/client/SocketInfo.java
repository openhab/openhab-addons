/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
