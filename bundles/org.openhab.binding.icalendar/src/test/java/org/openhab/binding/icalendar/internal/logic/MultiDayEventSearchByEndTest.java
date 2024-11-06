/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal.logic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of time-based filtering when using {@link EventTimeFilter#searchByEnd()} in {@link
 * BiweeklyPresentableCalendar#getFilteredEventsBetween(Instant, Instant, EventTimeFilter, EventTextFilter, int)}
 * with multi-day events.
 *
 * @author Christian Heinemann - Initial contribution
 */
@NonNullByDefault
public class MultiDayEventSearchByEndTest {

    private @NonNullByDefault({}) AbstractPresentableCalendar calendar;
    private final EventTimeFilter eventTimeFilter = EventTimeFilter.searchByEnd();

    @BeforeEach
    public void setUp() throws IOException, CalendarException {
        calendar = new BiweeklyPresentableCalendar(new FileInputStream("src/test/resources/test-multiday.ics"));
    }

    @Test
    public void eventWithTime() {
        Event expectedFilteredEvent = new Event("Multi-day test event with time", Instant.parse("2023-12-05T09:00:00Z"),
                Instant.parse("2023-12-07T15:00:00Z"), "");

        assertThat("Day before event starts",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-04T00:00:00Z"),
                        Instant.parse("2023-12-05T00:00:00Z"), eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 1 when event starts",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-05T00:00:00Z"),
                        Instant.parse("2023-12-06T00:00:00Z"), eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 2 when event is still active",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-06T00:00:00Z"),
                        Instant.parse("2023-12-07T00:00:00Z"), eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 3 when event ends",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-07T00:00:00Z"),
                        Instant.parse("2023-12-08T00:00:00Z"), eventTimeFilter, null, 1),
                contains(expectedFilteredEvent));

        assertThat("Hour when event ends",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-07T14:00:00Z"),
                        Instant.parse("2023-12-07T15:00:00Z"), eventTimeFilter, null, 1),
                contains(expectedFilteredEvent));

        assertThat("Hour after event ends",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-07T15:00:00Z"),
                        Instant.parse("2023-12-07T16:00:00Z"), eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day after event ends",
                calendar.getFilteredEventsBetween(Instant.parse("2023-12-08T00:00:00Z"),
                        Instant.parse("2023-12-09T00:00:00Z"), eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));
    }

    @Test
    public void eventWithoutTime() {
        Event expectedFilteredEvent = new Event("Multi-day test event without time", localDateAsInstant("2023-12-12"),
                localDateAsInstant("2023-12-15"), "");

        assertThat("Day before event starts", //
                calendar.getFilteredEventsBetween(localDateAsInstant("2023-12-11"), localDateAsInstant("2023-12-12"),
                        eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 1 when event starts", //
                calendar.getFilteredEventsBetween(localDateAsInstant("2023-12-12"), localDateAsInstant("2023-12-13"),
                        eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 2 when event is still active", //
                calendar.getFilteredEventsBetween(localDateAsInstant("2023-12-13"), localDateAsInstant("2023-12-14"),
                        eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));

        assertThat("Day 3 when event ends", //
                calendar.getFilteredEventsBetween(localDateAsInstant("2023-12-14"), localDateAsInstant("2023-12-15"),
                        eventTimeFilter, null, 1),
                contains(expectedFilteredEvent));

        assertThat("Day after event ends", //
                calendar.getFilteredEventsBetween(localDateAsInstant("2023-12-15"), localDateAsInstant("2023-12-16"),
                        eventTimeFilter, null, 1),
                is(emptyCollectionOf(Event.class)));
    }

    private Instant localDateAsInstant(CharSequence text) {
        return LocalDate.parse(text).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
