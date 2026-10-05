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
package org.openhab.binding.keba.internal.handler.udp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for UDP request lifecycle handling.
 *
 * @author Michael Weger - Initial contribution
 */
class KeContactTransceiverTest {

    @Test
    @Timeout(20)
    void sendDeliversReportAndReceivesReplyWhileCallerWaits() throws Exception {
        assertRoundTrip(false, false);
    }

    @Test
    @Timeout(20)
    void sendReceivesReplyOnSharedListenerPort() throws Exception {
        assertRoundTrip(true, false);
    }

    @Test
    @Timeout(40)
    void registeredHandlerCanSendAfterTransceiverRestart() throws Exception {
        assertRoundTrip(false, true);
        assertRoundTrip(true, true);
    }

    private static void assertRoundTrip(boolean useListenerPort, boolean restart) throws Exception {
        try (DatagramSocket wallbox = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0))) {
            wallbox.setSoTimeout(10000);
            KeContactTransceiver transceiver = new KeContactTransceiver(0, wallbox.getLocalPort());
            KeContactHandler handler = Objects.requireNonNull(createHandler("127.0.0.1"));
            AtomicReference<ByteBuffer> response = new AtomicReference<>();
            Thread sender = new Thread(() -> response.set(transceiver.send("report 1", handler)));
            sender.setDaemon(true);
            try {
                transceiver.registerHandler(handler);
                if (restart) {
                    transceiver.stop();
                    transceiver.start();
                }
                sender.start();
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                wallbox.receive(request);
                assertEquals("report 1", new String(request.getData(), request.getOffset(), request.getLength(),
                        StandardCharsets.US_ASCII));
                transceiver.registerHandler(handler);
                byte[] reply = "{\"ID\":1,\"Product\":\"P30\"}".getBytes(StandardCharsets.US_ASCII);
                wallbox.send(new DatagramPacket(reply, reply.length,
                        useListenerPort ? new InetSocketAddress("127.0.0.1", transceiver.getLocalPort())
                                : request.getSocketAddress()));
                sender.join(TimeUnit.SECONDS.toMillis(5));
                assertFalse(sender.isAlive());
                ByteBuffer received = response.get();
                assertNotNull(received);
                assertEquals(new String(reply, StandardCharsets.US_ASCII),
                        new String(received.array(), 0, received.limit(), StandardCharsets.US_ASCII));
            } finally {
                transceiver.unRegisterHandler(handler);
                sender.interrupt();
                sender.join(TimeUnit.SECONDS.toMillis(5));
                transceiver.stop();
            }
        }
    }

    @Test
    @Timeout(20)
    void repeatedRegistrationRetriesStartupAfterListenerPortBecomesAvailable() throws Exception {
        try (DatagramSocket wallbox = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0));
                DatagramSocket occupiedPort = new DatagramSocket(0)) {
            wallbox.setSoTimeout(5000);
            int listenerPort = occupiedPort.getLocalPort();
            KeContactTransceiver transceiver = new KeContactTransceiver(listenerPort, wallbox.getLocalPort());
            KeContactHandler handler = Objects.requireNonNull(createHandler("127.0.0.1"));
            AtomicReference<ByteBuffer> response = new AtomicReference<>();
            Thread sender = new Thread(() -> response.set(transceiver.send("report 2", handler)));
            sender.setDaemon(true);
            try {
                transceiver.registerHandler(handler);
                assertEquals(-1, transceiver.getLocalPort());
                occupiedPort.close();

                transceiver.registerHandler(handler);
                assertEquals(listenerPort, transceiver.getLocalPort());
                sender.start();
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                wallbox.receive(request);
                assertEquals("report 2", new String(request.getData(), request.getOffset(), request.getLength(),
                        StandardCharsets.US_ASCII));
                byte[] reply = "{\"ID\":2}".getBytes(StandardCharsets.US_ASCII);
                wallbox.send(new DatagramPacket(reply, reply.length, request.getSocketAddress()));
                sender.join(TimeUnit.SECONDS.toMillis(5));
                assertFalse(sender.isAlive());
                ByteBuffer received = response.get();
                assertNotNull(received);
                assertEquals("{\"ID\":2}",
                        new String(received.array(), 0, received.limit(), StandardCharsets.US_ASCII));
            } finally {
                transceiver.unRegisterHandler(handler);
                sender.interrupt();
                sender.join(TimeUnit.SECONDS.toMillis(5));
                transceiver.stop();
            }
        }
    }

    @Test
    @Timeout(20)
    void unregisterDoesNotStopAConcurrentlyRegisteredHandler() throws Exception {
        try (DatagramSocket wallbox = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0))) {
            wallbox.setSoTimeout(5000);
            CountDownLatch beforeStop = new CountDownLatch(1);
            CountDownLatch resumeStop = new CountDownLatch(1);
            KeContactTransceiver transceiver = new KeContactTransceiver(0, wallbox.getLocalPort()) {
                @Override
                void stop(boolean onlyIfEmpty) {
                    if (onlyIfEmpty) {
                        beforeStop.countDown();
                        try {
                            if (!resumeStop.await(5, TimeUnit.SECONDS)) {
                                throw new IllegalStateException("Timed out waiting to resume conditional stop");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(e);
                        }
                    }
                    super.stop(onlyIfEmpty);
                }
            };
            KeContactHandler oldHandler = Objects.requireNonNull(createHandler("127.0.0.1"));
            KeContactHandler newHandler = Objects.requireNonNull(createHandler("127.0.0.1"));
            AtomicReference<Throwable> unregisterFailure = new AtomicReference<>();
            Thread unregister = new Thread(() -> {
                try {
                    transceiver.unRegisterHandler(oldHandler);
                } catch (RuntimeException | Error e) {
                    unregisterFailure.set(e);
                }
            });
            AtomicReference<ByteBuffer> response = new AtomicReference<>();
            Thread sender = new Thread(() -> response.set(transceiver.send("report 2", newHandler)));
            unregister.setDaemon(true);
            sender.setDaemon(true);
            try {
                transceiver.registerHandler(oldHandler);
                int listenerPort = transceiver.getLocalPort();
                unregister.start();
                assertTrue(beforeStop.await(5, TimeUnit.SECONDS));
                transceiver.registerHandler(newHandler);
                resumeStop.countDown();
                unregister.join(TimeUnit.SECONDS.toMillis(5));
                assertFalse(unregister.isAlive());
                assertNull(unregisterFailure.get());
                assertEquals(listenerPort, transceiver.getLocalPort());

                sender.start();
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                wallbox.receive(request);
                assertEquals("report 2", new String(request.getData(), request.getOffset(), request.getLength(),
                        StandardCharsets.US_ASCII));
                byte[] reply = "{\"ID\":2}".getBytes(StandardCharsets.US_ASCII);
                wallbox.send(new DatagramPacket(reply, reply.length, request.getSocketAddress()));
                sender.join(TimeUnit.SECONDS.toMillis(5));
                assertFalse(sender.isAlive());
                ByteBuffer received = response.get();
                assertNotNull(received);
                assertEquals("{\"ID\":2}",
                        new String(received.array(), 0, received.limit(), StandardCharsets.US_ASCII));
            } finally {
                resumeStop.countDown();
                unregister.interrupt();
                unregister.join(TimeUnit.SECONDS.toMillis(5));
                transceiver.unRegisterHandler(oldHandler);
                transceiver.unRegisterHandler(newHandler);
                sender.interrupt();
                sender.join(TimeUnit.SECONDS.toMillis(5));
                transceiver.stop();
            }
        }
    }

    @Test
    @Timeout(20)
    void unregisterReleasesPendingSend() throws InterruptedException {
        KeContactTransceiver transceiver = new KeContactTransceiver();
        KeContactHandler handler = Objects.requireNonNull(createHandler());
        AtomicReference<ByteBuffer> response = new AtomicReference<>();
        Thread sender = new Thread(() -> response.set(transceiver.send("report 1", handler)));
        sender.setDaemon(true);
        boolean registered = false;
        try {
            transceiver.registerHandler(handler);
            registered = true;
            sender.start();
            awaitThreadState(sender);

            transceiver.unRegisterHandler(handler);
            sender.join(TimeUnit.SECONDS.toMillis(2));

            assertFalse(sender.isAlive());
            assertNull(response.get());
        } finally {
            if (registered) {
                transceiver.unRegisterHandler(handler);
            }
            sender.interrupt();
            sender.join(TimeUnit.SECONDS.toMillis(2));
            transceiver.stop();
        }
    }

    @Test
    @Timeout(30)
    void concurrentCallsKeepTheirOwnReplies() throws Exception {
        try (DatagramSocket wallbox = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0))) {
            wallbox.setSoTimeout(10000);
            KeContactTransceiver transceiver = new KeContactTransceiver(0, wallbox.getLocalPort());
            KeContactHandler handler = Objects.requireNonNull(createHandler("127.0.0.1"));
            AtomicReference<ByteBuffer> firstResponse = new AtomicReference<>();
            AtomicReference<ByteBuffer> secondResponse = new AtomicReference<>();
            Thread first = new Thread(() -> firstResponse.set(transceiver.send("report 1", handler)));
            Thread second = new Thread(() -> secondResponse.set(transceiver.send("report 2", handler)));
            first.setDaemon(true);
            second.setDaemon(true);
            try {
                transceiver.registerHandler(handler);
                first.start();
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                wallbox.receive(request);
                assertEquals("report 1",
                        new String(request.getData(), 0, request.getLength(), StandardCharsets.US_ASCII));
                second.start();
                awaitThreadState(second);
                byte[] firstReply = "{\"ID\":1}".getBytes(StandardCharsets.US_ASCII);
                wallbox.send(new DatagramPacket(firstReply, firstReply.length, request.getSocketAddress()));
                request.setLength(1024);
                wallbox.receive(request);
                assertEquals("report 2",
                        new String(request.getData(), 0, request.getLength(), StandardCharsets.US_ASCII));
                byte[] secondReply = "{\"ID\":2}".getBytes(StandardCharsets.US_ASCII);
                wallbox.send(new DatagramPacket(secondReply, secondReply.length, request.getSocketAddress()));
                first.join(TimeUnit.SECONDS.toMillis(5));
                second.join(TimeUnit.SECONDS.toMillis(5));
                assertFalse(first.isAlive());
                assertFalse(second.isAlive());
                ByteBuffer firstReceived = firstResponse.get();
                ByteBuffer secondReceived = secondResponse.get();
                assertNotNull(firstReceived);
                assertNotNull(secondReceived);
                assertEquals("{\"ID\":1}",
                        new String(firstReceived.array(), 0, firstReceived.limit(), StandardCharsets.US_ASCII));
                assertEquals("{\"ID\":2}",
                        new String(secondReceived.array(), 0, secondReceived.limit(), StandardCharsets.US_ASCII));
            } finally {
                transceiver.unRegisterHandler(handler);
                first.interrupt();
                second.interrupt();
                first.join(TimeUnit.SECONDS.toMillis(5));
                second.join(TimeUnit.SECONDS.toMillis(5));
                transceiver.stop();
            }
        }
    }

    @Test
    @Timeout(20)
    void stopReleasesPendingSend() throws InterruptedException {
        KeContactTransceiver transceiver = new KeContactTransceiver();
        KeContactHandler handler = Objects.requireNonNull(createHandler());
        AtomicReference<ByteBuffer> response = new AtomicReference<>();
        Thread sender = new Thread(() -> response.set(transceiver.send("report 1", handler)));
        sender.setDaemon(true);
        boolean registered = false;
        try {
            transceiver.registerHandler(handler);
            registered = true;
            sender.start();
            awaitThreadState(sender);

            transceiver.stop();
            sender.join(TimeUnit.SECONDS.toMillis(2));

            assertFalse(sender.isAlive());
            assertNull(response.get());
        } finally {
            if (registered) {
                transceiver.unRegisterHandler(handler);
            }
            sender.interrupt();
            sender.join(TimeUnit.SECONDS.toMillis(2));
            transceiver.stop();
        }
    }

    private static KeContactHandler createHandler() {
        return createHandler("192.0.2.1");
    }

    private static KeContactHandler createHandler(String host) {
        Thing thing = mock(Thing.class);
        ThingStatusInfo statusInfo = mock(ThingStatusInfo.class);
        when(statusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.NONE);
        when(thing.getStatusInfo()).thenReturn(statusInfo);
        when(thing.getUID()).thenReturn(new ThingUID("keba:kecontact:test"));

        KeContactHandler handler = mock(KeContactHandler.class);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getIPAddress()).thenReturn(host);
        return handler;
    }

    private static void awaitThreadState(Thread thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (thread.isAlive() && !isWaiting(thread.getState()) && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(isWaiting(thread.getState()));
    }

    private static boolean isWaiting(Thread.State state) {
        return state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
    }
}
