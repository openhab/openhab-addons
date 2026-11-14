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

/**
 * Tests for {@link TailwindUdpListener}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TailwindUdpListenerTest {
    private static final int WAIT_SECONDS = 20;

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private final TailwindUdpListener listener = new TailwindUdpListener("tailwind-test-udp",
            (sender, message) -> messages.add(message));

    @AfterEach
    public void tearDown() {
        listener.stop();
    }

    private static void send(int port, byte[] data) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), port));
        }
    }

    @Test
    public void messagesAreDeliveredWithoutTrailingNulOrLineEnding() throws Exception {
        int port = listener.start();

        send(port, "{\"result\":\"OK\"}\0".getBytes(StandardCharsets.UTF_8));
        send(port, "{\"door_num\":1}\r\n".getBytes(StandardCharsets.UTF_8));

        assertEquals("{\"result\":\"OK\"}", messages.poll(WAIT_SECONDS, TimeUnit.SECONDS));
        assertEquals("{\"door_num\":1}", messages.poll(WAIT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void stoppedListenerHasNoPortAndCanStartAgain() throws Exception {
        assertEquals(-1, listener.getPort());

        int port = listener.start();
        assertEquals(port, listener.getPort());
        listener.stop();
        assertEquals(-1, listener.getPort());

        int newPort = listener.start();
        assertNotEquals(-1, newPort);
        send(newPort, "again".getBytes(StandardCharsets.UTF_8));
        assertEquals("again", messages.poll(WAIT_SECONDS, TimeUnit.SECONDS));
    }
}
