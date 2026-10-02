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
package org.openhab.binding.caldav.internal.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;

@NonNullByDefault
class ICalendarParserTest {
    @Test
    void parsesDailyRecurrenceAndExdate() {
        String calendar = "BEGIN:VCALENDAR\nBEGIN:VEVENT\nUID:daily\nDTSTART:20260916T080000Z\n"
                + "DTEND:20260916T090000Z\nSUMMARY:Daily\nRRULE:FREQ=DAILY;COUNT=3\n"
                + "EXDATE:20260917T080000Z\nEND:VEVENT\nEND:VCALENDAR\n";

        var events = ICalendarParser.parse(calendar);

        assertEquals(2, events.size());
        assertEquals("daily", events.get(0).uid());
        assertNotNull(events.get(1).start());
        var recurrenceStart = Objects.requireNonNull(events.get(1).start());
        assertEquals("2026-09-18T08:00Z", recurrenceStart.toOffsetDateTime().toString());
    }

    @Test
    void rejectsMissingUid() {
        assertThrows(IllegalArgumentException.class, () -> ICalendarParser
                .parse("BEGIN:VCALENDAR\nBEGIN:VEVENT\nDTSTART:20260916T080000Z\nEND:VEVENT\nEND:VCALENDAR"));
    }

    @Test
    void calculatesInclusiveStartAndExclusiveEnd() {
        CalendarConfiguration configuration = new CalendarConfiguration();
        configuration.rangeStartOffset = -1;
        configuration.rangeEndOffset = 1;
        ZonedDateTime now = ZonedDateTime.of(2026, 9, 16, 13, 0, 0, 0, ZoneId.of("Europe/Berlin"));

        CalendarWindow window = CalendarWindow.from(configuration, now.getZone(), now);

        assertEquals("2026-09-15T00:00+02:00[Europe/Berlin]", window.start().toString());
        assertEquals("2026-09-18T00:00+02:00[Europe/Berlin]", window.end().toString());
    }

    @Test
    void parsesAllDayEventAndUnfoldsEscapedText() {
        String calendar = "BEGIN:VCALENDAR\r\nBEGIN:VEVENT\r\nUID:all-day\r\n"
                + "DTSTART;VALUE=DATE:20260917\r\nDTEND;VALUE=DATE:20260918\r\n"
                + "SUMMARY:Long title\r\n continued\r\nDESCRIPTION:Line one\\nLine two\\, here\r\n"
                + "END:VEVENT\r\nEND:VCALENDAR\r\n";

        var events = ICalendarParser.parse(calendar);

        assertEquals(1, events.size());
        assertTrue(events.get(0).allDay());
        assertNotNull(events.get(0).allDayStart());
        var allDayStart = Objects.requireNonNull(events.get(0).allDayStart());
        assertEquals("2026-09-17", allDayStart.toString());
        assertEquals("Long titlecontinued", events.get(0).title());
        assertEquals("Line one\nLine two, here", events.get(0).description());
    }

    @Test
    void expandsRdateIncludingStart() {
        String calendar = "BEGIN:VCALENDAR\nBEGIN:VEVENT\nUID:rdate\n"
                + "DTSTART:20260916T080000Z\nDTEND:20260916T090000Z\nRDATE:20260917T080000Z\n"
                + "END:VEVENT\nEND:VCALENDAR\n";

        var events = ICalendarParser.parse(calendar);

        assertEquals(2, events.size());
        assertNotNull(events.get(1).start());
        var rdateStart = Objects.requireNonNull(events.get(1).start());
        assertEquals("2026-09-17T08:00Z", rdateStart.toOffsetDateTime().toString());
    }
}
