/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Threaded socket implementation
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */

@NonNullByDefault
public class BroadlinkSocket {
    private static byte buffer[];
    private static DatagramPacket datagramPacket;
    @Nullable
    private static MulticastSocket socket = null;
    @Nullable
    private static Thread socketReceiveThread;
    private static List<BroadlinkSocketListener> listeners = new ArrayList<BroadlinkSocketListener>();
    private static final Logger LOGGER = LoggerFactory.getLogger(BroadlinkSocket.class);

    static {
        buffer = new byte[1024];
        datagramPacket = new DatagramPacket(buffer, buffer.length);
    }

    public static String decodeMAC(byte mac[]) {
        if (mac.length < 6) {
            throw new IllegalArgumentException("Insufficient MAC bytes provided, cannot decode it");
        }

        StringBuilder sb = new StringBuilder(18);
        for (int i = 5; i >= 0; i--) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", new Object[] { Byte.valueOf(mac[i]) }));
        }

        return sb.toString();
    }

    private static class ReceiverThread extends Thread {

        public void run() {
            receiveData(BroadlinkSocket.socket, BroadlinkSocket.datagramPacket);
        }

        private void receiveData(@Nullable MulticastSocket socket, DatagramPacket dgram) {
            final MulticastSocket localSocket = socket;

            try {
                while (true) {
                    if (localSocket != null) {
                        localSocket.receive(dgram);
                        BroadlinkSocketListener listener;
                        byte remoteMAC[];
                        org.openhab.core.thing.ThingTypeUID deviceType;
                        int model;
                        for (Iterator<BroadlinkSocketListener> iterator = (new ArrayList<BroadlinkSocketListener>(
                                BroadlinkSocket.listeners)).iterator(); iterator.hasNext(); listener.onDataReceived(
                                        dgram.getAddress().getHostAddress(), dgram.getPort(), decodeMAC(remoteMAC),
                                        deviceType, model)) {
                            listener = iterator.next();
                            byte receivedPacket[] = dgram.getData();
                            remoteMAC = Arrays.copyOfRange(receivedPacket, 58, 64);
                            model = Byte.toUnsignedInt(receivedPacket[52])
                                    | Byte.toUnsignedInt(receivedPacket[53]) << 8;
                            deviceType = ModelMapper.getThingType(model);
                        }
                    } else {
                        LOGGER.error("MulticastSocket is null");
                    }
                }
            } catch (IOException e) {
                if (!isInterrupted())
                    LOGGER.error("Error while receiving", e);
            }
            BroadlinkSocket.LOGGER.info("Receiver thread ended");
        }

        private ReceiverThread() {
        }
    }

    public static void registerListener(BroadlinkSocketListener listener) {
        listeners.add(listener);
        if (socket == null) {
            setupSocket();
        }
    }

    public static void unregisterListener(BroadlinkSocketListener listener) {
        final MulticastSocket localSocket = socket;

        listeners.remove(listener);
        if (listeners.isEmpty() && localSocket != null) {
            closeSocket();
        }
    }

    private static void setupSocket() {
        synchronized (BroadlinkSocket.class) {
            Thread localSocketReceiveThread = socketReceiveThread;

            try {
                socket = new MulticastSocket();
            } catch (IOException e) {
                LOGGER.error("Setup socket error '{}'.", e.getMessage());
            }
            localSocketReceiveThread = new ReceiverThread();
            localSocketReceiveThread.start();
            socketReceiveThread = localSocketReceiveThread;
        }
    }

    private static void closeSocket() {
        synchronized (BroadlinkSocket.class) {
            final MulticastSocket localSocket = socket;
            final Thread localSocketReceiveThread = socketReceiveThread;

            if (localSocketReceiveThread != null) {
                localSocketReceiveThread.interrupt();
            }
            if (localSocket != null) {
                LOGGER.info("Socket closed");
                localSocket.close();
                socket = null;
            }
        }
    }

    public static void sendMessage(byte message[]) {
        sendMessage(message, "255.255.255.255", 80);
    }

    public static void sendMessage(byte message[], String host, int port) {
        final MulticastSocket localSocket = socket;

        try {
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getByName(host), port);
            if (localSocket != null) {
                localSocket.send(sendPacket);
            } else {
                LOGGER.error("MulticastSocket is null");
            }
        } catch (IOException e) {
            LOGGER.error("IO Error sending message: '{}'", e.getMessage());
        }
    }
}
