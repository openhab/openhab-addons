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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.monitor.Direction;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Type;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXTimeoutException;
import io.calimero.Priority;
import io.calimero.cemi.CEMILData;
import io.calimero.datapoint.StateDP;
import io.calimero.link.KNXLinkClosedException;

/**
 * Checks that what openHAB sends shows in the group monitor with what became of it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OwnSendTest {
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 2, 3);
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 42);
    private static final GroupAddress SECURE_GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress PLAIN_GROUP = new GroupAddress(1, 0, 0);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private @Nullable MonitorTestClient client;

    @AfterEach
    void tearDown() {
        MonitorTestClient tmp = client;
        if (tmp != null) {
            tmp.dispose();
        }
        scheduler.shutdownNow();
    }

    private MonitorTestClient connect(TelegramMonitor monitor, FeedLink link) {
        MonitorTestClient c = new MonitorTestClient(scheduler, monitor, () -> link);
        client = c;
        c.initialize();
        assertTrue(c.isConnected());
        return c;
    }

    /** {@return the outcome of each row, "null" for a received one} */
    private static List<String> outcomes(TelegramMonitor monitor) {
        return monitor.records().stream().map(r -> String.valueOf(r.outcome())).toList();
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + 10_000_000_000L;
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < end, "condition not met within 10 seconds");
            Thread.sleep(5);
        }
    }

    @Test
    void aWriteThatWentOutIsOneSentRow() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON));

        List<TelegramRecord> records = monitor.records();
        assertEquals(1, records.size());
        TelegramRecord record = records.get(0);
        assertEquals(Direction.OUT, record.direction());
        assertEquals(TelegramType.WRITE, record.type());
        assertEquals(OPENHAB, record.source());
        assertEquals(PLAIN_GROUP, record.destination());
        assertEquals("1.001", record.dataType());
        assertEquals("ON", record.value());
        assertEquals(SecurityState.PLAIN, record.security());
        assertEquals(SendOutcome.SENT, record.outcome());
    }

    @Test
    void aWriteToAnAddressWithAGroupKeyIsSecured() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        c.writeToKNX(spec(SECURE_GROUP, OnOffType.OFF));

        assertEquals(SecurityState.SECURED, monitor.records().get(0).security());
    }

    @Test
    void eachAttemptIsARow() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);

        link.failNextSend(new KNXTimeoutException("made-up missing confirmation"));
        c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON));
        link.failNextSend(new KNXLinkClosedException("made-up failure"));
        c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON));

        assertEquals(List.of("NOT_CONFIRMED", "SENT", "FAILED", "SENT"), outcomes(monitor));
        assertEquals(2, link.sent().size());
    }

    @Test
    void aWriteThatNeverWentOutShowsBothAttempts() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);
        link.failNextSend(new KNXTimeoutException("made-up missing confirmation"));
        link.failNextSend(new KNXTimeoutException("made-up missing confirmation"));

        assertThrows(KNXTimeoutException.class, () -> c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON)));

        assertEquals(List.of("NOT_CONFIRMED", "NOT_CONFIRMED"), outcomes(monitor));
    }

    @Test
    void aWriteSaysWhetherItWentOut() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);

        assertTrue(c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON)));
        c.dispose();
        assertFalse(c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON)));

        assertEquals(1, link.sent().size());
        assertEquals(List.of("SENT"), outcomes(monitor));
    }

    @Test
    void aReadResponseIsAResponseRow() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        MonitorTestClient c = connect(monitor, new FeedLink(OPENHAB));

        c.respondToKNX(spec(PLAIN_GROUP, OnOffType.ON));

        TelegramRecord record = monitor.records().get(0);
        assertEquals(TelegramType.RESPONSE, record.type());
        assertEquals(SendOutcome.SENT, record.outcome());
    }

    @Test
    void aReadRequestShowsBeforeItsAnswer() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        link.answerReads((destination, request) -> new CEMILData(CEMILData.MC_LDATA_IND, SENSOR, destination,
                new byte[] { 0x00, 0x41 }, Priority.LOW));
        MonitorTestClient c = connect(monitor, link);

        c.readDatapoint(new StateDP(PLAIN_GROUP, "made up", 0, "1.001"));
        waitFor(() -> outcomes(monitor).equals(List.of("SENT", "null")));

        List<TelegramRecord> records = monitor.records();
        assertEquals(Direction.OUT, records.get(0).direction());
        assertEquals(TelegramType.READ, records.get(0).type());
        assertEquals("1.001", records.get(0).dataType());
        assertEquals(Direction.IN, records.get(1).direction());
        assertEquals(TelegramType.RESPONSE, records.get(1).type());
        assertTrue(records.get(0).number() < records.get(1).number());
    }

    @Test
    void aReadWithoutAnswerIsNotShownAsNotConfirmed() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);

        c.readDatapoint(new StateDP(PLAIN_GROUP, "made up", 0, "1.001"));
        waitFor(() -> outcomes(monitor).equals(List.of("NO_ANSWER")));

        assertEquals(1, link.sent().size(), "the request went out");
    }

    @Test
    void aReadRequestWithoutConfirmationIsNotConfirmed() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        link.failNextSend(new KNXTimeoutException("made-up missing confirmation"));
        MonitorTestClient c = connect(monitor, link);

        c.readDatapoint(new StateDP(PLAIN_GROUP, "made up", 0, "1.001"));
        waitFor(() -> outcomes(monitor).equals(List.of("NOT_CONFIRMED")));

        assertEquals(0, link.sent().size());
    }

    @Test
    void theOutcomeOfAReadDependsOnWhetherTheRequestWentOut() {
        KNXTimeoutException timeout = new KNXTimeoutException("made-up timeout");

        assertEquals(SendOutcome.NO_ANSWER, AbstractKNXClient.readOutcome(timeout, true));
        assertEquals(SendOutcome.NOT_CONFIRMED, AbstractKNXClient.readOutcome(timeout, false));
        assertEquals(SendOutcome.SENT, AbstractKNXClient.readOutcome(new InterruptedException(), true));
        assertEquals(SendOutcome.FAILED, AbstractKNXClient.readOutcome(new IllegalStateException(), false));
    }

    @Test
    void anInterruptedSendIsNoMissingConfirmation() {
        Thread.currentThread().interrupt();
        try {
            assertEquals(SendOutcome.FAILED,
                    AbstractKNXClient.readOutcome(new KNXTimeoutException("interrupted"), false));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void aFailingMonitorDoesNotStopTheSend() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor(new BrokenClock());
        FeedLink link = new FeedLink(OPENHAB);
        MonitorTestClient c = connect(monitor, link);

        c.writeToKNX(spec(PLAIN_GROUP, OnOffType.ON));
        c.respondToKNX(spec(PLAIN_GROUP, OnOffType.ON));
        c.readDatapoint(new StateDP(PLAIN_GROUP, "made up", 0, "1.001"));
        waitFor(() -> link.sent().size() == 3);

        assertEquals(List.of(), monitor.records());
    }

    private static OutboundSpec spec(GroupAddress groupAddress, Type value) {
        return new OutboundSpec() {
            @Override
            public String getDPT() {
                return "1.001";
            }

            @Override
            public GroupAddress getGroupAddress() {
                return groupAddress;
            }

            @Override
            public Type getValue() {
                return value;
            }

            @Override
            public boolean matchesDestination(GroupAddress other) {
                return groupAddress.equals(other);
            }
        };
    }

    /** Fails every reading, as a monitor that is broken inside would. */
    private static class BrokenClock extends Clock {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            throw new IllegalStateException("made-up failure inside the monitor");
        }
    }
}
