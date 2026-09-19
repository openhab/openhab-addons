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

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

/**
 * Regression coverage for recurrence semantics and time zones.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
@Timeout(10)
class CalendarRecurrenceTest {
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private static final CalendarWindow WINDOW = new CalendarWindow(LocalDate.of(2026, 1, 1).atStartOfDay(BERLIN),
            LocalDate.of(2027, 1, 1).atStartOfDay(BERLIN));

    private List<CalendarEvent> parse(String events) {
        return ICalendarParser.parse("BEGIN:VCALENDAR\nVERSION:2.0\n" + events + "END:VCALENDAR\n", WINDOW, BERLIN,
                false);
    }

    private String event(String properties) {
        return "BEGIN:VEVENT\nUID:one\n" + properties + "END:VEVENT\n";
    }

    @Test
    void monthlyByDayAndUntil() {
        var events = parse(event(
                "DTSTART:20260105T090000Z\nDURATION:PT1H\nRRULE:FREQ=MONTHLY;BYDAY=1MO;UNTIL=20260331T235959Z\n"));
        assertEquals(List.of("2026-01-05", "2026-02-02", "2026-03-02"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toLocalDate().toString()).toList());
    }

    @Test
    void yearlyRecurrence() {
        assertEquals(1, parse(event("DTSTART:20200105T090000Z\nDURATION:PT1H\nRRULE:FREQ=YEARLY\n")).size());
    }

    @Test
    void timezoneRecurrenceKeepsWallClockAcrossDst() {
        var events = parse(event(
                "DTSTART;TZID=Europe/Berlin:20260328T090000\nDTEND;TZID=Europe/Berlin:20260328T100000\nRRULE:FREQ=DAILY;COUNT=3\n"));
        assertEquals(3, events.size());
        assertEquals(List.of(9, 9, 9), events.stream().map(e -> Objects.requireNonNull(e.start()).getHour()).toList());
        assertEquals(3600, Objects.requireNonNull(events.getFirst().start()).getOffset().getTotalSeconds());
        assertEquals(7200, Objects.requireNonNull(events.getLast().start()).getOffset().getTotalSeconds());
    }

    @Test
    void floatingTimeUsesOpenhabZone() {
        var event = parse(event("DTSTART:20260918T090000\nDTEND:20260918T100000\n")).getFirst();
        assertEquals("2026-09-18T07:00:00Z", Objects.requireNonNull(event.start()).toInstant().toString());
    }

    @Test
    void allDayRecurrenceHasExclusiveCalendarEnd() {
        var events = parse(event("DTSTART;VALUE=DATE:20260328\nDTEND;VALUE=DATE:20260330\nRRULE:FREQ=DAILY;COUNT=3\n"));
        assertEquals(3, events.size());
        assertTrue(events.stream().allMatch(CalendarEvent::allDay));
        assertEquals(LocalDate.of(2026, 3, 31), events.get(1).allDayEnd());
    }

    @Test
    void recurrenceDatesIncludeBaseAndNormalizeExclusions() {
        var events = parse(event(
                "DTSTART:20260918T090000Z\nDURATION:PT1H\nRDATE:20260919T090000Z,20260920T090000Z\nEXDATE:20260919T090000Z\n"));
        assertEquals(2, events.size());
        assertEquals(18, Objects.requireNonNull(events.getFirst().start()).getDayOfMonth());
        assertEquals(20, Objects.requireNonNull(events.getLast().start()).getDayOfMonth());
    }

    @Test
    void movedAndCancelledExceptionsReplaceOriginals() {
        var events = parse(event("DTSTART:20260918T090000Z\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=3\n")
                + event("RECURRENCE-ID:20260919T090000Z\nDTSTART:20260919T120000Z\nDURATION:PT1H\n")
                + event("RECURRENCE-ID:20260920T090000Z\nSTATUS:CANCELLED\n"));
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> "2026-09-19T09:00:00Z".equals(e.recurrenceId())
                && Objects.requireNonNull(e.start()).getHour() == 12));
        assertTrue(events.stream().noneMatch(e -> Objects.requireNonNull(e.start()).getDayOfMonth() == 20));
    }

    @Test
    void defaultDurationsAndPointEvents() {
        var events = parse(event("DTSTART;VALUE=DATE:20260918\n"));
        assertEquals(LocalDate.of(2026, 9, 19), events.getFirst().allDayEnd());
        var point = parse(event("DTSTART:20260918T090000Z\n")).getFirst();
        assertEquals(point.start(), point.end());
    }

    @Test
    void rejectsOversizedResources() {
        assertThrows(CalendarLimitException.class,
                () -> ICalendarParser.parse("x".repeat(ICalendarParser.MAX_RESOURCE_SIZE + 1), WINDOW, BERLIN, false));
    }

    @Test
    void includesCancelledExceptionWithoutStartWhenRequested() {
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART:20260918T090000Z\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=2\n")
                + event("RECURRENCE-ID:20260919T090000Z\nSTATUS:CANCELLED\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, BERLIN, true);
        assertEquals(2, events.size());
        var cancelled = events.stream().filter(e -> "CANCELLED".equals(e.status())).findFirst().orElseThrow();
        assertEquals("2026-09-19T09:00:00Z", cancelled.recurrenceId());
        assertEquals(3600,
                java.time.Duration
                        .between(Objects.requireNonNull(cancelled.start()), Objects.requireNonNull(cancelled.end()))
                        .toSeconds());
    }

    @Test
    void floatingTimeDoesNotDependOnJvmZone() {
        ZoneId zone = ZoneId.of("Pacific/Honolulu");
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART:20260918T090000\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=2\nEXDATE:20260919T090000\n")
                + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, zone, false);
        assertEquals(1, events.size());
        assertEquals("2026-09-18T19:00:00Z", Objects.requireNonNull(events.getFirst().start()).toInstant().toString());
    }

    @Test
    void rejectsInvalidRecurrenceAndUnknownTimezone() {
        assertThrows(IllegalArgumentException.class,
                () -> parse(event("DTSTART:20260918T090000Z\nRRULE:FREQ=INVALID\n")));
        assertThrows(IllegalArgumentException.class,
                () -> parse(event("DTSTART;TZID=Invalid/Unknown:20260918T090000\n")));
    }

    @Test
    void nominalDurationDayDiffersFromTwentyFourHoursAcrossDst() {
        var day = parse(event("DTSTART;TZID=Europe/Berlin:20260328T090000\nDURATION:P1D\n")).getFirst();
        var hours = parse(event("DTSTART;TZID=Europe/Berlin:20260328T090000\nDURATION:PT24H\n")).getFirst();
        assertEquals(9, Objects.requireNonNull(day.end()).getHour());
        assertEquals(10, Objects.requireNonNull(hours.end()).getHour());
        assertEquals(23, java.time.Duration
                .between(Objects.requireNonNull(day.start()), Objects.requireNonNull(day.end())).toHours());
    }

    @Test
    void customVtimezoneIsUsedInsteadOfJvmDefault() {
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VTIMEZONE\nTZID:Custom/Fixed\n"
                + "BEGIN:STANDARD\nDTSTART:19700101T000000\nTZOFFSETFROM:+0230\nTZOFFSETTO:+0230\nEND:STANDARD\nEND:VTIMEZONE\n"
                + event("DTSTART;TZID=Custom/Fixed:20260918T090000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, BERLIN, false);
        assertEquals("2026-09-18T06:30:00Z", Objects.requireNonNull(events.getFirst().start()).toInstant().toString());
    }

    @Test
    void allDayRecurrencePreservesDatesInAnotherOpenhabZone() {
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART;VALUE=DATE:20260918\nRRULE:FREQ=DAILY;COUNT=2\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, ZoneId.of("Pacific/Honolulu"), false);
        assertEquals(List.of(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 19)),
                events.stream().map(CalendarEvent::allDayStart).toList());
    }
}
