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
package org.openhab.binding.revogismartstripcontrol.internal.udp;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * The {@link DatagramSocketWrapper} wraps Java's DatagramSocket for better testing
 * UdpSenderService
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class DatagramSocketWrapper {

    @Nullable DatagramSocket datagramSocket;

    public void initSocket() throws SocketException {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        datagramSocket = new DatagramSocket();
        datagramSocket.setBroadcast(true);
        datagramSocket.setSoTimeout(3);
    }

    public void closeSocket() {
        datagramSocket.close();
    }

    public void sendPacket(DatagramPacket datagramPacket) throws IOException {
        datagramSocket.send(datagramPacket);
    }

    public void receiveAnswer(DatagramPacket datagramPacket) throws IOException {
        datagramSocket.receive(datagramPacket);
    }
}