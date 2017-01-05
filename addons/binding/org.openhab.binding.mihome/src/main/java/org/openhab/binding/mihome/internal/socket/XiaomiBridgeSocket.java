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
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of the multicast communication with the bridge.
 *
 * @author Dieter Schmidt
 *
 */
public class XiaomiBridgeSocket extends XiaomiSocket {

    private static final Logger logger = LoggerFactory.getLogger(XiaomiBridgeSocket.class);

    public XiaomiBridgeSocket(int port) {
        super(port);
    }

    /**
     * Sets up the {@link XiaomiBridgeSocket}.
     *
     * Connects the socket to the specific multicast address and port.
     * Starts the {@link ReceiverThread} for the socket.
     */
    @Override
    synchronized void setupSocket() {
        try {
            logger.debug("Setup socket");
            socket = new MulticastSocket(port); // must bind receive side
            ((MulticastSocket) socket).joinGroup(InetAddress.getByName(MCAST_ADDR));
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
