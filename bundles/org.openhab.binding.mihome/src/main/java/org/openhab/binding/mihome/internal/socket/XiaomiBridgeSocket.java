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
package org.openhab.binding.mihome.internal.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of the multicast communication with the bridge.
 *
 * @author Dieter Schmidt - Initial contribution
 *
 */
@NonNullByDefault
public class XiaomiBridgeSocket extends XiaomiSocket {

    private final Logger logger = LoggerFactory.getLogger(XiaomiBridgeSocket.class);

    public XiaomiBridgeSocket(int port, String owner) {
        super(port, owner);
    }

    /**
     * Sets up the {@link XiaomiBridgeSocket}.
     *
     * Connects the socket to the specific multicast address and port.
     */
    @Override
    protected synchronized void setupSocket() {
        MulticastSocket socket = (MulticastSocket) getSocket();
        if (socket != null) {
            logger.debug("Socket already setup");
            return;
        }

        try {
            logger.debug("Setup socket");
            socket = new MulticastSocket(getPort());
            setSocket(socket); // must bind receive side
            socket.joinGroup(InetAddress.getByName(MCAST_ADDR));
            logger.debug("Initialized socket to {}:{} on {}:{}", socket.getRemoteSocketAddress(), socket.getPort(),
                    socket.getLocalAddress(), socket.getLocalPort());
        } catch (IOException e) {
            logger.error("Setup socket error", e);
        }
    }
}
