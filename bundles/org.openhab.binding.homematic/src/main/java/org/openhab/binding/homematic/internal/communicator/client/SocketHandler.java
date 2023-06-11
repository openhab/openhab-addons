/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple socket cache class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SocketHandler {
    private final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    private Map<Integer, SocketInfo> socketsPerPort = new HashMap<>();
    private HomematicConfig config;

    public SocketHandler(HomematicConfig config) {
        this.config = config;
    }

    /**
     * Returns a socket for the given port, (re)creates it if required.
     */
    public Socket getSocket(int port) throws IOException {
        SocketInfo socketInfo = socketsPerPort.get(port);
        if (socketInfo == null) {
            logger.trace("Creating new socket for port {}", port);
            Socket socket = new Socket();
            socket.setSoTimeout(config.getTimeout() * 1000);
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(config.getGatewayAddress(), port), socket.getSoTimeout());
            socketInfo = new SocketInfo(socket);
            socketsPerPort.put(port, socketInfo);
        } else {
            boolean isMaxAliveReached = System.currentTimeMillis()
                    - socketInfo.getCreated() > (config.getSocketMaxAlive() * 1000);

            if (isMaxAliveReached) {
                logger.debug("Max alive time reached for socket on port {}", port);
                removeSocket(port);
                return getSocket(port);
            }
            logger.trace("Returning socket for port {}", port);
        }
        return socketInfo.getSocket();
    }

    /**
     * Removes the socket for the given port from the cache.
     */
    public void removeSocket(int port) {
        SocketInfo socketInfo = socketsPerPort.get(port);
        if (socketInfo != null) {
            logger.trace("Closing Socket on port {}", port);
            socketsPerPort.remove(port);
            closeSilent(socketInfo.getSocket());
        }
    }

    /**
     * Removes all cached sockets.
     */
    public void flush() {
        synchronized (SocketHandler.class) {
            Integer[] portsToRemove = socketsPerPort.keySet().toArray(new Integer[0]);
            for (Integer key : portsToRemove) {
                removeSocket(key);
            }
        }
    }

    /**
     * Silently closes the given socket.
     */
    private void closeSilent(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
