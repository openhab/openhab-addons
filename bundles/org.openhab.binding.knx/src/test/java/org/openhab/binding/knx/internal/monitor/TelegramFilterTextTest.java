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
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the filter text the console and REST share: one case per key and one per failure message.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramFilterTextTest {
    private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");
    private static final ZoneId ZONE = ZoneOffset.ofHours(1);
    private static final Clock CLOCK = Clock.fixed(START.plusSeconds(60), ZoneOffset.UTC);
    private static final IndividualAddress SENSOR = new IndividualAddress(7, 7, 20);
    private static final IndividualAddress OPENHAB = new IndividualAddress(7, 7, 250);
    private static final GroupAddress TEMPERATURE = new GroupAddress(31, 7, 2);
    private static final GroupAddress LIGHT = new GroupAddress(31, 7, 1);

    private final List<TelegramRecord> rows = new ArrayList<>();

    private void row(int seconds, IndividualAddress source, GroupAddress destination, TelegramType type,
            Direction direction, @Nullable String dataType) {
        rows.add(new TelegramRecord(rows.size() + 1, START.plusSeconds(seconds), source, destination, type, direction,
                new byte[] { 1 }, SecurityState.PLAIN, null, null, dataType, null, null, null, null, false));
    }

    private void someRows() {
        row(0, SENSOR, TEMPERATURE, TelegramType.WRITE, Direction.IN, "9.001"); // 1
        row(10, SENSOR, LIGHT, TelegramType.WRITE, Direction.IN, "1.001"); // 2
        row(20, OPENHAB, LIGHT, TelegramType.READ, Direction.OUT, "1.001"); // 3
        row(30, OPENHAB, TEMPERATURE, TelegramType.RESPONSE, Direction.OUT, "9.001"); // 4
    }

    private List<Long> numbers(String... keysAndValues) {
        TelegramFilter.Builder filter = TelegramFilter.builder();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            TelegramFilterText.apply(filter, keysAndValues[i], keysAndValues[i + 1], CLOCK, ZONE);
        }
        return filter.build().apply(rows).stream().map(TelegramRecord::number).toList();
    }

    private static @Nullable String failure(String key, String value) {
        return assertThrows(IllegalArgumentException.class,
                () -> TelegramFilterText.apply(TelegramFilter.builder(), key, value, CLOCK, ZONE)).getMessage();
    }

    @Test
    void eachKey() {
        someRows();
        assertEquals(List.of(3L, 4L), numbers("src", "7.7.250"));
        assertEquals(List.of(1L, 2L, 3L, 4L), numbers("src", "7.7.20, 7.7.250"));
        assertEquals(List.of(2L, 3L), numbers("dst", "31/7/1"));
        assertEquals(List.of(3L, 4L), numbers("type", "READ,response"));
        assertEquals(List.of(3L, 4L), numbers("dir", "out"));
        assertEquals(List.of(1L, 4L), numbers("dpt", "9"));
        assertEquals(List.of(2L, 3L), numbers("dpt", "1.001"));
        assertEquals(List.of(3L, 4L), numbers("from", "-45s"));
        assertEquals(List.of(3L, 4L), numbers("from", "11:00:20"));
        assertEquals(List.of(3L, 4L), numbers("from", "2026-03-01T11:00:20"));
        assertEquals(List.of(3L, 4L), numbers("from", "2026-03-01T10:00:20Z"));
        assertEquals(List.of(1L, 2L), numbers("to", "-50s"));
        assertEquals(List.of(3L), numbers("type", "read", "context", "9999"));
        assertEquals(List.of(2L, 3L, 4L), numbers("type", "read", "context", "10000"));
        assertEquals(List.of(3L, 4L), numbers("limit", "2"));
    }

    @Test
    void eachFailureMessage() {
        assertEquals("not an individual address", failure("src", "nope"));
        assertEquals("not a group address", failure("dst", "31/7/1,x"));
        assertEquals("not one of write, read, response", failure("type", "maybe"));
        assertEquals("not one of in, out", failure("dir", "up"));
        assertEquals("not a data type such as 9 or 9.001", failure("dpt", "nine"));
        assertEquals("not a number from 0 on", failure("context", "-1"));
        assertEquals("not a number from 0 on", failure("limit", "lots"));
        assertEquals("unknown filter", failure("speed", "3"));
    }

    @Test
    void aTimeThatCannotBeReadIsAnIllegalArgumentWithTheConsolesMessage() {
        String expected = assertThrows(DateTimeException.class, () -> LocalTime.parse("nonsense")).getMessage();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> TelegramFilterText.apply(TelegramFilter.builder(), "from", "nonsense", CLOCK, ZONE));
        assertEquals(expected, e.getMessage());
        assertInstanceOf(DateTimeException.class, e.getCause());

        String local = assertThrows(DateTimeException.class, () -> LocalDateTime.parse("2026-13-01T00:00"))
                .getMessage();
        assertEquals(local, failure("to", "2026-13-01T00:00"));
        assertEquals(assertThrows(DateTimeException.class, () -> Instant.parse("soonZ")).getMessage(),
                failure("from", "soonZ"));
    }

    @Test
    void aGroupAddressIsReadAsTheConsoleReadsIt() {
        assertEquals(LIGHT, TelegramFilterText.groupAddress("31/7/1"));
        assertEquals(new GroupAddress(31, 7, 3), TelegramFilterText.groupAddress("31/7/3"));
        assertEquals("not a group address",
                assertThrows(IllegalArgumentException.class, () -> TelegramFilterText.groupAddress("x")).getMessage());
        assertEquals("not a group address",
                assertThrows(IllegalArgumentException.class, () -> TelegramFilterText.groupAddress("32/0/0"))
                        .getMessage());
    }
}
