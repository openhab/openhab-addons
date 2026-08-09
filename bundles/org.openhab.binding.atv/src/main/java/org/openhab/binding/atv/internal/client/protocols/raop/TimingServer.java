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
import java.time.Clock;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopPackets.TimingPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic timing server responding to timing requests.
 *
 * <p>
 * The receiver periodically asks the sender for its current time to keep clocks in
 * sync; this server echoes the request's send time as reference time and stamps
 * receive/send times with the current NTP time, using response type {@code 0x53 | 0x80}.
 *
 * <p>
 * Binds a UDP socket on an ephemeral port and serves requests from a dedicated
 * virtual thread until {@link #close()} is called.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class TimingServer implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimingServer.class);

    private static final int RESPONSE_TYPE = 0x53 | 0x80;

    private final Clock clock;
    private final DatagramSocket socket;
    private final Thread receiverThread;

    /** Creates a timing server on an ephemeral port bound to the wildcard address. */
    public TimingServer(Clock clock) throws IOException {
        this(clock, 0, null);
    }

    /** Creates a timing server on an ephemeral port bound to the given local address. */
    public TimingServer(Clock clock, InetAddress bindAddress) throws IOException {
        this(clock, 0, bindAddress);
    }

    /**
     * Creates a timing server on the given port (0 = ephemeral) bound to the given local
     * address ({@code null} = wildcard).
     */
    public TimingServer(Clock clock, int port, @Nullable InetAddress bindAddress) throws IOException {
        this.clock = clock;
        this.socket = new DatagramSocket(port, bindAddress);
        this.receiverThread = Thread.ofVirtual().name("raop-timing-server").start(this::receiveLoop);
    }

    /** Returns the local port this server listens on. */
    public int port() {
        return socket.getLocalPort();
    }

    /** Closes the timing server and stops the receive loop. */
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
        byte[] buffer = new byte[64];
        while (!socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                handleRequest(packet);
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    LOGGER.error("Error received: {}", e.toString());
                }
                return;
            } catch (RuntimeException e) {
                LOGGER.debug("Failed to handle timing request", e);
            }
        }
    }

    private void handleRequest(DatagramPacket packet) throws IOException {
        byte[] data = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
        TimingPacket request = TimingPacket.decode(data, true);

        long[] recvtime = RaopTiming.ntp2parts(RaopTiming.ntpNow(clock));
        TimingPacket response = new TimingPacket(request.proto(), RESPONSE_TYPE, 7, 0, request.sendtimeSec(),
                request.sendtimeFrac(), recvtime[0], recvtime[1], recvtime[0], recvtime[1]);

        byte[] encoded = response.encode();
        socket.send(new DatagramPacket(encoded, encoded.length, packet.getSocketAddress()));
    }
}
