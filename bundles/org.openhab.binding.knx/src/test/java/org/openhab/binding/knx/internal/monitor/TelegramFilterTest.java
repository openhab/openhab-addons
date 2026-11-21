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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the one place where the monitor's filters are applied.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramFilterTest {
    private static final Instant START = Instant.parse("2026-03-01T10:00:00Z");
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 20);
    private static final IndividualAddress SWITCH = new IndividualAddress(1, 1, 21);
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 1, 250);
    private static final GroupAddress TEMPERATURE = new GroupAddress(3, 1, 10);
    private static final GroupAddress LIGHT = new GroupAddress(3, 1, 11);
    private static final GroupAddress BLIND = new GroupAddress(3, 1, 12);

    private final List<TelegramRecord> rows = new ArrayList<>();

    /** Adds a row {@code seconds} after the start. */
    private void row(int seconds, IndividualAddress source, GroupAddress destination, TelegramType type,
            Direction direction, @Nullable String dataType) {
        rows.add(new TelegramRecord(rows.size() + 1, START.plusSeconds(seconds), source, destination, type, direction,
                new byte[] { 1 }, SecurityState.PLAIN, null, direction == Direction.OUT ? SendOutcome.SENT : null,
                dataType, null, null, null, null, false));
    }

    private List<Long> numbers(TelegramFilter filter) {
        return filter.apply(rows).stream().map(TelegramRecord::number).toList();
    }

    private void someRows() {
        row(0, SENSOR, TEMPERATURE, TelegramType.WRITE, Direction.IN, "9.001"); // 1
        row(10, SWITCH, LIGHT, TelegramType.WRITE, Direction.IN, "1.001"); // 2
        row(20, OPENHAB, LIGHT, TelegramType.READ, Direction.OUT, "1.001"); // 3
        row(21, SWITCH, LIGHT, TelegramType.RESPONSE, Direction.IN, "1.001"); // 4
        row(30, OPENHAB, BLIND, TelegramType.WRITE, Direction.OUT, "5.001"); // 5
        row(40, SENSOR, TEMPERATURE, TelegramType.WRITE, Direction.IN, null); // 6
    }

    @Test
    void withoutFiltersEverythingComesOldestFirst() {
        someRows();

        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), numbers(TelegramFilter.ALL));
    }

    @Test
    void eachFilterAlone() {
        someRows();

        assertEquals(List.of(1L, 6L), numbers(TelegramFilter.builder().source(SENSOR).build()));
        assertEquals(List.of(2L, 3L, 4L), numbers(TelegramFilter.builder().destination(LIGHT).build()));
        assertEquals(List.of(3L), numbers(TelegramFilter.builder().type(TelegramType.READ).build()));
        assertEquals(List.of(3L, 5L), numbers(TelegramFilter.builder().direction(Direction.OUT).build()));
        assertEquals(List.of(2L, 3L, 4L), numbers(TelegramFilter.builder().dataType("1.001").build()));
        assertEquals(List.of(3L, 4L, 5L),
                numbers(TelegramFilter.builder().from(START.plusSeconds(20)).to(START.plusSeconds(30)).build()));
    }

    @Test
    void aMainTypeMatchesItsSubTypes() {
        someRows();

        assertEquals(List.of(5L), numbers(TelegramFilter.builder().dataType("5").build()));
        assertEquals(List.of(), numbers(TelegramFilter.builder().dataType("5.010").build()));
        assertEquals(List.of(), numbers(TelegramFilter.builder().dataType("50").build()));
    }

    @Test
    void valuesInsideOneFilterAreOr() {
        someRows();

        assertEquals(List.of(1L, 2L, 4L, 6L), numbers(TelegramFilter.builder().source(SENSOR, SWITCH).build()));
        assertEquals(List.of(1L, 2L, 3L), numbers(TelegramFilter.builder().dataType("9.001", "1.001")
                .type(TelegramType.READ, TelegramType.WRITE).direction(Direction.OUT, Direction.IN).build()));
    }

    @Test
    void filtersAreAnd() {
        someRows();

        assertEquals(List.of(2L),
                numbers(TelegramFilter.builder().destination(LIGHT).type(TelegramType.WRITE).build()));
        assertEquals(List.of(), numbers(TelegramFilter.builder().source(OPENHAB).direction(Direction.IN).build()));
    }

    @Test
    void theContextSpanAddsNeighboursAfterMatching() {
        someRows();

        assertEquals(List.of(3L, 4L),
                numbers(TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(1)).build()));
        assertEquals(List.of(2L, 3L, 4L, 5L),
                numbers(TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(10)).build()));
    }

    @Test
    void theContextSpanIsAppliedToEveryMatch() {
        someRows();

        assertEquals(List.of(1L, 4L, 5L), numbers(TelegramFilter.builder().dataType("9.001", "5.001")
                .source(SENSOR, OPENHAB).type(TelegramType.WRITE).context(Duration.ofSeconds(9)).build()));
    }

    @Test
    void theLimitKeepsTheNewest() {
        someRows();

        assertEquals(List.of(5L, 6L), numbers(TelegramFilter.builder().limit(2).build()));
        assertEquals(List.of(3L, 4L), numbers(TelegramFilter.builder().destination(LIGHT).limit(2).build()));
        assertEquals(List.of(), numbers(TelegramFilter.builder().limit(0).build()));
    }

    @Test
    void theLimitComesAfterTheContextSpan() {
        someRows();

        assertEquals(List.of(4L), numbers(
                TelegramFilter.builder().type(TelegramType.READ).context(Duration.ofSeconds(1)).limit(1).build()));
    }

    @Test
    void aRowWithoutDataTypeDoesNotMatchADataTypeFilter() {
        someRows();

        assertFalse(numbers(TelegramFilter.builder().dataType("9").build()).contains(6L));
    }

    @Test
    void theMonitorQueriesThroughTheFilter() {
        TelegramMonitor monitor = new TelegramMonitor();
        monitor.useChannelTypes(address -> TEMPERATURE.equals(address)
                ? new DataType("9.001", org.openhab.core.library.types.DecimalType.class)
                : null);
        monitor.received(SENSOR, TEMPERATURE, TelegramType.WRITE, new byte[] { 0x0c, 0x33 }, SecurityState.PLAIN);
        monitor.received(SWITCH, LIGHT, TelegramType.WRITE, new byte[] { 1 }, SecurityState.PLAIN);

        List<TelegramRecord> found = monitor.query(TelegramFilter.builder().dataType("9").build());

        assertEquals(1, found.size());
        assertEquals("21.5", found.get(0).value());
    }
}
