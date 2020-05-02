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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of the multicast communication with the bridge.
 *
 * @author Dieter Schmidt - Initial contribution
 *
 */
public class XiaomiBridgeSocket extends XiaomiSocket {

    private final Logger logger = LoggerFactory.getLogger(XiaomiBridgeSocket.class);

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
    synchronized DatagramSocket setupSocket() {
        DatagramSocket openSocket = getOpenSockets().get(getPort());
        if (openSocket != null) {
            return openSocket;
        }
        try {
            logger.debug("Setup socket");
            setSocket(new MulticastSocket(getPort())); // must bind receive side
            ((MulticastSocket) getSocket()).joinGroup(InetAddress.getByName(MCAST_ADDR));
            logger.debug("Initialized socket to {}:{} on {}:{}", getSocket().getRemoteSocketAddress(),
                    getSocket().getPort(), getSocket().getLocalAddress(), getSocket().getLocalPort());
        } catch (IOException e) {
            logger.error("Setup socket error", e);
        }
        return getSocket();
    }
}
