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
package org.openhab.binding.network.internal.dhcp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.network.internal.dhcp.DHCPPacket.BadPacketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives UDP messages and try to parse them as DHCP messages.
 * First try to listen to the DHCP port 67 and if that failes because of missing access rights,
 * port 6767 will be opened instead (and the user is required to setup a port forarding).
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DHCPPacketListenerServer extends Thread {
    private byte[] buffer = new byte[1024];
    protected @Nullable DatagramSocket dsocket;
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    boolean willbeclosed = false;
    Logger logger = LoggerFactory.getLogger(DHCPPacketListenerServer.class);
    private boolean useUnprevilegedPort = false;
    private final IPRequestReceivedCallback listener;

    DHCPPacketListenerServer(IPRequestReceivedCallback listener) throws SocketException, BindException {
        this.listener = listener;
        try {
            bindSocketTo(67);
        } catch (SocketException e) {
            useUnprevilegedPort = true;
            bindSocketTo(6767);
        }
    }

    protected void bindSocketTo(int port) throws SocketException {
        DatagramSocket dsocket = new DatagramSocket(null);
        dsocket.setReuseAddress(true);
        dsocket.setBroadcast(true);
        dsocket.bind(new InetSocketAddress(port));
        this.dsocket = dsocket;
    }

    protected void receivePacket(DHCPPacket request, @Nullable InetAddress udpRemote)
            throws BadPacketException, UnknownHostException, IOException {
        if (request.getOp() != DHCPPacket.BOOTREQUEST) {
            return; // skipping non BOOTREQUEST message types
        }

        Byte dhcpMessageType = request.getDHCPMessageType();

        if (dhcpMessageType != DHCPPacket.DHCPREQUEST) {
            return; // skipping non DHCPREQUEST message types
        }

        InetAddress requestedAddress = request.getRequestedIPAddress();
        if (requestedAddress == null) {
            // There is no requested address field. This may be a DHCPREQUEST message to renew
            // the lease. Let's deduct the IP by the IP/UDP src.
            requestedAddress = udpRemote;
            if (requestedAddress == null) {
                logger.warn("DHO_DHCP_REQUESTED_ADDRESS field is missing");
                return;
            }
        }
        listener.dhcpRequestReceived(requestedAddress.getHostAddress());
    }

    @Override
    public void run() {
        try {
            logger.info("DHCP request packet listener online");
            while (!willbeclosed) {
                packet.setLength(buffer.length);
                DatagramSocket socket = dsocket;
                if (socket == null) {
                    return;
                }
                socket.receive(packet);
                receivePacket(new DHCPPacket(packet), packet.getAddress());
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.warn("{}", e.getLocalizedMessage());
        }
    }

    public @Nullable DatagramSocket getSocket() {
        return dsocket;
    }

    // Return true if the instance couldn't bind to port 67 and used port 6767 instead
    // to listen to DHCP traffic (port forwarding necessary).
    public boolean isUseUnprevilegedPort() {
        return useUnprevilegedPort;
    }

    /**
     * Closes the socket and waits for the receive thread to finish.
     * Does nothing if the receive thread is not running.
     */
    public void close() {
        if (isAlive()) {
            willbeclosed = true;
            DatagramSocket socket = dsocket;
            if (socket != null) {
                socket.close();
            }
            try {
                join(1000);
            } catch (InterruptedException e) {
            }
            interrupt();
            dsocket = null;
        }
    }
}
