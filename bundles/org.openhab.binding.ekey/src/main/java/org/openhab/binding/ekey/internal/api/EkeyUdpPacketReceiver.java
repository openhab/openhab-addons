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
package org.openhab.binding.ekey.internal.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ekey.internal.handler.EkeyHandler;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class provides the DatagramSocket that listens for eKey packets on the network
 * This will run in a thread and can be interrupted by calling <code>stopListener()<code>
 * Before starting the thread initialization is required (mode, ip, port and deliminator)
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EkeyUdpPacketReceiver {

    private final Logger logger = LoggerFactory.getLogger(EkeyUdpPacketReceiver.class);

    private final int buffersize = 1024;
    private final String ipAddress;
    private final int port;
    private final String readerThreadName;

    private @Nullable DatagramSocket socket;

    private @Nullable EkeyPacketListener packetListener;

    private boolean connected = false;

    public EkeyUdpPacketReceiver(final String ipAddress, final int port, final String readerThreadName) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.readerThreadName = readerThreadName;
    }

    public void openConnection() throws IOException {
        closeConnection();

        EkeyPacketListener listener = this.packetListener;

        socket = new DatagramSocket(port);

        Thread udpListener = new Thread(new UDPListener());
        udpListener.setName(readerThreadName);
        udpListener.setDaemon(true);
        udpListener.start();

        setConnected(true);
        if (listener != null) {
            listener.connectionStatusChanged(ThingStatus.ONLINE, null);
        }
    }

    public void closeConnection() {
        setConnected(false);
        try {
            DatagramSocket localSocket = socket;
            if (localSocket != null) {
                localSocket.close();
                localSocket = null;
            }
        } catch (Exception exception) {
            logger.debug("closeConnection(): Error closing connection - {}", exception.getMessage());
        }
    }

    private class UDPListener implements Runnable {

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            logger.debug("Starting ekey Packet Receiver");

            DatagramSocket localSocket = socket;

            if (localSocket == null) {
                throw new IllegalStateException("Cannot access socket.");
            }

            byte[] lastPacket = null;
            DatagramPacket packet = new DatagramPacket(new byte[buffersize], buffersize);
            packet.setData(new byte[buffersize]);

            while (isConnected()) {
                try {
                    logger.trace("Listen for incoming packet");
                    localSocket.receive(packet);
                    logger.trace("Packet received - {}", packet.getData());
                } catch (IOException exception) {
                    logger.debug("Exception during packet read - {}", exception.getMessage());
                }
                InetAddress sourceIp;
                try {
                    sourceIp = InetAddress.getByName(ipAddress);
                    if (packet.getAddress().equals(sourceIp)) {
                        lastPacket = packet.getData();
                        readMessage(lastPacket);
                    } else {
                        logger.warn("Packet received from unknown source (ip={}) - {}",
                                packet.getAddress().getHostAddress(), packet.getData());
                    }
                } catch (UnknownHostException e) {
                    logger.debug("Exception during address conversion - {}", e.getMessage());
                }
            }
        }
    }

    public void readMessage(byte[] message) {
        EkeyPacketListener listener = this.packetListener;

        if (listener != null && message.length != 0) {
            listener.messageReceived(message);
        }
    }

    public void addEkeyPacketListener(EkeyPacketListener listener) {
        if (this.packetListener == null) {
            this.packetListener = listener;
        }
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void removeEkeyPacketListener(EkeyHandler ekeyHandler) {
        if (this.packetListener != null) {
            this.packetListener = null;
        }
    }
}
