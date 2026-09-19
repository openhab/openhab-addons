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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneInfo;
import biweekly.io.text.ICalReader;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.property.ExceptionDates;
import biweekly.property.RecurrenceDates;
import biweekly.util.ICalDate;

/**
 * Bounded RFC 5545 expansion. Legacy date types remain at the library boundary.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Recurrence, exceptions and time-zone processing
 */
@NonNullByDefault
public final class ICalendarParser {
    public static final int MAX_INSTANCES = 50000;
    public static final int MAX_RESOURCE_SIZE = 1024 * 1024;

    private ICalendarParser() {
    }

    public static List<CalendarEvent> parse(String content) {
        ZoneId zone = ZoneId.systemDefault();
        return parse(content, new CalendarWindow(LocalDate.of(1900, 1, 1).atStartOfDay(zone),
                LocalDate.of(2200, 1, 1).atStartOfDay(zone)), zone, true);
    }

    public static List<CalendarEvent> parse(String content, CalendarWindow window, ZoneId zone,
            boolean includeCancelled) {
        if (content.getBytes(StandardCharsets.UTF_8).length > MAX_RESOURCE_SIZE) {
            throw new CalendarLimitException("Calendar resource exceeds limit");
        }
        List<CalendarEvent> result = new ArrayList<>();
        try (ICalReader reader = new ICalReader(content)) {
            ICalendar calendar;
            boolean found = false;
            while ((calendar = reader.readNext()) != null) {
                found = true;
                Set<String> critical = Set.of("DTSTART", "DTEND", "DURATION", "RRULE", "RDATE", "EXDATE",
                        "RECURRENCE-ID");
                if (reader.getWarnings().stream()
                        .anyMatch(w -> critical.contains(Objects.toString(w.getPropertyName(), "")))) {
                    throw new IllegalArgumentException("Invalid calendar date or recurrence property");
                }
                expand(calendar, window, zone, includeCancelled, result);
            }
            if (!found) {
                throw new IllegalArgumentException("No iCalendar data");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid iCalendar resource", e);
        }
        return List.copyOf(result);
    }

    private static void expand(ICalendar calendar, CalendarWindow window, ZoneId zone, boolean includeCancelled,
            List<CalendarEvent> result) {
        TimezoneInfo info = calendar.getTimezoneInfo();
        Map<String, VEvent> overrides = new HashMap<>();
        for (VEvent event : calendar.getEvents()) {
            normalizeFloating(event, info, zone);
            var recurrence = event.getRecurrenceId();
            if (recurrence != null) {
                if (recurrence.getRange() != null) {
                    throw new IllegalArgumentException("Recurrence range is not supported");
                }
                overrides.put(uid(event) + "|" + identity(recurrence.getValue()), event);
            }
        }
        Set<String> emitted = new HashSet<>();
        for (VEvent event : calendar.getEvents()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CalendarLimitException("Calendar expansion interrupted");
            }
            if (event.getRecurrenceId() != null) {
                continue;
            }
            String uid = uid(event);
            var dateStart = event.getDateStart();
            if (dateStart == null) {
                throw new IllegalArgumentException("Event has no start date");
            }
            ICalDate first = dateStart.getValue();
            TimeZone tz = timezone(info, dateStart, zone);
            boolean allDay = !first.hasTime();
            Duration length = length(event);
            long days = allDay ? allDayLength(event) : 0;
            // biweekly uses the JVM zone for date-only values; convert them back to dates immediately.
            var iterator = event.getDateIterator(tz);
            Instant lower = window.start().toInstant().minus(length).minus(2, ChronoUnit.DAYS);
            iterator.advanceTo(Date.from(lower));
            int iterations = 0;
            while (iterator.hasNext()) {
                if (++iterations > MAX_INSTANCES || Thread.currentThread().isInterrupted()) {
                    throw new CalendarLimitException("Calendar expansion exceeds limit");
                }
                Date occurrence = iterator.next();
                LocalDate date = occurrence.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                ZonedDateTime start = allDay ? date.atStartOfDay(zone) : zoned(occurrence.toInstant(), tz);
                if (!start.isBefore(window.end())) {
                    break;
                }
                String recurrence = allDay ? date.toString() : occurrence.toInstant().toString();
                String key = uid + "|" + recurrence;
                VEvent replacement = overrides.get(key);
                if (replacement != null) {
                    continue;
                }
                ZonedDateTime end = allDay ? date.plusDays(days).atStartOfDay(zone) : end(event, start, tz);
                boolean recurring = event.getRecurrenceRule() != null || !event.getRecurrenceDates().isEmpty();
                CalendarEvent instance = instance(event, recurring ? recurrence : null, start, end, allDay);
                add(result, emitted, instance, window, zone, includeCancelled);
            }
        }
        // Detached/moved exceptions are selected by their actual dates, even when their original start is outside the
        // window.
        for (VEvent event : overrides.values()) {
            var startProperty = event.getDateStart();
            if (startProperty == null) {
                if (event.getStatus() != null && event.getStatus().isCancelled()) {
                    if (includeCancelled) {
                        var id = Objects.requireNonNull(event.getRecurrenceId());
                        ICalDate date = id.getValue();
                        VEvent master = calendar.getEvents().stream()
                                .filter(e -> e.getRecurrenceId() == null && uid(e).equals(uid(event))).findFirst()
                                .orElse(null);
                        boolean allDay = !date.hasTime();
                        ZonedDateTime start = allDay ? localDate(date).atStartOfDay(zone)
                                : zoned(date.toInstant(), timezone(info, id, zone));
                        ZonedDateTime end = allDay ? start.plusDays(master == null ? 1 : allDayLength(master))
                                : start.plus(master == null ? Duration.ZERO : length(master));
                        add(result, emitted, instance(event, identity(date), start, end, allDay), window, zone, true);
                    }
                    continue;
                }
                throw new IllegalArgumentException("Recurrence exception has no start date");
            }
            ICalDate date = startProperty.getValue();
            boolean allDay = !date.hasTime();
            ZonedDateTime start = allDay ? localDate(date).atStartOfDay(zone)
                    : zoned(date.toInstant(), timezone(info, startProperty, zone));
            ZonedDateTime end = allDay ? start.plusDays(allDayLength(event))
                    : end(event, start, timezone(info, startProperty, zone));
            CalendarEvent instance = instance(event,
                    identity(Objects.requireNonNull(event.getRecurrenceId()).getValue()), start, end, allDay);
            add(result, emitted, instance, window, zone, includeCancelled);
        }
    }

    private static void normalizeFloating(VEvent event, TimezoneInfo info, ZoneId zone) {
        for (var property : event.getProperties().values()) {
            // biweekly removes resolved TZIDs; a remaining identifier has no usable definition.
            if (property.getParameters().getTimezoneId() != null) {
                throw new IllegalArgumentException("Unresolved calendar time zone");
            }
            if (property instanceof DateOrDateTimeProperty date && info.isFloating(date) && date.getValue().hasTime()) {
                normalize(date.getValue(), zone);
            } else if (property instanceof RecurrenceDates dates) {
                if (!dates.getPeriods().isEmpty()) {
                    throw new IllegalArgumentException("RDATE periods are not supported");
                }
                if (info.isFloating(dates)) {
                    dates.getDates().forEach(value -> normalize(value, zone));
                }
            } else if (property instanceof ExceptionDates dates && info.isFloating(dates)) {
                dates.getValues().forEach(value -> normalize(value, zone));
            }
        }
        // DTSTART belongs to the recurrence set even if RDATE is also present.
        if (event.getRecurrenceRule() == null && !event.getRecurrenceDates().isEmpty()
                && event.getDateStart() != null) {
            RecurrenceDates dates = new RecurrenceDates();
            dates.getDates().add(Objects.requireNonNull(event.getDateStart()).getValue());
            event.addRecurrenceDates(dates);
        }
    }

    private static void normalize(ICalDate date, ZoneId zone) {
        var raw = date.getRawComponents();
        if (date.hasTime() && raw != null) {
            date.setTime(LocalDateTime
                    .of(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond())
                    .atZone(zone).toInstant().toEpochMilli());
        }
    }

    private static TimeZone timezone(TimezoneInfo info, DateOrDateTimeProperty property, ZoneId zone) {
        if (info.isFloating(property)) {
            return TimeZone.getTimeZone(zone);
        }
        var assignment = info.getTimezone(property);
        return assignment == null ? TimeZone.getTimeZone("UTC") : assignment.getTimeZone();
    }

    private static ZonedDateTime zoned(Instant instant, TimeZone timezone) {
        return instant.atZone(ZoneOffset.ofTotalSeconds(timezone.getOffset(instant.toEpochMilli()) / 1000));
    }

    private static LocalDate localDate(ICalDate date) {
        var raw = date.getRawComponents();
        return raw == null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.of(raw.getYear(), raw.getMonth(), raw.getDate());
    }

    private static String identity(ICalDate date) {
        return date.hasTime() ? date.toInstant().toString() : localDate(date).toString();
    }

    private static String uid(VEvent event) {
        var uid = event.getUid();
        if (uid == null || uid.getValue().isBlank()) {
            throw new IllegalArgumentException("Event has no UID");
        }
        return uid.getValue();
    }

    private static Duration length(VEvent event) {
        var start = event.getDateStart();
        if (start == null) {
            throw new IllegalArgumentException("Event has no start date");
        }
        var end = event.getDateEnd();
        var duration = event.getDuration();
        Duration length = end != null ? Duration.between(start.getValue().toInstant(), end.getValue().toInstant())
                : duration != null ? Duration.ofMillis(duration.getValue().toMillis())
                        : start.getValue().hasTime() ? Duration.ZERO : Duration.ofDays(1);
        if (length.isNegative()) {
            throw new IllegalArgumentException("Event ends before it starts");
        }
        return length;
    }

    private static ZonedDateTime end(VEvent event, ZonedDateTime start, TimeZone timezone) {
        var duration = event.getDuration();
        if (event.getDateEnd() != null || duration == null) {
            return zoned(start.toInstant().plus(length(event)), timezone);
        }
        var value = duration.getValue();
        long days = Math.addExact(Math.multiplyExact(Objects.requireNonNullElse(value.getWeeks(), 0).longValue(), 7),
                Objects.requireNonNullElse(value.getDays(), 0));
        // RFC 5545 distinguishes nominal days/weeks from exact hours across DST transitions.
        Calendar calendar = Calendar.getInstance(timezone, java.util.Locale.ROOT);
        calendar.setTimeInMillis(start.toInstant().toEpochMilli());
        calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(days));
        long seconds = Objects.requireNonNullElse(value.getHours(), 0).longValue() * 3600
                + Objects.requireNonNullElse(value.getMinutes(), 0).longValue() * 60
                + Objects.requireNonNullElse(value.getSeconds(), 0);
        return zoned(calendar.toInstant().plusSeconds(seconds), timezone);
    }

    private static long allDayLength(VEvent event) {
        var end = event.getDateEnd();
        var start = Objects.requireNonNull(event.getDateStart());
        long days = end == null ? Math.max(1, length(event).toDays())
                : ChronoUnit.DAYS.between(localDate(start.getValue()), localDate(end.getValue()));
        if (days < 1) {
            throw new IllegalArgumentException("Invalid all-day duration");
        }
        return days;
    }

    private static CalendarEvent instance(VEvent event, @org.eclipse.jdt.annotation.Nullable String recurrence,
            ZonedDateTime start, ZonedDateTime end, boolean allDay) {
        List<String> categories = event.getCategories().stream().flatMap(value -> value.getValues().stream()).toList();
        return new CalendarEvent(uid(event), recurrence,
                event.getSummary() == null ? "" : event.getSummary().getValue().replace("\r\n", "\n"),
                event.getDescription() == null ? "" : event.getDescription().getValue().replace("\r\n", "\n"),
                event.getLocation() == null ? "" : event.getLocation().getValue().replace("\r\n", "\n"),
                allDay ? null : start, allDay ? null : end, allDay ? start.toLocalDate() : null,
                allDay ? end.toLocalDate() : null, allDay,
                event.getStatus() == null ? "CONFIRMED" : event.getStatus().getValue(), categories,
                event.getOrganizer() == null ? "" : Objects.requireNonNullElse(event.getOrganizer().getEmail(), ""));
    }

    private static void add(List<CalendarEvent> result, Set<String> emitted, CalendarEvent event, CalendarWindow window,
            ZoneId zone, boolean includeCancelled) {
        if ((includeCancelled || !"CANCELLED".equals(event.status()))
                && CalendarEventSelection.overlaps(event, window, zone) && emitted.add(event.instanceId())) {
            if (result.size() >= MAX_INSTANCES) {
                throw new CalendarLimitException("Calendar expansion exceeds limit");
            }
            result.add(event);
        }
    }
}
