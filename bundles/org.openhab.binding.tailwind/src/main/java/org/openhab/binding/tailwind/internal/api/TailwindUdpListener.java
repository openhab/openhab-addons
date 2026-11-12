/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives the status reports that a TailWind controller sends as UDP messages.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - Uses a free port, simplified
 */
@NonNullByDefault
public class TailwindUdpListener {
    private static final int MAX_PACKET_SIZE = 2048;

    private final Logger logger = LoggerFactory.getLogger(TailwindUdpListener.class);
    private final String threadName;
    private final BiConsumer<InetAddress, String> consumer;
    private @Nullable DatagramSocket socket;

    /**
     * @param threadName name of the thread that receives the messages
     * @param consumer called on that thread with the sender and the text of each message
     */
    public TailwindUdpListener(String threadName, BiConsumer<InetAddress, String> consumer) {
        this.threadName = threadName;
        this.consumer = consumer;
    }

    /**
     * Starts listening on a free port.
     *
     * @return the port
     */
    public synchronized int start() throws SocketException {
        stop();
        DatagramSocket newSocket = new DatagramSocket();
        socket = newSocket;
        // A thread of its own, because receiving blocks for as long as the listener runs
        Thread thread = new Thread(() -> receive(newSocket), threadName);
        thread.setDaemon(true);
        thread.start();
        return newSocket.getLocalPort();
    }

    public synchronized void stop() {
        DatagramSocket oldSocket = socket;
        socket = null;
        if (oldSocket != null) {
            // Closing the socket ends the blocking receive, and with it the thread
            oldSocket.close();
        }
    }

    /**
     * Returns the port the listener receives on, or -1 if it is not running.
     */
    public synchronized int getPort() {
        DatagramSocket currentSocket = socket;
        return currentSocket == null || currentSocket.isClosed() ? -1 : currentSocket.getLocalPort();
    }

    private void receive(DatagramSocket receivingSocket) {
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        while (!receivingSocket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                receivingSocket.receive(packet);
            } catch (IOException e) {
                if (!receivingSocket.isClosed()) {
                    logger.debug("Receiving status reports failed, closing the socket: {}", e.getMessage());
                    receivingSocket.close();
                }
                return;
            }
            // trim() also removes a trailing NUL or line ending
            String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
            if (message.isEmpty()) {
                continue;
            }
            try {
                consumer.accept(packet.getAddress(), message);
            } catch (RuntimeException e) {
                logger.warn("Handling a status report failed", e);
            }
        }
    }
}
