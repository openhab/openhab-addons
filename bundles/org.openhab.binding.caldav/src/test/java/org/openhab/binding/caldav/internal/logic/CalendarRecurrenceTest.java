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

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.model.CalendarEvent;
import org.openhab.binding.caldav.internal.sync.CalendarSynchronizer;

/**
 * Regression coverage for recurrence semantics and time zones.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Recurrence and custom time-zone regression coverage
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
    void knownTimezoneWorksWithoutVtimezone() {
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART;TZID=Europe/Bratislava:20260919T030000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, ZoneId.of("UTC"), false);
        assertEquals(1, events.size());
        assertEquals("2026-09-19T01:00:00Z", Objects.requireNonNull(events.getFirst().start()).toInstant().toString());
        assertEquals("2026-09-19T02:00:00Z", Objects.requireNonNull(events.getFirst().end()).toInstant().toString());
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

    @Test
    void monthlyRecurrenceSkipsInvalidMonthDaysWithoutConsumingCount() {
        var events = parse(event("DTSTART:20260131T090000Z\nDURATION:PT1H\nRRULE:FREQ=MONTHLY;COUNT=4\n"));
        assertEquals(List.of("2026-01-31", "2026-03-31", "2026-05-31", "2026-07-31"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toLocalDate().toString()).toList());
    }

    @Test
    void yearlyLeapDayRecurrenceSkipsNonLeapYears() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2024, 1, 1).atStartOfDay(BERLIN),
                LocalDate.of(2033, 1, 1).atStartOfDay(BERLIN));
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART:20240229T090000Z\nDURATION:PT1H\nRRULE:FREQ=YEARLY;COUNT=3\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, window, BERLIN, false);
        assertEquals(List.of("2024-02-29", "2028-02-29", "2032-02-29"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toLocalDate().toString()).toList());
    }

    @Test
    void bySetPosSelectsLastWeekdayOfEveryMonth() {
        var events = parse(event("DTSTART:20260130T090000Z\nDURATION:PT1H\n"
                + "RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-1;COUNT=3\n"));
        assertEquals(List.of("2026-01-30", "2026-02-27", "2026-03-31"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toLocalDate().toString()).toList());
    }

    @Test
    void weekStartChangesBiweeklyRecurrenceSet() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(1997, 8, 1).atStartOfDay(BERLIN),
                LocalDate.of(1997, 9, 1).atStartOfDay(BERLIN));
        for (String weekStart : List.of("MO", "SU")) {
            String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                    + event("DTSTART:19970805T090000Z\nDURATION:PT1H\n"
                            + "RRULE:FREQ=WEEKLY;INTERVAL=2;COUNT=4;BYDAY=TU,SU;WKST=" + weekStart + "\n")
                    + "END:VCALENDAR\n";
            var events = ICalendarParser.parse(data, window, BERLIN, false);
            assertEquals("MO".equals(weekStart) ? List.of(5, 10, 19, 24) : List.of(5, 17, 19, 31),
                    events.stream().map(e -> Objects.requireNonNull(e.start()).getDayOfMonth()).toList());
        }
    }

    @Test
    void ruleAndExplicitDatesAreDeduplicatedBeforeExclusion() {
        var events = parse(event("DTSTART:20260918T090000Z\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=2\n"
                + "RDATE:20260918T090000Z,20260919T090000Z,20260920T090000Z\nEXDATE:20260919T090000Z\n"));
        assertEquals(List.of(18, 20),
                events.stream().map(e -> Objects.requireNonNull(e.start()).getDayOfMonth()).toList());
        assertEquals(2, events.stream().map(CalendarEvent::instanceId).distinct().count());
    }

    @Test
    void explicitGapAndOverlapTimesUseRfc5545Offsets() {
        var gap = parse(event("DTSTART;TZID=Europe/Berlin:20260329T023000\nDURATION:PT1H\n")).getFirst();
        var overlap = parse(event("DTSTART;TZID=Europe/Berlin:20261025T023000\nDURATION:PT1H\n")).getFirst();
        assertEquals("2026-03-29T01:30:00Z", Objects.requireNonNull(gap.start()).toInstant().toString());
        assertEquals("2026-10-25T00:30:00Z", Objects.requireNonNull(overlap.start()).toInstant().toString());
    }

    @Test
    void dailyRecurrenceSkipsNonexistentLocalTimeWithoutConsumingCount() {
        var events = parse(
                event("DTSTART;TZID=Europe/Berlin:20260328T023000\nDURATION:PT1H\n" + "RRULE:FREQ=DAILY;COUNT=3\n"));
        assertEquals(List.of("2026-03-28T01:30:00Z", "2026-03-30T00:30:00Z", "2026-03-31T00:30:00Z"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toInstant().toString()).toList());
    }

    @Test
    void expansionAndUtf8ResourceBytesAreBounded() {
        assertThrows(CalendarLimitException.class, () -> parse(event(
                "DTSTART:20260101T000000Z\nRRULE:FREQ=SECONDLY;COUNT=" + (ICalendarParser.MAX_INSTANCES + 1) + "\n")));
        String properties = "DTSTART:20260918T090000Z\nSUMMARY:" + "界".repeat(ICalendarParser.MAX_RESOURCE_SIZE / 2)
                + "\n";
        assertThrows(CalendarLimitException.class, () -> parse(event(properties)));
    }

    @Test
    void unsupportedRangeAndPeriodDatesRejectEntireResource() {
        String master = event("DTSTART:20260918T090000Z\nRRULE:FREQ=DAILY;COUNT=3\n");
        assertThrows(IllegalArgumentException.class, () -> parse(
                master + event("RECURRENCE-ID;RANGE=THISANDFUTURE:20260919T090000Z\nDTSTART:20260919T120000Z\n")));
        assertThrows(IllegalArgumentException.class,
                () -> parse(event("DTSTART:20260918T090000Z\nRDATE;VALUE=PERIOD:20260919T090000Z/PT1H\n")));
    }

    @Test
    void oldDenseCountSeriesInUtcCanAdvanceToNarrowHorizon() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2026, 1, 2).atStartOfDay(ZoneId.of("UTC")),
                LocalDate.of(2026, 1, 2).atStartOfDay(ZoneId.of("UTC")).plusMinutes(2));
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART:20260101T000000Z\nRRULE:FREQ=SECONDLY;COUNT=90000\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, window, ZoneId.of("UTC"), false);
        assertEquals(120, events.size());
        assertEquals("2026-01-02T00:00:00Z", Objects.requireNonNull(events.getFirst().start()).toInstant().toString());
        assertEquals("2026-01-02T00:01:59Z", Objects.requireNonNull(events.getLast().start()).toInstant().toString());
    }

    @Test
    void timezoneExceptionMatchesFirstOverlapOccurrence() {
        var events = parse(
                event("DTSTART;TZID=Europe/Berlin:20261024T023000\nDURATION:PT1H\n" + "RRULE:FREQ=DAILY;COUNT=2\n")
                        + event("RECURRENCE-ID;TZID=Europe/Berlin:20261025T023000\n"
                                + "DTSTART;TZID=Europe/Berlin:20261025T033000\nDURATION:PT1H\n"));
        assertEquals(2, events.size());
        var moved = events.stream().filter(e -> "2026-10-25T00:30:00Z".equals(e.recurrenceId())).findFirst()
                .orElseThrow();
        assertEquals("2026-10-25T02:30:00Z", Objects.requireNonNull(moved.start()).toInstant().toString());
    }

    @Test
    void customVtimezoneRecurrenceSkipsGapAndUsesFirstOverlap() {
        String timezone = "BEGIN:VTIMEZONE\nTZID:Custom/Berlin\nBEGIN:STANDARD\nDTSTART:19701025T030000\n"
                + "TZOFFSETFROM:+0200\nTZOFFSETTO:+0100\nRRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\nEND:STANDARD\n"
                + "BEGIN:DAYLIGHT\nDTSTART:19700329T020000\nTZOFFSETFROM:+0100\nTZOFFSETTO:+0200\n"
                + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\nEND:DAYLIGHT\nEND:VTIMEZONE\n";
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Berlin:20260328T023000\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=3\n")
                + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, WINDOW, BERLIN, false);
        assertEquals(List.of("2026-03-28T01:30:00Z", "2026-03-30T00:30:00Z", "2026-03-31T00:30:00Z"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toInstant().toString()).toList());
        String overlap = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Berlin:20261025T023000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
        var event = ICalendarParser.parse(overlap, WINDOW, BERLIN, false).getFirst();
        assertEquals("2026-10-25T00:30:00Z", Objects.requireNonNull(event.start()).toInstant().toString());
    }

    @Test
    void customAfternoonTransitionAndNominalDurationUseUtcBoundaries() {
        String timezone = "BEGIN:VTIMEZONE\nTZID:Custom/Afternoon\nBEGIN:STANDARD\nDTSTART:19700101T000000\n"
                + "TZOFFSETFROM:+0230\nTZOFFSETTO:+0230\nEND:STANDARD\nBEGIN:STANDARD\n"
                + "DTSTART:20260918T150000\nTZOFFSETFROM:+0230\nTZOFFSETTO:+0100\nEND:STANDARD\nEND:VTIMEZONE\n";
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Afternoon:20260918T160000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
        var afternoon = ICalendarParser.parse(data, WINDOW, BERLIN, false).getFirst();
        assertEquals("2026-09-18T15:00:00Z", Objects.requireNonNull(afternoon.start()).toInstant().toString());
        String nominal = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Afternoon:20260917T160000\nDURATION:P1D\n") + "END:VCALENDAR\n";
        var day = ICalendarParser.parse(nominal, WINDOW, BERLIN, false).getFirst();
        assertEquals("2026-09-17T13:30:00Z", Objects.requireNonNull(day.start()).toInstant().toString());
        assertEquals("2026-09-18T15:00:00Z", Objects.requireNonNull(day.end()).toInstant().toString());
        assertEquals(25 * 60 + 30, java.time.Duration
                .between(Objects.requireNonNull(day.start()), Objects.requireNonNull(day.end())).toMinutes());
    }

    @Test
    void explicitCustomGapUsesTheImmediatelyPrecedingOffset() {
        String timezone = "BEGIN:VTIMEZONE\nTZID:Custom/TwoChanges\nBEGIN:STANDARD\nDTSTART:19700101T000000\n"
                + "TZOFFSETFROM:+0000\nTZOFFSETTO:+0000\nEND:STANDARD\nBEGIN:DAYLIGHT\n"
                + "DTSTART:20260918T010000\nTZOFFSETFROM:+0000\nTZOFFSETTO:+0100\nEND:DAYLIGHT\n"
                + "BEGIN:DAYLIGHT\nDTSTART:20260918T030000\nTZOFFSETFROM:+0100\nTZOFFSETTO:+0200\n"
                + "END:DAYLIGHT\nEND:VTIMEZONE\n";
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/TwoChanges:20260918T033000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
        var event = ICalendarParser.parse(data, WINDOW, BERLIN, false).getFirst();
        assertEquals("2026-09-18T02:30:00Z", Objects.requireNonNull(event.start()).toInstant().toString());
    }

    @Test
    void customObservanceUntilIsComparedInUtc() {
        String timezone = customLimitedTimezone("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU;UNTIL=20260329T010000Z\n");
        CalendarWindow window = new CalendarWindow(LocalDate.of(2026, 1, 1).atStartOfDay(BERLIN),
                LocalDate.of(2028, 1, 1).atStartOfDay(BERLIN));
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Limited:20260329T040000\nDURATION:PT1H\nRRULE:FREQ=YEARLY;COUNT=2\n")
                + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, window, BERLIN, false);
        assertEquals(List.of("2026-03-29T02:00:00Z", "2027-03-29T03:00:00Z"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toInstant().toString()).toList());
    }

    @Test
    void customObservanceExdateSuppressesOnlyItsTransition() {
        String timezone = customLimitedTimezone("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\nEXDATE:20260329T020000\n");
        CalendarWindow window = new CalendarWindow(LocalDate.of(2026, 1, 1).atStartOfDay(BERLIN),
                LocalDate.of(2028, 1, 1).atStartOfDay(BERLIN));
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n" + timezone
                + event("DTSTART;TZID=Custom/Limited:20260329T040000\nDURATION:PT1H\nRRULE:FREQ=YEARLY;COUNT=2\n")
                + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, window, BERLIN, false);
        assertEquals(List.of("2026-03-29T03:00:00Z", "2027-03-29T02:00:00Z"),
                events.stream().map(e -> Objects.requireNonNull(e.start()).toInstant().toString()).toList());
    }

    private static String customLimitedTimezone(String daylightRecurrence) {
        return "BEGIN:VTIMEZONE\nTZID:Custom/Limited\nBEGIN:STANDARD\nDTSTART:20251026T030000\n"
                + "TZOFFSETFROM:+0200\nTZOFFSETTO:+0100\nRRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\nEND:STANDARD\n"
                + "BEGIN:DAYLIGHT\nDTSTART:20250330T020000\nTZOFFSETFROM:+0100\nTZOFFSETTO:+0200\n" + daylightRecurrence
                + "END:DAYLIGHT\nEND:VTIMEZONE\n";
    }

    @Test
    void leapSecondUsesSecond59ForUtcFloatingAndCustomTimezone() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2016, 12, 30).atStartOfDay(BERLIN),
                LocalDate.of(2017, 1, 3).atStartOfDay(BERLIN));
        for (String start : List.of("DTSTART:20161231T235960Z", "DTSTART:20170101T005960",
                "DTSTART;TZID=Custom/Leap:20170101T005960")) {
            var event = ICalendarParser.parse(leapCalendar(start + "\nDURATION:PT1S\n"), window, BERLIN, false)
                    .getFirst();
            assertEquals("2016-12-31T23:59:59Z", Objects.requireNonNull(event.start()).toInstant().toString(), start);
            assertEquals("2017-01-01T00:00:00Z", Objects.requireNonNull(event.end()).toInstant().toString(), start);
        }
    }

    @Test
    void malformedDateTimeRemainsIsolatedFromLeapSecondAndHealthyResources() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2016, 12, 30).atStartOfDay(BERLIN),
                LocalDate.of(2017, 1, 3).atStartOfDay(BERLIN));
        var resources = Map.of("/leap.ics",
                new CalendarSynchronizer.CachedResource(
                        "leap", leapCalendar("DTSTART;TZID=Custom/Leap:20170101T005960\nDURATION:PT1S\n")),
                "/malformed.ics",
                new CalendarSynchronizer.CachedResource("bad",
                        leapCalendar("DTSTART:20170101T005961\nDURATION:PT1S\n")),
                "/healthy.ics", new CalendarSynchronizer.CachedResource("good",
                        leapCalendar("DTSTART:20170101T020000\nDURATION:PT1S\n").replace("UID:one", "UID:healthy")));
        var result = CalendarSynchronizer.expand(new CalendarSynchronizer.Snapshot("", "", resources), window, BERLIN,
                false);
        assertEquals(1, result.failedResources());
        assertEquals(2, result.events().size());
        assertTrue(result.events().stream().anyMatch(event -> "healthy".equals(event.uid())));
        assertTrue(result.events().stream().anyMatch(event -> "one".equals(event.uid())));
    }

    @Test
    void leapSecondDatesAndUntilUseTheSameNormalizedInstant() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2016, 12, 30).atStartOfDay(BERLIN),
                LocalDate.of(2017, 1, 3).atStartOfDay(BERLIN));
        String data = leapCalendar(
                "DTSTART:20161231T235958Z\nDURATION:PT1S\n" + "RRULE:FREQ=SECONDLY;UNTIL=20161231T235960Z\n");
        var until = ICalendarParser.parse(data, window, BERLIN, false);
        assertEquals(List.of("2016-12-31T23:59:58Z", "2016-12-31T23:59:59Z"),
                until.stream().map(event -> Objects.requireNonNull(event.start()).toInstant().toString()).toList());
        String excluded = leapCalendar(
                "DTSTART:20161231T235958Z\nDURATION:PT1S\nRRULE:FREQ=SECONDLY;COUNT=3\n" + "EXDATE:20161231T235960Z\n");
        var events = ICalendarParser.parse(excluded, window, BERLIN, false);
        assertEquals(List.of("2016-12-31T23:59:58Z", "2017-01-01T00:00:00Z"),
                events.stream().map(event -> Objects.requireNonNull(event.start()).toInstant().toString()).toList());
        String included = leapCalendar("DTSTART:20161231T235958Z\nDURATION:PT1S\nRDATE:20161231T235960Z\n");
        assertEquals(List.of("2016-12-31T23:59:58Z", "2016-12-31T23:59:59Z"),
                ICalendarParser.parse(included, window, BERLIN, false).stream()
                        .map(event -> Objects.requireNonNull(event.start()).toInstant().toString()).toList());
    }

    private String leapCalendar(String properties) {
        return "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VTIMEZONE\nTZID:Custom/Leap\nBEGIN:STANDARD\n"
                + "DTSTART:19700101T000000\nTZOFFSETFROM:+0100\nTZOFFSETTO:+0100\nEND:STANDARD\nEND:VTIMEZONE\n"
                + event(properties) + "END:VCALENDAR\n";
    }

    @Test
    void historicalFloatingTimesKeepZoneRulesOffsetAndDuration() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(1890, 1, 1).atStartOfDay(BERLIN),
                LocalDate.of(1890, 1, 5).atStartOfDay(BERLIN));
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\n"
                + event("DTSTART:18900102T120000\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=2\n") + "END:VCALENDAR\n";
        var events = ICalendarParser.parse(data, window, BERLIN, false);
        assertEquals(2, events.size());
        assertEquals("1890-01-02T12:00+00:53:28", Objects.requireNonNull(events.getFirst().start()).toString());
        assertEquals("1890-01-02T13:00+00:53:28", Objects.requireNonNull(events.getFirst().end()).toString());
        assertEquals("1890-01-03T12:00+00:53:28", Objects.requireNonNull(events.getLast().start()).toString());
        assertEquals("1890-01-02T11:06:32Z", Objects.requireNonNull(events.getFirst().start()).toInstant().toString());
    }

    @Test
    void customTimezonePreservesSecondsInPositiveAndNegativeOffsets() {
        CalendarWindow window = new CalendarWindow(LocalDate.of(2026, 1, 1).atStartOfDay(BERLIN),
                LocalDate.of(2026, 1, 4).atStartOfDay(BERLIN));
        for (String offset : List.of("+003045", "-003045")) {
            String data = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VTIMEZONE\nTZID:Custom/Seconds\nBEGIN:STANDARD\n"
                    + "DTSTART:19700101T000000\nTZOFFSETFROM:" + offset + "\nTZOFFSETTO:" + offset
                    + "\nEND:STANDARD\nEND:VTIMEZONE\n"
                    + event("DTSTART;TZID=Custom/Seconds:20260102T120000\nDURATION:PT1H\n") + "END:VCALENDAR\n";
            var parsed = ICalendarParser.parse(data, window, BERLIN, false).getFirst();
            assertEquals(offset.startsWith("+") ? "2026-01-02T12:00+00:30:45" : "2026-01-02T12:00-00:30:45",
                    Objects.requireNonNull(parsed.start()).toString());
            assertEquals(Duration.ofHours(1),
                    Duration.between(Objects.requireNonNull(parsed.start()), Objects.requireNonNull(parsed.end())));
        }
    }
}
