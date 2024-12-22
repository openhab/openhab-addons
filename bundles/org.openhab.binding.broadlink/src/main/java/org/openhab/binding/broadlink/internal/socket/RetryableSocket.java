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
package org.openhab.binding.broadlink.internal.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.config.BroadlinkDeviceConfiguration;
import org.slf4j.Logger;

/**
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public class RetryableSocket {
    private final BroadlinkDeviceConfiguration thingConfig;
    private final Logger logger;

    private @Nullable DatagramSocket socket = null;

    public RetryableSocket(BroadlinkDeviceConfiguration thingConfig, Logger logger) {
        this.thingConfig = thingConfig;
        this.logger = logger;
    }

    /**
     * Send a packet to the device, and expect a response.
     * We'll try again if we fail to get any response.
     */
    public byte @Nullable [] sendAndReceive(byte message[], String purpose) {
        byte[] firstAttempt = sendAndReceiveOneTime(message, purpose);

        if (firstAttempt != null) {
            return firstAttempt;
        } else {
            return sendAndReceiveOneTime(message, purpose);
        }
    }

    private byte @Nullable [] sendAndReceiveOneTime(byte message[], String purpose) {
        // To avoid the possibility of a very fast response not being heard,
        // we set up as much as possible for the response ahead of time:
        byte response[] = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(response, response.length);
        if (sendDatagram(message, purpose)) {
            receivePacket = receiveDatagram(purpose, receivePacket);
            if (receivePacket != null) {
                return receivePacket.getData();
            }
        }

        return null;
    }

    private boolean sendDatagram(byte message[], String purpose) {
        try {
            DatagramSocket socket = this.socket;
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                socket.setSoTimeout(5000);
                this.socket = socket;
            }
            InetAddress host = InetAddress.getByName(thingConfig.getIpAddress());
            int port = thingConfig.getPort();
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, new InetSocketAddress(host, port));
            socket.send(sendPacket);
            return true;
        } catch (IOException e) {
            logger.warn("IO error during UDP command sending {}:{}", purpose, e.getMessage());
            return false;
        }
    }

    private @Nullable DatagramPacket receiveDatagram(String purpose, DatagramPacket receivePacket) {
        DatagramSocket socket = this.socket;
        try {
            if (socket == null) {
                logger.warn("receiveDatagram {} for socket was unexpectedly null", purpose);
            } else {
                socket.receive(receivePacket);
                return receivePacket;
            }
        } catch (SocketTimeoutException ste) {
            logger.debug("No further {} response received for device", purpose);
        } catch (Exception e) {
            logger.warn("While {} got unexpected exception: {}", purpose, e.getMessage());
        }

        return null;
    }

    public void close() {
        DatagramSocket socket = this.socket;
        if (socket != null) {
            socket.close();
            this.socket = null;
        }
    }
}
