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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.monitor.Direction;
import org.openhab.binding.knx.internal.monitor.SendOutcome;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Type;

import io.calimero.DataUnitBuilder;
import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.Priority;
import io.calimero.cemi.CEMILData;
import io.calimero.datapoint.StateDP;
import io.calimero.link.KNXLinkClosedException;

/**
 * Checks that the row of an own send holds the value bytes that went to the bus, and that those bytes are what
 * they were before the monitor recorded them.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OwnSendPayloadTest {
    private static final IndividualAddress OPENHAB = new IndividualAddress(7, 7, 250);
    private static final IndividualAddress ACTUATOR = new IndividualAddress(7, 7, 1);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);
    private static final GroupAddress DISPLAY = new GroupAddress(31, 7, 6);
    private static final String TEXT = "Made-up text 7";

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

    private static void sendThree(MonitorTestClient c) throws Exception {
        c.writeToKNX(spec(LIGHT, "1.001", OnOffType.ON));
        c.writeToKNX(spec(TEMPERATURE, "9.001", new DecimalType(21.5)));
        c.respondToKNX(spec(DISPLAY, "16.000", new StringType(TEXT)));
    }

    private static byte[] withService(int service, byte... data) {
        byte[] apdu = new byte[data.length + 2];
        apdu[1] = (byte) service;
        System.arraycopy(data, 0, apdu, 2, data.length);
        return apdu;
    }

    @Test
    void theFramesAreThoseOfTheStartCommit() throws Exception {
        assertEquals(14, TEXT.length());
        FeedLink link = new FeedLink(OPENHAB);
        sendThree(connect(new TelegramMonitor(), link));

        List<byte[]> frames = link.sent();

        assertEquals(3, frames.size());
        assertArrayEquals(new byte[] { 0x00, (byte) 0x81 }, frames.get(0));
        assertArrayEquals(new byte[] { 0x00, (byte) 0x80, 0x0c, 0x33 }, frames.get(1));
        assertArrayEquals(withService(0x40, TEXT.getBytes(StandardCharsets.US_ASCII)), frames.get(2));
    }

    @Test
    void eachOwnSendRowHoldsTheValueBytesOfItsFrame() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        sendThree(connect(monitor, link));

        List<byte[]> frames = link.sent();
        List<TelegramRecord> records = monitor.records();

        assertEquals(3, records.size());
        for (int i = 0; i < 3; i++) {
            assertArrayEquals(DataUnitBuilder.extractASDU(frames.get(i)), records.get(i).payload(), "row " + i);
        }
        assertArrayEquals(new byte[] { 0x01 }, records.get(0).payload());
        assertArrayEquals(new byte[] { 0x0c, 0x33 }, records.get(1).payload());
        assertEquals(TelegramType.RESPONSE, records.get(2).type());
        assertEquals(TEXT, new String(records.get(2).payload(), StandardCharsets.US_ASCII));
        assertEquals("21.5", records.get(1).value(), "the row text stays as it was");
    }

    @Test
    void aFailedAttemptHasTheBytesAndAReadRequestHasNone() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        link.answerReads((destination, request) -> request.length == 2 && (request[1] & 0xc0) == 0
                ? new CEMILData(CEMILData.MC_LDATA_IND, ACTUATOR, destination, new byte[] { 0x00, 0x41 }, Priority.LOW)
                : null);
        MonitorTestClient c = connect(monitor, link);
        link.failNextSend(new KNXLinkClosedException("made-up failure"));

        c.writeToKNX(spec(TEMPERATURE, "9.001", new DecimalType(21.5)));
        c.readDatapoint(new StateDP(LIGHT, "made up", 0, "1.001"));
        waitFor(() -> monitor.records().stream()
                .anyMatch(r -> r.type() == TelegramType.READ && r.outcome() != SendOutcome.PENDING));

        List<TelegramRecord> records = monitor.records();
        assertEquals(SendOutcome.FAILED, records.get(0).outcome(), records::toString);
        assertArrayEquals(new byte[] { 0x0c, 0x33 }, records.get(0).payload());
        assertEquals(SendOutcome.SENT, records.get(1).outcome());
        assertArrayEquals(new byte[] { 0x0c, 0x33 }, records.get(1).payload());
        TelegramRecord read = records.get(2);
        assertEquals(TelegramType.READ, read.type());
        assertEquals(Direction.OUT, read.direction());
        assertArrayEquals(new byte[0], read.payload());
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long end = System.nanoTime() + 10_000_000_000L;
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < end, "condition not met within 10 seconds");
            Thread.sleep(5);
        }
    }

    private static OutboundSpec spec(GroupAddress groupAddress, String dpt, Type value) {
        return new OutboundSpec() {
            @Override
            public String getDPT() {
                return dpt;
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
}
