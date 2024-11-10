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
package org.openhab.binding.smaenergymeter.internal.packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smaenergymeter.internal.handler.EnergyMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PacketListener} class is responsible for communication with the SMA devices.
 * It handles udp/multicast traffic and broadcast received data to subsequent payload handlers.
 *
 * @author Łukasz Dywicki - Initial contribution
 */

@NonNullByDefault
public class PacketListener {

    private final DefaultPacketListenerRegistry registry;
    private final List<PayloadHandler> handlers = new CopyOnWriteArrayList<>();

    private String multicastGroup;
    private int port;

    public static final String DEFAULT_MCAST_GRP = "239.12.255.254";
    public static final int DEFAULT_MCAST_PORT = 9522;

    private @Nullable MulticastSocket socket;
    private @Nullable ScheduledFuture<?> future;

    public PacketListener(DefaultPacketListenerRegistry registry, String multicastGroup, int port) {
        this.registry = registry;
        this.multicastGroup = multicastGroup;
        this.port = port;
    }

    public void addPayloadHandler(PayloadHandler handler) throws IOException {
        if (handlers.isEmpty()) {
            open();
        }
        handlers.add(handler);
    }

    public void removePayloadHandler(PayloadHandler handler) {
        handlers.remove(handler);

        if (handlers.isEmpty()) {
            registry.close(multicastGroup, port);
        }
    }

    public boolean isOpen() {
        MulticastSocket socket = this.socket;
        return socket != null && socket.isConnected();
    }

    private void open() throws IOException {
        if (isOpen()) {
            // no need to bind socket second time
            return;
        }
        MulticastSocket socket = new MulticastSocket(port);
        socket.setSoTimeout(5000);
        InetAddress mcastGroupAddress = InetAddress.getByName(multicastGroup);
        InetSocketAddress socketAddress = new InetSocketAddress(mcastGroupAddress, port);
        socket.joinGroup(socketAddress, null);

        future = registry.addTask(new ReceivingTask(socket, multicastGroup + ":" + port, handlers));
        this.socket = socket;
    }

    void close() throws IOException {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
            this.future = null;
        }

        InetAddress mcastGroupAddress = InetAddress.getByName(multicastGroup);
        InetSocketAddress socketAddress = new InetSocketAddress(mcastGroupAddress, port);
        MulticastSocket socket = this.socket;
        if (socket != null) {
            socket.leaveGroup(socketAddress, null);
            socket.close();
            this.socket = null;
        }
    }

    public void request() {
        MulticastSocket socket = this.socket;
        if (socket != null) {
            registry.execute(new ReceivingTask(socket, multicastGroup + ":" + port, handlers));
        }
    }

    static class ReceivingTask implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(ReceivingTask.class);
        private final DatagramSocket socket;
        private final String group;
        private final List<PayloadHandler> handlers;

        ReceivingTask(DatagramSocket socket, String group, List<PayloadHandler> handlers) {
            this.socket = socket;
            this.group = group;
            this.handlers = handlers;
        }

        public void run() {
            byte[] bytes = new byte[608];
            DatagramPacket msgPacket = new DatagramPacket(bytes, bytes.length);
            DatagramSocket socket = this.socket;

            try {
                do {
                    // this loop is intended to receive all packets queued on the socket,
                    // having a receive() call without loop causes packets to get queued over time,
                    // if more than one meter present because we consume one packet per second
                    socket.receive(msgPacket);
                    EnergyMeter meter = new EnergyMeter();
                    meter.parse(bytes);

                    for (PayloadHandler handler : handlers) {
                        handler.handle(meter);
                    }
                } while (msgPacket.getLength() == 608);
            } catch (IOException e) {
                logger.debug("Unexpected payload received for group {}", group, e);
            }
        }
    }
}
