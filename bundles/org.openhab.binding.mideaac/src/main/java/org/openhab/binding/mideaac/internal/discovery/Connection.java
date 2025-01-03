/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.discovery;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Connection} Manages the discovery connection to a Midea AC.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class Connection implements Closeable {

    /**
     * UDP port1 to send command.
     */
    public static final int MIDEAAC_SEND_PORT1 = 6445;
    /**
     * UDP port2 to send command.
     */
    public static final int MIDEAAC_SEND_PORT2 = 20086;
    /**
     * UDP port devices send discover replies back.
     */
    public static final int MIDEAAC_RECEIVE_PORT = 6440;

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
     * Sends the 9 bytes command to the Midea AC device.
     *
     * @param command the 9 bytes command
     * @throws IOException Connection to the LED failed
     */
    public void sendCommand(byte[] command) throws IOException {
        {
            DatagramPacket sendPkt = new DatagramPacket(command, command.length, iNetAddress, MIDEAAC_SEND_PORT1);
            socket.send(sendPkt);
        }
        {
            DatagramPacket sendPkt = new DatagramPacket(command, command.length, iNetAddress, MIDEAAC_SEND_PORT2);
            socket.send(sendPkt);
        }
    }

    @Override
    public void close() {
        socket.close();
    }
}
