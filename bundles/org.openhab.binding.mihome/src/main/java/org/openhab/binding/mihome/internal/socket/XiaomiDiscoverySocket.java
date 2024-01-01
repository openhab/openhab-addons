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
package org.openhab.binding.mihome.internal.socket;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of the discovery communication with the MiHome gateway
 *
 * @author Dieter Schmidt - Initial contribution
 *
 */
@NonNullByDefault
public class XiaomiDiscoverySocket extends XiaomiSocket {

    private static final int MCAST_PORT = 4321;

    private final Logger logger = LoggerFactory.getLogger(XiaomiDiscoverySocket.class);

    public XiaomiDiscoverySocket(String owner) {
        super(owner);
    }

    /**
     * Sets up the {@link XiaomiDiscoverySocket}.
     *
     * Connects the socket to the specific multicast address and port.
     */
    @Override
    protected void setupSocket() {
        synchronized (XiaomiDiscoverySocket.class) {
            try {
                logger.debug("Setup discovery socket");
                DatagramSocket socket = new DatagramSocket(0);
                setSocket(socket);
                logger.debug("Initialized socket to {}:{} on {}:{}", socket.getInetAddress(), socket.getPort(),
                        socket.getLocalAddress(), socket.getLocalPort());
            } catch (IOException e) {
                logger.error("Setup socket error", e);
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
