/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service.dhcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.TreeMap;

import org.openhab.binding.network.service.StateUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton UDP Receiver class. StateUpdate objects can register and unregister.
 * If the first one is registered and there is no singleton instance, an instance will be created and the
 * receiver thread will be started. If the last StateUpdate is removed, the thread will be stopped
 * after the receive socket is closed. This instance listens to the UDP port 67 and will call
 * StateUpdate.newState(0) for the address that is registered and matches the DHO_DHCP_REQUESTED_ADDRESS address field.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class ReceiveDHCPRequestPackets extends Thread {
    private byte[] buffer = new byte[1024];
    private DatagramSocket dsocket = new DatagramSocket(null);
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    private boolean willbeclosed = false;
    private Logger logger = LoggerFactory.getLogger(ReceiveDHCPRequestPackets.class);
    private static ReceiveDHCPRequestPackets instance;
    private static Map<String, StateUpdate> registeredListeners = new TreeMap<>();

    public static synchronized void register(String hostAddress, StateUpdate receiveParseSimpleUDP)
            throws SocketException {
        if (instance == null) {
            instance = new ReceiveDHCPRequestPackets();
            instance.start();
        }
        registeredListeners.put(hostAddress, receiveParseSimpleUDP);
    }

    public static void unregister(String hostAddress) {
        synchronized (registeredListeners) {
            registeredListeners.remove(hostAddress);
            if (!registeredListeners.isEmpty()) {
                return;
            }
        }

        if (instance != null && instance.isAlive()) {
            instance.willbeclosed = true;
            if (instance.dsocket != null) {
                instance.dsocket.close();
            }
            try {
                instance.join(1000);
            } catch (InterruptedException e) { }
            instance.interrupt();
            instance.dsocket = null;
        }
        instance = null;
    }

    ReceiveDHCPRequestPackets() throws SocketException {
        dsocket.setReuseAddress(true);
        dsocket.setBroadcast(true);
        dsocket.bind(new InetSocketAddress(67));
    }

    @Override
    public void run() {
        try {
            logger.info("DHCP request packet listener online");
            while (!willbeclosed) {
                packet.setLength(buffer.length);
                dsocket.receive(packet);

                DHCPPacket request;
                try {
                    request = new DHCPPacket(packet);
                } catch (Exception e) {
                    continue;
                }

                if (request.getOp() != DHCPPacket.BOOTREQUEST) {
                    continue; // skipping non BOOTREQUEST message types
                }

                Byte dhcpMessageType = request.getDHCPMessageType();

                if (dhcpMessageType == null || dhcpMessageType != DHCPPacket.DHCPREQUEST) {
                    continue; // skipping non DHCPREQUEST message types
                }

                InetAddress requestedAddress = request.getRequestedIPAddress();
                if (requestedAddress == null) {
                    logger.error("DHCPREQUEST field is missing");
                    continue;
                }
                String requestedAddressStr = requestedAddress.getHostAddress();
                StateUpdate receiver = registeredListeners.get(requestedAddressStr);
                if (receiver != null) {
                    logger.info("DHCP request for registered address: " + requestedAddressStr);
                    receiver.newState(0);
                } else {
                    logger.info("DHCP request for unknown address: " + requestedAddressStr);
                }
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.error(e.getLocalizedMessage());
        }
    }

    public DatagramSocket getSocket() {
        return dsocket;
    }
}
