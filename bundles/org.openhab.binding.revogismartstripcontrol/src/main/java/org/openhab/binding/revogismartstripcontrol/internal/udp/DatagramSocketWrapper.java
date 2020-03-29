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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DatagramSocketWrapper} wraps Java's DatagramSocket for better testing
 * UdpSenderService
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class DatagramSocketWrapper {

    private final Logger logger = LoggerFactory.getLogger(DatagramSocketWrapper.class);
    @Nullable
    DatagramSocket datagramSocket;

    public void initSocket() throws SocketException {
        closeSocket();
        datagramSocket = new DatagramSocket();
        datagramSocket.setBroadcast(true);
        datagramSocket.setSoTimeout(3);
    }

    public void closeSocket() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
    }

    public void sendPacket(DatagramPacket datagramPacket) throws IOException {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.send(datagramPacket);
        } else {
            logger.error("Datagram Socket closed or not initialized");
        }
    }

    public void receiveAnswer(DatagramPacket datagramPacket) throws IOException {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.receive(datagramPacket);
        } else {
            logger.error("Datagram Socket closed or not initialized");
        }
    }
}
