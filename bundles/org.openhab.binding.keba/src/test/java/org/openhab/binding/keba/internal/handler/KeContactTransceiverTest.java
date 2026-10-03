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
package org.openhab.binding.keba.internal.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
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
    void unregisterReleasesPendingSend() throws InterruptedException {
        KeContactTransceiver transceiver = new KeContactTransceiver();
        KeContactHandler handler = Objects.requireNonNull(createHandler());
        AtomicReference<ByteBuffer> response = new AtomicReference<>();
        Thread sender = new Thread(() -> response.set(transceiver.send("report 1", handler)));
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
    void stopReleasesPendingSend() throws InterruptedException {
        KeContactTransceiver transceiver = new KeContactTransceiver();
        KeContactHandler handler = Objects.requireNonNull(createHandler());
        AtomicReference<ByteBuffer> response = new AtomicReference<>();
        Thread sender = new Thread(() -> response.set(transceiver.send("report 1", handler)));
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
        Thing thing = mock(Thing.class);
        ThingStatusInfo statusInfo = mock(ThingStatusInfo.class);
        when(statusInfo.getStatusDetail()).thenReturn(ThingStatusDetail.NONE);
        when(thing.getStatusInfo()).thenReturn(statusInfo);
        when(thing.getUID()).thenReturn(new ThingUID("keba:kecontact:test"));

        KeContactHandler handler = mock(KeContactHandler.class);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getIPAddress()).thenReturn("192.0.2.1");
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
