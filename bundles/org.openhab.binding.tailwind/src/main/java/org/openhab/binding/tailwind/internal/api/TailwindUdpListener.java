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
    private static final long STOP_TIMEOUT_MS = 2000;

    private final Logger logger = LoggerFactory.getLogger(TailwindUdpListener.class);
    private final String threadName;
    private final BiConsumer<InetAddress, String> consumer;
    private @Nullable DatagramSocket socket;
    private @Nullable Thread thread;

    public TailwindUdpListener(String threadName, BiConsumer<InetAddress, String> consumer) {
        this.threadName = threadName;
        this.consumer = consumer;
    }

    public int start() throws SocketException {
        return start(0);
    }

    // Port 0 picks a free port; returns the port in use
    public int start(int port) throws SocketException {
        stop();
        synchronized (this) {
            DatagramSocket newSocket = port > 0 ? new DatagramSocket(port) : new DatagramSocket();
            socket = newSocket;
            Thread newThread = new Thread(() -> receive(newSocket), threadName);
            newThread.setDaemon(true);
            thread = newThread;
            newThread.start();
            return newSocket.getLocalPort();
        }
    }

    // Closing the socket ends the blocking receive. Waiting for the thread means no message is delivered after
    // stop() returns.
    public void stop() {
        Thread oldThread;
        synchronized (this) {
            DatagramSocket oldSocket = socket;
            socket = null;
            oldThread = thread;
            thread = null;
            if (oldSocket != null) {
                oldSocket.close();
            }
        }
        if (oldThread != null && !oldThread.equals(Thread.currentThread())) {
            try {
                oldThread.join(STOP_TIMEOUT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // -1 if the listener is not running
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
            logger.trace("UDP message from {}: {}", packet.getAddress().getHostAddress(), message);
            try {
                consumer.accept(packet.getAddress(), message);
            } catch (RuntimeException e) {
                logger.warn("Handling a status report failed", e);
            }
        }
    }
}
