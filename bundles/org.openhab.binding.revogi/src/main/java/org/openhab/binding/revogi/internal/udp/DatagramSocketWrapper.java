/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.revogi.internal.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DatagramSocketWrapper} wraps Java's DatagramSocket for better testing
 * UdpSenderService
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class DatagramSocketWrapper {

    @Nullable
    private DatagramSocket datagramSocket;

    public void initSocket() throws SocketException {
        closeSocket();
        DatagramSocket localDatagramSocket = new DatagramSocket();
        localDatagramSocket.setBroadcast(true);
        localDatagramSocket.setSoTimeout(3);
        datagramSocket = localDatagramSocket;
    }

    public void closeSocket() {
        DatagramSocket localDatagramSocket = this.datagramSocket;
        if (localDatagramSocket != null && !localDatagramSocket.isClosed()) {
            localDatagramSocket.close();
        }
    }

    public void sendPacket(DatagramPacket datagramPacket) throws IOException {
        DatagramSocket localDatagramSocket = this.datagramSocket;
        if (localDatagramSocket != null && !localDatagramSocket.isClosed()) {
            localDatagramSocket.send(datagramPacket);
        } else {
            throw new SocketException("Datagram Socket closed or not initialized");
        }
    }

    public void receiveAnswer(DatagramPacket datagramPacket) throws IOException {
        DatagramSocket localDatagramSocket = this.datagramSocket;
        if (localDatagramSocket != null && !localDatagramSocket.isClosed()) {
            localDatagramSocket.receive(datagramPacket);
        } else {
            throw new SocketException("Datagram Socket closed or not initialized");
        }
    }
}
