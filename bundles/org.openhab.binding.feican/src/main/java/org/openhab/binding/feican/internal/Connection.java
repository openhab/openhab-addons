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
package org.openhab.binding.feican.internal;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Manages the connection to a Feican bulb.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class Connection implements Closeable {

    /**
     * UDP port to send command.
     */
    public static final int FEICAN_SEND_PORT = 5000;
    /**
     * UDP port devices send discover replies back.
     */
    public static final int FEICAN_RECEIVE_PORT = 6000;

    private final InetAddress iNetAddress;
    private final DatagramSocket socket;

    /**
     * Initializes a connection to the given IP address.
     *
     * @param ipAddress IP address of the connection
     * @throws UnknownHostException if ipAddress could not be resolved.
     * @throws SocketException if no Datagram socket connection could be made.
     */
    public Connection(String ipAddress) throws SocketException, UnknownHostException {
        iNetAddress = InetAddress.getByName(ipAddress);
        socket = new DatagramSocket();
    }

    /**
     * Sends the 9 bytes command to the Feican device.
     *
     * @param command the 9 bytes command
     * @throws IOException Connection to the bulb failed
     */
    public void sendCommand(byte[] command) throws IOException {
        DatagramPacket sendPkt = new DatagramPacket(command, command.length, iNetAddress, FEICAN_SEND_PORT);
        socket.send(sendPkt);
    }

    @Override
    public void close() {
        socket.close();
    }
}
