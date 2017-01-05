/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.socket;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of the discovery communication with the MiHome gateway
 *
 * @author Dieter Schmidt
 *
 */
public class XiaomiDiscoverySocket extends XiaomiSocket {

    private static final int MCAST_PORT = 4321;

    private static final Logger logger = LoggerFactory.getLogger(XiaomiDiscoverySocket.class);

    public XiaomiDiscoverySocket() {
        super();
    }

    /**
     * Sets up the {@link XiaomiDiscoverySocket}.
     *
     * Connects the socket to the specific multicast address and port.
     * Starts the {@link ReceiverThread} for the socket.
     */
    @Override
    void setupSocket() {
        synchronized (XiaomiDiscoverySocket.class) {
            try {
                logger.debug("Setup discovery socket");
                socket = new DatagramSocket(port);
                logger.debug("Initialized socket to {}:{} on {}:{}", socket.getInetAddress(), socket.getPort(),
                        socket.getLocalAddress(), socket.getLocalPort());
            } catch (IOException e) {
                logger.error("Setup socket error", e);
            }

            socketReceiveThread = new ReceiverThread();
            socketReceiveThread.start();
            if (socket != null) {
                openSockets.put(port, this);
            }
        }
    }

    /**
     * Sends a message through the {@link XiaomiDiscoverySocket}
     * to the MiHome multicast address 224.0.0.50 and port 4321
     *
     * @param message - Message to be sent
     */
    public void sendMessage(String message) {
        try {
            sendMessage(message, InetAddress.getByName(MCAST_ADDR), MCAST_PORT);
        } catch (UnknownHostException e) {
            logger.error("Sending error", e);
        }
    }

}
