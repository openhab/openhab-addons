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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private @Nullable final String netIf;

    public XiaomiBridgeSocket(int port, String netIf, String owner) {
        super(port, owner);
        this.netIf = netIf;
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

            if (netIf != null) {
                socket.setNetworkInterface(NetworkInterface.getByName(netIf));
            }

            setSocket(socket); // must bind receive side
            socket.joinGroup(InetAddress.getByName(MCAST_ADDR));
            logger.debug("Initialized socket to {}:{} on {}:{} bound to {} network interface",
                    socket.getRemoteSocketAddress(), socket.getPort(), socket.getLocalAddress(), socket.getLocalPort(),
                    socket.getNetworkInterface());
        } catch (IOException e) {
            logger.error("Setup socket error", e);
        }
    }
}
