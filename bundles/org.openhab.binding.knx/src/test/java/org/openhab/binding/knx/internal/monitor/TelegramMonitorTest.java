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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the store of the group monitor: the ring, the running numbers, the last telegram per address and what is
 * added when rows are handed out.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramMonitorTest {
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 20);
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 1, 250);
    private static final GroupAddress TEMPERATURE = new GroupAddress(3, 1, 10);
    private static final GroupAddress LIGHT = new GroupAddress(3, 1, 11);
    private static final GroupAddress UNUSED = new GroupAddress(3, 1, 99);
    private static final byte[] TWENTY_ONE_AND_A_HALF = { 0x0c, 0x33 };

    private final StepClock clock = new StepClock();
    private final TelegramMonitor monitor = new TelegramMonitor(clock);

    private void receive(GroupAddress destination, byte[] asdu) {
        monitor.received(SENSOR, destination, TelegramType.WRITE, asdu, SecurityState.PLAIN);
    }

    private void receive(GroupAddress destination, int value) {
        receive(destination, new byte[] { (byte) value });
    }

    private List<Long> numbers() {
        return monitor.records().stream().map(TelegramRecord::number).toList();
    }

    @Test
    void numbersStartAtOneAndRowsComeOldestFirst() {
        receive(LIGHT, 1);
        receive(LIGHT, 0);

        List<TelegramRecord> records = monitor.records();
        assertEquals(List.of(1L, 2L), records.stream().map(TelegramRecord::number).toList());
        assertTrue(records.get(0).time().isBefore(records.get(1).time()));
    }

    @Test
    void theRingKeepsTheNewestRows() {
        monitor.resize(3);
        for (int i = 0; i < 5; i++) {
            receive(LIGHT, i & 1);
        }

        assertEquals(List.of(3L, 4L, 5L), numbers());
        assertEquals(3, monitor.status().rows());
        assertEquals(3, monitor.status().bufferSize());
    }

    @Test
    void numbersKeepCountingAcrossResizeAndClear() {
        monitor.resize(2);
        receive(LIGHT, 1);
        receive(LIGHT, 1);
        receive(LIGHT, 1);
        monitor.resize(5);
        receive(LIGHT, 1);
        assertEquals(List.of(2L, 3L, 4L), numbers());

        monitor.clear();
        assertEquals(List.of(), numbers());
        receive(LIGHT, 1);

        assertEquals(List.of(5L), numbers());
    }

    @Test
    void aResizeKeepsTheNewestRows() {
        for (int i = 0; i < 6; i++) {
            receive(LIGHT, 1);
        }

        monitor.resize(2);

        assertEquals(List.of(5L, 6L), numbers());
    }

    @Test
    void sizeZeroKeepsNoRowsAndNoLastValuesButKeepsCounting() {
        receive(LIGHT, 1);
        monitor.resize(0);
        receive(LIGHT, 0);

        assertEquals(List.of(), monitor.records());
        assertEquals(Map.of(), monitor.lastTelegrams());
        assertEquals(2, monitor.status().received());

        monitor.resize(10);
        receive(LIGHT, 1);
        assertEquals(List.of(3L), numbers());
    }

    @Test
    void theLastTelegramOfAnAddressOutlivesItsRow() {
        monitor.resize(2);
        receive(TEMPERATURE, TWENTY_ONE_AND_A_HALF);
        receive(LIGHT, 1);
        receive(LIGHT, 0);

        Map<GroupAddress, TelegramRecord> last = monitor.lastTelegrams();

        assertEquals(List.of(2L, 3L), numbers());
        assertEquals(1L, Objects.requireNonNull(last.get(TEMPERATURE)).number());
        assertEquals(3L, Objects.requireNonNull(last.get(LIGHT)).number());
    }

    @Test
    void aReadRequestIsNoLastValue() {
        receive(LIGHT, 1);
        monitor.received(SENSOR, LIGHT, TelegramType.READ, new byte[0], SecurityState.PLAIN);

        assertEquals(1L, Objects.requireNonNull(monitor.lastTelegrams().get(LIGHT)).number());
    }

    @Test
    void clearEmptiesRowsAndLastValues() {
        receive(LIGHT, 1);

        monitor.clear();

        assertEquals(List.of(), monitor.records());
        assertEquals(Map.of(), monitor.lastTelegrams());
    }

    @Test
    void theChannelsTypeDecodesTheValue() {
        monitor.useChannelTypes(address -> TEMPERATURE.equals(address) ? new DataType("9.001", DecimalType.class)
                : LIGHT.equals(address) ? new DataType("1.001", OnOffType.class) : null);
        receive(TEMPERATURE, TWENTY_ONE_AND_A_HALF);
        receive(LIGHT, 1);

        List<TelegramRecord> records = monitor.records();

        assertEquals("9.001", records.get(0).dataType());
        assertEquals("21.5", records.get(0).value());
        assertEquals("°C", records.get(0).unit());
        assertEquals("ON", records.get(1).value());
        assertNull(records.get(1).unit());
    }

    @Test
    void withoutAChannelTheBytesStayRaw() {
        receive(UNUSED, TWENTY_ONE_AND_A_HALF);

        TelegramRecord record = monitor.records().get(0);

        assertNull(record.dataType());
        assertNull(record.value());
        assertArrayEquals(TWENTY_ONE_AND_A_HALF, record.payload());
    }

    @Test
    void aPayloadThatDoesNotFitTheTypeStaysRaw() {
        monitor.useChannelTypes(address -> new DataType("9.001", DecimalType.class));
        receive(TEMPERATURE, 0x0c);

        TelegramRecord record = monitor.records().get(0);

        assertEquals("9.001", record.dataType());
        assertNull(record.value());
        assertArrayEquals(new byte[] { 0x0c }, record.payload());
    }

    @Test
    void theProjectGivesTypeAndNamesWhereNoChannelDoes() {
        monitor.useChannelTypes(address -> LIGHT.equals(address) ? new DataType("1.001", OnOffType.class) : null);
        monitor.useProject(new ProjectLookup() {
            @Override
            public @Nullable String groupAddressName(GroupAddress address) {
                return "Made-up name " + address;
            }

            @Override
            public @Nullable String dataType(GroupAddress address) {
                return LIGHT.equals(address) ? "5.001" : "9.001";
            }

            @Override
            public @Nullable String deviceName(IndividualAddress address) {
                return "Made-up device";
            }
        });
        receive(TEMPERATURE, TWENTY_ONE_AND_A_HALF);
        receive(LIGHT, 1);

        List<TelegramRecord> records = monitor.records();

        assertEquals("21.5", records.get(0).value());
        assertEquals("Made-up name 3/1/10", records.get(0).destinationName());
        assertEquals("Made-up device", records.get(0).sourceName());
        assertEquals("1.001", records.get(1).dataType(), "the channel comes before the project");
    }

    @Test
    void aFailingLookupGivesRawRows() {
        monitor.useChannelTypes(address -> {
            throw new IllegalStateException("made-up failure");
        });
        receive(TEMPERATURE, TWENTY_ONE_AND_A_HALF);

        assertNull(monitor.records().get(0).value());
    }

    @Test
    void aReadRequestShowsTheTypeButNoValue() {
        monitor.useChannelTypes(address -> new DataType("1.001", OnOffType.class));
        monitor.received(SENSOR, LIGHT, TelegramType.READ, new byte[0], SecurityState.SECURED);

        TelegramRecord record = monitor.records().get(0);

        assertEquals("1.001", record.dataType());
        assertNull(record.value());
        assertEquals(SecurityState.SECURED, record.security());
        assertEquals(Direction.IN, record.direction());
        assertNull(record.outcome());
    }

    @Test
    void anOwnSendKeepsTypeAndValueItWasSentWith() {
        monitor.useChannelTypes(address -> new DataType("9.001", DecimalType.class));
        monitor.sent(OPENHAB, LIGHT, TelegramType.WRITE, new byte[0], "1.001", new ValueText("ON", null),
                SecurityState.SECURED, SendOutcome.NOT_CONFIRMED);

        TelegramRecord record = monitor.records().get(0);

        assertEquals(Direction.OUT, record.direction());
        assertEquals(OPENHAB, record.source());
        assertEquals("1.001", record.dataType());
        assertEquals("ON", record.value());
        assertEquals(SendOutcome.NOT_CONFIRMED, record.outcome());
        assertEquals(1, monitor.status().sent());
        assertEquals(0, monitor.status().received());
    }

    @Test
    void aPendingSendGetsItsOutcomeLater() {
        OwnSend read = monitor.sending(OPENHAB, LIGHT, TelegramType.READ, "1.001", null, SecurityState.PLAIN);
        assertEquals(SendOutcome.PENDING, monitor.records().get(0).outcome());
        TelegramRecord before = monitor.records().get(0);

        read.ended(SendOutcome.NO_ANSWER);

        assertEquals(SendOutcome.NO_ANSWER, monitor.records().get(0).outcome());
        assertEquals(SendOutcome.PENDING, before.outcome(), "a handed-out record does not change");
    }

    @Test
    void theStatusTellsTheConnection() {
        assertNull(monitor.status().connection());

        monitor.connected(ConnectionKind.SECURE_TUNNEL, OPENHAB);
        assertEquals(new MonitorStatus.Connection(ConnectionKind.SECURE_TUNNEL, OPENHAB),
                monitor.status().connection());

        monitor.disconnected();
        assertNull(monitor.status().connection());
    }

    @Test
    void severalThreadsAppendWhileOthersRead() throws Exception {
        TelegramMonitor shared = new TelegramMonitor();
        shared.resize(100_000);
        int writers = 4;
        int perWriter = 2_000;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(writers + 2);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int w = 0; w < writers; w++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < perWriter; i++) {
                        shared.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
                    }
                    return null;
                }));
            }
            for (int r = 0; r < 2; r++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < 50; i++) {
                        List<TelegramRecord> rows = shared.records();
                        for (int k = 1; k < rows.size(); k++) {
                            assertTrue(rows.get(k - 1).number() < rows.get(k).number());
                        }
                        shared.lastTelegrams();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        List<TelegramRecord> rows = shared.records();
        assertEquals(writers * perWriter, rows.size());
        assertEquals(writers * perWriter, rows.get(rows.size() - 1).number());
        assertEquals(writers * perWriter, shared.status().received());
    }

    /** Each reading is one millisecond after the one before. */
    private static class StepClock extends Clock {
        private Instant now = Instant.parse("2026-03-01T10:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public synchronized Instant instant() {
            now = now.plus(Duration.ofMillis(1));
            return now;
        }
    }
}
