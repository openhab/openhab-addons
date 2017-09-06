/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feican.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Manages the connection to a Feican bulb.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Connection {

    /**
     * UDP port to send command.
     */
    public static final int FEICAN_SEND_PORT = 5000;
    /**
     * UDP port devices send discover replies back.
     */
    public static final int FEICAN_RECEIVE_PORT = 6000;

    private final String ipAddress;

    /**
     * Initializes a connection to the given IP address.
     *
     * @param ipAddress IP address of the connection
     */
    public Connection(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Sends the 9 bytes command to the Feican device.
     *
     * @param command the 9 bytes command
     * @throws IOException Connection to the bulb failed
     */
    public void sendCommand(byte[] command) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress addr = InetAddress.getByName(ipAddress);
            DatagramPacket sendPkt = new DatagramPacket(command, command.length, addr, FEICAN_SEND_PORT);
            socket.send(sendPkt);
        }
    }
}
