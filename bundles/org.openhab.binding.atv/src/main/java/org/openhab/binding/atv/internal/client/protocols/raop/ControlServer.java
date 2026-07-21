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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopPackets.RetransmitRequest;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopPackets.SyncPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RAOP control channel endpoint.
 *
 * <p>
 * Sends sync packets to the receiver and answers retransmit requests ({@code type &
 * 0x7F == 0x55}) from a backlog of recently sent audio packets. The periodic sync
 * <em>scheduling</em> is wired by the stream client; this class only provides the packet
 * plumbing ({@link #sendSync}, retransmit handling and the backlog).
 *
 * <p>
 * Binds a UDP socket on an ephemeral port and serves incoming control data from a
 * dedicated virtual thread until {@link #close()} is called.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class ControlServer implements AutoCloseable {

    /** Notified for every parsed retransmit request (after backlog handling). */
    @FunctionalInterface
    public interface RetransmitListener {
        /** Called when a retransmit request has been received. */
        void onRetransmitRequest(RetransmitRequest request, SocketAddress sender);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlServer.class);

    private static final int RETRANSMIT_REQUEST_TYPE = 0x55;

    private final DatagramSocket socket;
    private final PacketFifo<byte[]> packetBacklog;
    private final Thread receiverThread;
    private volatile @Nullable RetransmitListener listener;

    /**
     * Creates a control server on an ephemeral port bound to the wildcard address.
     */
    public ControlServer(PacketFifo<byte[]> packetBacklog) throws IOException {
        this(packetBacklog, 0, null);
    }

    /**
     * Creates a control server on an ephemeral port bound to the given local address.
     */
    public ControlServer(PacketFifo<byte[]> packetBacklog, InetAddress bindAddress) throws IOException {
        this(packetBacklog, 0, bindAddress);
    }

    /**
     * Creates a control server on the given port (0 = ephemeral) bound to the given local
     * address ({@code null} = wildcard).
     */
    public ControlServer(PacketFifo<byte[]> packetBacklog, int port, @Nullable InetAddress bindAddress)
            throws IOException {
        this.packetBacklog = packetBacklog;
        this.socket = new DatagramSocket(port, bindAddress);
        this.receiverThread = Thread.ofVirtual().name("raop-control-server").start(this::receiveLoop);
    }

    /**
     * Returns the local port this control endpoint listens on.
     */
    public int port() {
        return socket.getLocalPort();
    }

    /**
     * Returns the backlog of recently sent audio packets used for retransmission.
     */
    public PacketFifo<byte[]> packetBacklog() {
        return packetBacklog;
    }

    /**
     * Sets a listener notified of incoming retransmit requests (may be {@code null}).
     */
    public void setRetransmitListener(@Nullable RetransmitListener listener) {
        this.listener = listener;
    }

    /**
     * Sends a sync packet to the receiver's control port.
     *
     * <p>
     * Proto is {@code 0x90} for the first packet of a session and {@code 0x80}
     * afterwards, type {@code 0xD4}, seqno {@code 0x0007}.
     *
     * @param dest receiver control address (host + control port)
     * @param first whether this is the first sync packet of the session
     * @param rtptime current RTP time including latency
     * @param latency latency in frames
     * @param ntpTime current time in NTP format (from {@code ts2ntp(head_ts, rate)})
     */
    public void sendSync(SocketAddress dest, boolean first, long rtptime, long latency, long ntpTime)
            throws IOException {
        long[] parts = RaopTiming.ntp2parts(ntpTime);
        SyncPacket packet = new SyncPacket(first ? 0x90 : 0x80, 0xD4, 0x0007, rtptime - latency, parts[0], parts[1],
                rtptime);
        byte[] encoded = packet.encode();
        LOGGER.debug("Sending sync packet (sec={}, frac={}, rtptime={})", parts[0], parts[1], rtptime);
        socket.send(new DatagramPacket(encoded, encoded.length, dest));
    }

    /**
     * Closes the control server and stops the receive loop.
     */
    @Override
    public void close() {
        socket.close();
        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void receiveLoop() {
        byte[] buffer = new byte[4096];
        while (!socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                handleDatagram(packet);
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    LOGGER.error("Control connection error: {}", e.toString());
                }
                return;
            } catch (RuntimeException e) {
                LOGGER.debug("Failed to handle control data", e);
            }
        }
    }

    private void handleDatagram(DatagramPacket packet) throws IOException {
        byte[] data = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
        if (data.length < 2) {
            LOGGER.debug("Received too short control data from {}", packet.getSocketAddress());
            return;
        }

        int actualType = data[1] & 0x7F; // Remove marker bit
        if (actualType == RETRANSMIT_REQUEST_TYPE) {
            RetransmitRequest request = RetransmitRequest.decode(data, true);
            retransmitLostPackets(request, packet.getSocketAddress());
            RetransmitListener currentListener = listener;
            if (currentListener != null) {
                currentListener.onRetransmitRequest(request, packet.getSocketAddress());
            }
        } else {
            LOGGER.debug("Received unhandled control data from {}", packet.getSocketAddress());
        }
    }

    private void retransmitLostPackets(RetransmitRequest request, SocketAddress addr) throws IOException {
        LOGGER.debug("{} from {}", request, addr);

        for (int i = 0; i < request.lostPackets(); i++) {
            int seqno = request.lostSeqno() + i;
            if (packetBacklog.contains(seqno)) {
                byte[] packet = packetBacklog.get(seqno);

                // Very "low level" here just because it's simple and avoids
                // unnecessary conversions: 0x80 0xD6 + original seqno + packet
                byte[] response = new byte[4 + packet.length];
                response[0] = (byte) 0x80;
                response[1] = (byte) 0xD6;
                response[2] = packet[2];
                response[3] = packet[3];
                System.arraycopy(packet, 0, response, 4, packet.length);

                socket.send(new DatagramPacket(response, response.length, addr));
            } else {
                LOGGER.debug("Packet {} not in backlog", seqno);
            }
        }
    }
}
