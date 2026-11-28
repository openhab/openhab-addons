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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks reading on after a row number: the filter's {@code afterNumber}, and the monitor's last number and id.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramAfterNumberTest {
    private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");
    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);

    private final List<TelegramRecord> rows = new ArrayList<>();

    /** Rows 1 to 6, one second apart; rows 3 and 6 are reads. */
    private void sixRows() {
        for (int i = 1; i <= 6; i++) {
            rows.add(new TelegramRecord(i, START.plusSeconds(i), SENSOR, i % 2 == 0 ? LIGHT : TEMPERATURE,
                    i % 3 == 0 ? TelegramType.READ : TelegramType.WRITE, Direction.IN, new byte[] { 1 },
                    SecurityState.PLAIN, null, null, null, null, null, null, null, false));
        }
    }

    private List<Long> numbers(TelegramFilter filter) {
        return filter.apply(rows).stream().map(TelegramRecord::number).toList();
    }

    @Test
    void onlyRowsAboveTheNumberMatch() {
        sixRows();
        assertEquals(List.of(4L, 5L, 6L), numbers(TelegramFilter.builder().afterNumber(3).build()));
        assertFalse(TelegramFilter.builder().afterNumber(3).build().matches(rows.get(2)));
        assertTrue(TelegramFilter.builder().afterNumber(3).build().matches(rows.get(3)));
        assertEquals(List.of(), numbers(TelegramFilter.builder().afterNumber(6).build()));
    }

    @Test
    void withANumberTheLimitKeepsTheOldestRows() {
        sixRows();
        assertEquals(List.of(4L, 5L), numbers(TelegramFilter.builder().afterNumber(3).limit(2).build()));
        assertEquals(List.of(1L, 2L), numbers(TelegramFilter.builder().afterNumber(0).limit(2).build()));
        assertEquals(List.of(1L, 2L), numbers(TelegramFilter.builder().afterNumber(-5).limit(2).build()));
        assertEquals(List.of(5L, 6L), numbers(TelegramFilter.builder().limit(2).build()));
    }

    @Test
    void contextRowsAtOrBelowTheNumberNeverAppear() {
        sixRows();
        TelegramFilter reads = TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(1)).build();
        assertEquals(List.of(2L, 3L, 4L, 5L, 6L), numbers(reads));

        TelegramFilter readsAfter3 = TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(1))
                .afterNumber(3).build();
        assertEquals(List.of(5L, 6L), numbers(readsAfter3));
        TelegramFilter readsAfter2 = TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(1))
                .afterNumber(2).build();
        assertEquals(List.of(3L, 4L, 5L, 6L), numbers(readsAfter2));
    }

    @Test
    void theMonitorTellsItsLastNumberAndItsId() {
        TelegramMonitor monitor = new TelegramMonitor();
        assertEquals(0, monitor.lastNumber());
        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);
        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 0 }, SecurityState.PLAIN);
        assertEquals(2, monitor.lastNumber());
        monitor.clear();
        assertEquals(2, monitor.lastNumber());
        monitor.resize(0);
        monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { 0 }, SecurityState.PLAIN);
        assertEquals(3, monitor.lastNumber());

        String id = monitor.id();
        assertFalse(id.isBlank());
        assertEquals(id, monitor.id());
        assertNotEquals(id, new TelegramMonitor().id());
    }

    @Test
    void aQueryAfterANumberLooksOnlyAtTheRowsAboveIt() {
        TelegramMonitor monitor = new TelegramMonitor();
        AtomicInteger asked = new AtomicInteger();
        monitor.useProject(new ProjectLookup() {
            @Override
            public @Nullable String groupAddressName(GroupAddress address) {
                return null;
            }

            @Override
            public @Nullable String dataType(GroupAddress address) {
                return null;
            }

            @Override
            public @Nullable String deviceName(IndividualAddress address) {
                asked.incrementAndGet();
                return null;
            }
        });
        for (int i = 0; i < 10; i++) {
            monitor.received(SENSOR, LIGHT, TelegramType.WRITE, new byte[] { (byte) i }, SecurityState.PLAIN);
        }

        List<TelegramRecord> after8 = monitor.query(TelegramFilter.builder().afterNumber(8).build());

        assertEquals(List.of(9L, 10L), after8.stream().map(TelegramRecord::number).toList());
        assertTrue(asked.get() <= 4, "rows at or below the number were enriched: " + asked.get());
        asked.set(0);
        assertEquals(10, monitor.query(TelegramFilter.ALL).size());
        assertTrue(asked.get() >= 10, "without a number every row is looked at");
    }

    @Test
    void aQueryAfterANumberWithOnlyALimitLooksAtNoMoreRowsThanTheLimit() {
        AtomicInteger asked = new AtomicInteger();
        TelegramMonitor monitor = countingMonitor(asked);
        monitor.resize(20_000);
        for (int i = 0; i < 20_000; i++) {
            monitor.received(SENSOR, i % 2 == 0 ? LIGHT : TEMPERATURE, TelegramType.WRITE, new byte[] { 1 },
                    SecurityState.PLAIN);
        }

        assertEquals(LongStream.rangeClosed(1, 256).boxed().toList(),
                numbers(monitor.query(TelegramFilter.builder().afterNumber(0).limit(256).build())));
        assertTrue(asked.get() <= 2 * 256, "rows looked at from the oldest end: " + asked.get());

        asked.set(0);
        assertEquals(LongStream.rangeClosed(19_701, 19_750).boxed().toList(),
                numbers(monitor.query(TelegramFilter.builder().afterNumber(19_700).limit(50).build())));
        assertTrue(asked.get() <= 2 * 50, "rows looked at from the newest end: " + asked.get());

        assertEquals(LongStream.rangeClosed(19_991, 20_000).boxed().toList(),
                numbers(monitor.query(TelegramFilter.builder().afterNumber(19_990).limit(256).build())));
        assertEquals(List.of(), numbers(monitor.query(TelegramFilter.builder().afterNumber(20_000).limit(5).build())));
        assertEquals(List.of(), numbers(monitor.query(TelegramFilter.builder().afterNumber(10).limit(0).build())));
    }

    @Test
    void aQueryAfterANumberWithAnotherFilterStillFindsMatchesBeyondTheLimit() {
        TelegramMonitor monitor = countingMonitor(new AtomicInteger());
        for (int i = 0; i < 20; i++) {
            monitor.received(SENSOR, i < 15 ? TEMPERATURE : LIGHT, TelegramType.WRITE, new byte[] { 1 },
                    SecurityState.PLAIN);
        }

        assertEquals(List.of(16L, 17L, 18L),
                numbers(monitor.query(TelegramFilter.builder().destination(LIGHT).afterNumber(0).limit(3).build())));
        assertEquals(List.of(18L, 19L, 20L),
                numbers(monitor.query(TelegramFilter.builder().destination(LIGHT).limit(3).build())));
    }

    @Test
    void aWindowLooksAtNoMoreRowsThanAskedStopsAtTheLimitAndTellsHowFarItLooked() {
        AtomicInteger asked = new AtomicInteger();
        TelegramMonitor monitor = countingMonitor(asked);
        for (int number = 1; number <= 1000; number++) {
            monitor.received(SENSOR, number % 10 == 0 ? TEMPERATURE : LIGHT, TelegramType.WRITE, new byte[] { 1 },
                    SecurityState.PLAIN);
        }
        TelegramFilter temperature = TelegramFilter.builder().destination(TEMPERATURE).build();

        TelegramMonitor.Window window = monitor.window(temperature, 0, 50, 300);
        assertEquals(LongStream.rangeClosed(1, 30).map(n -> n * 10).boxed().toList(), numbers(window.rows()));
        assertEquals(1, window.first());
        assertEquals(300, window.last());
        assertTrue(asked.get() <= 300 + 30, "rows looked at: " + asked.get());

        window = monitor.window(temperature, 300, 5, 5000);
        assertEquals(List.of(310L, 320L, 330L, 340L, 350L), numbers(window.rows()));
        assertEquals(301, window.first());
        assertEquals(350, window.last());

        window = monitor.window(temperature, 995, 5, 5000);
        assertEquals(List.of(1000L), numbers(window.rows()));
        assertEquals(996, window.first());
        assertEquals(1000, window.last());

        for (TelegramMonitor.Window none : List.of(monitor.window(temperature, 1000, 5, 5000),
                monitor.window(temperature, 0, 0, 5000), monitor.window(temperature, 0, 5, 0))) {
            assertEquals(List.of(), none.rows());
            assertEquals(0, none.first());
            assertEquals(0, none.last());
        }
        assertEquals(LongStream.rangeClosed(1, 50).map(n -> n * 10).boxed().toList(), numbers(
                monitor.query(TelegramFilter.builder().destination(TEMPERATURE).afterNumber(0).limit(50).build())));
    }

    private static List<Long> numbers(List<TelegramRecord> rows) {
        return rows.stream().map(TelegramRecord::number).toList();
    }

    private static TelegramMonitor countingMonitor(AtomicInteger asked) {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useProject(new ProjectLookup() {
            @Override
            public @Nullable String groupAddressName(GroupAddress address) {
                return null;
            }

            @Override
            public @Nullable String dataType(GroupAddress address) {
                return null;
            }

            @Override
            public @Nullable String deviceName(IndividualAddress address) {
                asked.incrementAndGet();
                return null;
            }
        });
        return monitor;
    }
}
