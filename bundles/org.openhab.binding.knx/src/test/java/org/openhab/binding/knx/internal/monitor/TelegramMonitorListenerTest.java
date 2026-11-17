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
package org.openhab.binding.knx.internal.monitor;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the monitor's listener: one signal per row after the row is stored and the lock is released, one per
 * outcome of an own send, and a listener that throws or is removed never disturbs the monitor.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramMonitorListenerTest {
    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);
    private static final IndividualAddress OWN = new IndividualAddress(7, 7, 250);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);

    /** Notes every call, and whether the monitor's lock was held and the row could be read at that moment. */
    private static final class Recorder implements TelegramMonitor.Listener {
        final List<String> calls = new ArrayList<>();
        final TelegramMonitor monitor;
        final Object lock;

        Recorder(TelegramMonitor monitor) throws ReflectiveOperationException {
            this.monitor = monitor;
            Field field = TelegramMonitor.class.getDeclaredField("lock");
            field.setAccessible(true);
            this.lock = Objects.requireNonNull(field.get(monitor));
        }

        @Override
        public void appended(long number) {
            assertFalse(Thread.holdsLock(lock), "appended under the monitor's lock");
            assertEquals(number, monitor.lastNumber());
            assertTrue(monitor.records().stream().anyMatch(row -> row.number() == number), "row not stored yet");
            calls.add("appended " + number);
        }

        @Override
        public void outcomeChanged(long number, SendOutcome outcome) {
            assertFalse(Thread.holdsLock(lock), "outcomeChanged under the monitor's lock");
            calls.add("outcome " + number + " " + outcome);
        }
    }

    private static final class Throwing implements TelegramMonitor.Listener {
        int calls;

        @Override
        public void appended(long number) {
            calls++;
            throw new IllegalStateException("made-up listener failure");
        }

        @Override
        public void outcomeChanged(long number, SendOutcome outcome) {
            calls++;
            throw new IllegalStateException("made-up listener failure");
        }
    }

    @Test
    void everyKindOfRowIsSignalledOnceWithItsNumber() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        Recorder recorder = new Recorder(monitor);
        monitor.addListener(recorder);

        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        monitor.sent(OWN, LIGHT, TelegramType.WRITE, new byte[] { 0 }, "1.001", null, SecurityState.PLAIN,
                SendOutcome.SENT);
        monitor.notDecrypted(SENSOR, LIGHT, NotDecryptedReason.NO_KEY, address -> true);
        monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);

        assertEquals(List.of("appended 1", "appended 2", "appended 3", "appended 4"), recorder.calls);
    }

    @Test
    void theEndOfAnOwnSendSignalsItsFinalOutcome() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        Recorder recorder = new Recorder(monitor);
        monitor.addListener(recorder);

        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        send.ended(SendOutcome.NO_ANSWER);

        assertEquals(List.of("appended 1", "outcome 1 NO_ANSWER"), recorder.calls);
        assertEquals(SendOutcome.NO_ANSWER, monitor.records().get(0).outcome());
    }

    @Test
    void aListenerThatThrowsStopsNeitherTheReceivePathNorASendNorTheNextListener() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        Throwing throwing = new Throwing();
        Recorder recorder = new Recorder(monitor);
        monitor.addListener(throwing);
        monitor.addListener(recorder);

        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        send.ended(SendOutcome.SENT);

        assertEquals(3, throwing.calls);
        assertEquals(List.of("appended 1", "appended 2", "outcome 2 SENT"), recorder.calls);
        assertEquals(2, monitor.records().size());
    }

    @Test
    void aRemovedListenerIsNotCalledAgain() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        Recorder recorder = new Recorder(monitor);
        monitor.addListener(recorder);
        OwnSend send = monitor.sending(OWN, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);

        monitor.removeListener(recorder);
        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        send.ended(SendOutcome.SENT);

        assertEquals(List.of("appended 1"), recorder.calls);
    }

    @Test
    void withBufferSizeZeroNoRowIsSignalled() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.resize(0);
        Recorder recorder = new Recorder(monitor);
        monitor.addListener(recorder);

        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);

        assertEquals(List.of(), recorder.calls);
    }
}
