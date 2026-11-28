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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tailwind.internal.api.TailwindUdpReceiver.Registration;

/**
 * Tests for {@link TailwindUdpReceiver}.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindUdpReceiverTest {
    private static final int WAIT_SECONDS = 20;

    private final TailwindUdpReceiver receiver = new TailwindUdpReceiver();
    private final BlockingQueue<String> first = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> second = new LinkedBlockingQueue<>();

    @AfterEach
    public void tearDown() {
        receiver.stop();
    }

    private static int freePort() throws Exception {
        try (DatagramSocket probe = new DatagramSocket()) {
            return probe.getLocalPort();
        }
    }

    private static void send(int port, String text) throws Exception {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), port));
        }
    }

    @Test
    public void thingsWithTheSamePortShareTheListener() throws Exception {
        int port = freePort();
        Registration one = receiver.register(port, (sender, text) -> first.add(text));
        Registration two = receiver.register(port, (sender, text) -> second.add(text));
        assertEquals(port, one.getPort());
        assertEquals(port, two.getPort());

        send(port, "both");
        assertEquals("both", first.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertEquals("both", second.poll(WAIT_SECONDS, TimeUnit.SECONDS));

        one.close();
        send(port, "second only");
        assertEquals("second only", second.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertNull(first.poll(200, TimeUnit.MILLISECONDS));

        two.close();
        assertEquals(-1, two.getPort());
    }

    @Test
    public void aFreePortIsUsedWhenThePortIsTaken() throws Exception {
        try (DatagramSocket other = new DatagramSocket()) {
            int taken = other.getLocalPort();

            Registration registration = receiver.register(taken, (sender, text) -> first.add(text));

            int port = registration.getPort();
            assertNotEquals(-1, port);
            assertNotEquals(taken, port);
            send(port, "moved");
            assertEquals("moved", first.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        }
    }
}
