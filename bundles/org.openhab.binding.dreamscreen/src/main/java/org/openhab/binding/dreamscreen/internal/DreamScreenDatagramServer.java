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
package org.openhab.binding.dreamscreen.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DreamScreenDatagramServer} implements the datagram exchange with the device.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
class DreamScreenDatagramServer {
    private final static int DREAMSCREEN_PORT = 8888;
    private final static byte[] CRC_TABLE = new byte[] { 0x00, 0x07, 0x0E, 0x09, 0x1C, 0x1B, 0x12, 0x15, 0x38, 0x3F,
            0x36, 0x31, 0x24, 0x23, 0x2A, 0x2D, 0x70, 0x77, 0x7E, 0x79, 0x6C, 0x6B, 0x62, 0x65, 0x48, 0x4F, 0x46, 0x41,
            0x54, 0x53, 0x5A, 0x5D, (byte) 0xE0, (byte) 0xE7, (byte) 0xEE, (byte) 0xE9, (byte) 0xFC, (byte) 0xFB,
            (byte) 0xF2, (byte) 0xF5, (byte) 0xD8, (byte) 0xDF, (byte) 0xD6, (byte) 0xD1, (byte) 0xC4, (byte) 0xC3,
            (byte) 0xCA, (byte) 0xCD, (byte) 0x90, (byte) 0x97, (byte) 0x9E, (byte) 0x99, (byte) 0x8C, (byte) 0x8B,
            (byte) 0x82, (byte) 0x85, (byte) 0xA8, (byte) 0xAF, (byte) 0xA6, (byte) 0xA1, (byte) 0xB4, (byte) 0xB3,
            (byte) 0xBA, (byte) 0xBD, (byte) 0xC7, (byte) 0xC0, (byte) 0xC9, (byte) 0xCE, (byte) 0xDB, (byte) 0xDC,
            (byte) 0xD5, (byte) 0xD2, (byte) 0xFF, (byte) 0xF8, (byte) 0xF1, (byte) 0xF6, (byte) 0xE3, (byte) 0xE4,
            (byte) 0xED, (byte) 0xEA, (byte) 0xB7, (byte) 0xB0, (byte) 0xB9, (byte) 0xBE, (byte) 0xAB, (byte) 0xAC,
            (byte) 0xA5, (byte) 0xA2, (byte) 0x8F, (byte) 0x88, (byte) 0x81, (byte) 0x86, (byte) 0x93, (byte) 0x94,
            (byte) 0x9D, (byte) 0x9A, 0x27, 0x20, 0x29, 0x2E, 0x3B, 0x3C, 0x35, 0x32, 0x1F, 0x18, 0x11, 0x16, 0x03,
            0x04, 0x0D, 0x0A, 0x57, 0x50, 0x59, 0x5E, 0x4B, 0x4C, 0x45, 0x42, 0x6F, 0x68, 0x61, 0x66, 0x73, 0x74, 0x7D,
            0x7A, (byte) 0x89, (byte) 0x8E, (byte) 0x87, (byte) 0x80, (byte) 0x95, (byte) 0x92, (byte) 0x9B,
            (byte) 0x9C, (byte) 0xB1, (byte) 0xB6, (byte) 0xBF, (byte) 0xB8, (byte) 0xAD, (byte) 0xAA, (byte) 0xA3,
            (byte) 0xA4, (byte) 0xF9, (byte) 0xFE, (byte) 0xF7, (byte) 0xF0, (byte) 0xE5, (byte) 0xE2, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xC1, (byte) 0xC6, (byte) 0xCF, (byte) 0xC8, (byte) 0xDD, (byte) 0xDA, (byte) 0xD3,
            (byte) 0xD4, 0x69, 0x6E, 0x67, 0x60, 0x75, 0x72, 0x7B, 0x7C, 0x51, 0x56, 0x5F, 0x58, 0x4D, 0x4A, 0x43, 0x44,
            0x19, 0x1E, 0x17, 0x10, 0x05, 0x02, 0x0B, 0x0C, 0x21, 0x26, 0x2F, 0x28, 0x3D, 0x3A, 0x33, 0x34, 0x4E, 0x49,
            0x40, 0x47, 0x52, 0x55, 0x5C, 0x5B, 0x76, 0x71, 0x78, 0x7F, 0x6A, 0x6D, 0x64, 0x63, 0x3E, 0x39, 0x30, 0x37,
            0x22, 0x25, 0x2C, 0x2B, 0x06, 0x01, 0x08, 0x0F, 0x1A, 0x1D, 0x14, 0x13, (byte) 0xAE, (byte) 0xA9,
            (byte) 0xA0, (byte) 0xA7, (byte) 0xB2, (byte) 0xB5, (byte) 0xBC, (byte) 0xBB, (byte) 0x96, (byte) 0x91,
            (byte) 0x98, (byte) 0x9F, (byte) 0x8A, (byte) 0x8D, (byte) 0x84, (byte) 0x83, (byte) 0xDE, (byte) 0xD9,
            (byte) 0xD0, (byte) 0xD7, (byte) 0xC2, (byte) 0xC5, (byte) 0xCC, (byte) 0xCB, (byte) 0xE6, (byte) 0xE1,
            (byte) 0xE8, (byte) 0xEF, (byte) 0xFA, (byte) 0xFD, (byte) 0xF4, (byte) 0xF3 };

    private final Logger logger = LoggerFactory.getLogger(DreamScreenDatagramServer.class);
    private final Map<@Nullable String, DreamScreenHandler> dreamScreens = new ConcurrentHashMap<>();

    private @Nullable DatagramSocket serverSocket;
    private @Nullable InetAddress hostAddress;
    private @Nullable InetAddress broadcastAddress;
    private @Nullable ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> refreshing;

    void register(DreamScreenHandler handler, ScheduledExecutorService scheduler) throws IOException {
        dreamScreens.put(handler.getName(), handler);
        this.scheduler = scheduler;
        ensureRefreshing();
    }

    void unregister(DreamScreenHandler handler) {
        dreamScreens.remove(handler.getName());
        if (dreamScreens.isEmpty()) {
            stopRefreshing();
        }
    }

    void shutdown() {
        dreamScreens.clear();
        stopRefreshing();
    }

    private synchronized void ensureRefreshing() throws IOException {
        if (this.broadcastAddress == null) {
            this.broadcastAddress = InetAddress.getByName("255.255.255.255");
        }
        if (this.refreshing == null) {
            final DatagramSocket socket = new DatagramSocket(DREAMSCREEN_PORT, this.hostAddress);
            socket.setReuseAddress(true);
            this.serverSocket = socket;

            final Thread server = new Thread(this::runServer, "dreamscreen-tv");
            server.setDaemon(true);
            server.start();

            final ScheduledExecutorService scheduler = this.scheduler;
            if (scheduler != null) {
                this.refreshing = scheduler.scheduleAtFixedRate(this::requestRefresh, 0, 10, TimeUnit.SECONDS);
            } else {
                logger.error("No scheduler found to refresh DreamScreen TV");
            }
        }
    }

    private void requestRefresh() {
        try {
            broadcast(0xFF, 0x30, 0x01, 0x0A, new byte[0]);
        } catch (IOException e) {
            logger.error("Error requesting refresh of state", e);
        }
    }

    private synchronized void stopRefreshing() {
        final DatagramSocket socket = this.serverSocket;
        if (socket != null) {
            socket.close();
        }
        this.serverSocket = null;

        final ScheduledFuture<?> refreshing = this.refreshing;
        if (refreshing != null) {
            refreshing.cancel(true);
        }
        this.refreshing = null;
    }

    @SuppressWarnings("null")
    private void runServer() {
        final byte[] buf = new byte[256];

        while (serverSocket != null && !serverSocket.isClosed()) {
            try {
                final DatagramPacket data = new DatagramPacket(buf, buf.length);
                serverSocket.receive(data);

                final int off = data.getOffset();
                final int len = data.getLength();

                logger.info("DreamScreen message: {} {}-{}:{}", data.getAddress(), off, len, buf);

                if (isValidStateMsg(buf, off, len) && isDreamScreen(buf, off, len)) {
                    logger.info("Received DreamScreen message from {}", data.getAddress());
                    refreshDreamScreen(buf, off, len, data.getAddress());
                }
            } catch (IOException ioe) {
                logger.error("Error receiving DreamScreen data", ioe);
            }
        }
    }

    void setHostAddress(@Nullable String value) {
        try {
            this.hostAddress = InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            logger.error("Cannot set host address to {}", value, e);
        }

        try {
            if (this.refreshing != null) {
                stopRefreshing();
                ensureRefreshing();
            }
        } catch (IOException e) {
            logger.error("Cannot restart DreamScreen refresh on new host address {}", value, e);
        }
    }

    private boolean isValidStateMsg(final byte[] buf, int off, int len) {
        if (isValidMsg(buf, off, len)) {
            final int upperCommand = buf[off + 4];
            final int lowerCommand = buf[off + 5];

            return upperCommand == 0x01 && lowerCommand == 0x0A;
        }
        return false;
    }

    private boolean isValidMsg(final byte[] buf, int off, int len) {
        if (len > 6 && buf[off] == (byte) 0xFC) {
            final int msgLen = buf[off + 1] & 0xFF;
            if (msgLen + 2 > len) {
                return false; // invalid length
            } else if (buf[off + msgLen + 1] != calcCRC8(buf, off)) {
                return false; // invalid crc
            }
            return true;
        }
        return false; // message not long enough
    }

    private boolean isDreamScreen(final byte[] buf, int off, int len) {
        final int msgLen = buf[off + 1] & 0xFF;
        final int productId = buf[off + msgLen];

        return msgLen > 77 && productId > 0 && productId <= 2;
    }

    @SuppressWarnings("null")
    private void refreshDreamScreen(final byte[] buf, int off, int len, InetAddress address) {
        final String name = new String(buf, off + 6, 16, StandardCharsets.UTF_8).trim();
        final DreamScreenHandler dreamScreen = this.dreamScreens.get(name);

        if (dreamScreen != null) {
            dreamScreen.refreshState(buf, off, len, address);
        }
    }

    void send(int group, int commandUpper, int commandLower, byte[] payload, @Nullable InetAddress address)
            throws IOException {
        send(group, 0b00010001, commandUpper, commandLower, payload, address);
    }

    void send(int group, int flags, int commandUpper, int commandLower, byte[] payload, @Nullable InetAddress address)
            throws IOException {
        final DatagramSocket socket = new DatagramSocket();
        try {
            socket.setBroadcast(false);
            socket.send(buildPacket(group, flags, commandUpper, commandLower, payload, address));
        } finally {
            socket.close();
        }
    }

    void broadcast(int group, int commandUpper, int commandLower, byte[] payload) throws IOException {
        broadcast(group, 0b00100001, commandUpper, commandLower, payload);
    }

    void broadcast(int group, int flags, int commandUpper, int commandLower, byte[] payload) throws IOException {
        final DatagramSocket socket = new DatagramSocket(0, hostAddress);
        try {
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            socket.send(buildPacket(group, flags, commandUpper, commandLower, payload, broadcastAddress));
        } finally {
            socket.close();
        }
    }

    private DatagramPacket buildPacket(int group, int flags, int commandUpper, int commandLower, byte[] payload,
            @Nullable InetAddress address) throws IOException {
        final byte[] msg = new byte[payload.length + 7];
        msg[0] = (byte) 0xFC;
        msg[1] = (byte) (0x05 + payload.length);
        msg[2] = (byte) group;
        msg[3] = (byte) flags;
        msg[4] = (byte) commandUpper;
        msg[5] = (byte) commandLower;
        System.arraycopy(payload, 0, msg, 6, payload.length);
        msg[payload.length + 6] = calcCRC8(msg, 0);
        return new DatagramPacket(msg, msg.length, address, DREAMSCREEN_PORT);
    }

    private static final byte calcCRC8(byte[] data, int off) {
        int size = (data[off + 1] & 0xFF) + 1;
        int cntr = 0;
        byte crc = 0x00;
        while (cntr < size) {
            crc = CRC_TABLE[(byte) (crc ^ (data[off + cntr])) & 0xFF];
            cntr++;
        }
        return crc;
    }
}
