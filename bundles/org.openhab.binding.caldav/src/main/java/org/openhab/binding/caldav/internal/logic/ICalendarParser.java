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
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.Observance;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.scribe.property.TimezoneOffsetFromScribe;
import biweekly.io.scribe.property.TimezoneOffsetToScribe;
import biweekly.io.text.ICalReader;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.ICalProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.TimezoneOffsetFrom;
import biweekly.property.TimezoneOffsetTo;
import biweekly.util.Google2445Utils;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * Bounded RFC 5545 expansion. Legacy date types remain at the library boundary.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Recurrence, exceptions and time-zone processing
 * @author Andreas Vilippus - Wall-time recurrence and custom time-zone corrections
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
            // biweekly accepts TZOFFSET seconds but its default scribe discards that component.
            reader.registerScribe(new TimezoneOffsetFromScribe() {
                @Override
                protected TimezoneOffsetFrom _parseText(@Nullable String value, @Nullable ICalDataType dataType,
                        @Nullable ICalParameters parameters, @Nullable ParseContext context) {
                    return new TimezoneOffsetFrom(parseUtcOffset(Objects.requireNonNull(value)));
                }
            });
            reader.registerScribe(new TimezoneOffsetToScribe() {
                @Override
                protected TimezoneOffsetTo _parseText(@Nullable String value, @Nullable ICalDataType dataType,
                        @Nullable ICalParameters parameters, @Nullable ParseContext context) {
                    return new TimezoneOffsetTo(parseUtcOffset(Objects.requireNonNull(value)));
                }
            });
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
        } catch (IOException | DateTimeException e) {
            throw new IllegalArgumentException("Invalid iCalendar resource", e);
        }
        return List.copyOf(result);
    }

    private static UtcOffset parseUtcOffset(String value) {
        if (value.matches("[+-](\\d{6}|\\d{2}:\\d{2}:\\d{2})")) {
            return new UtcOffset(ZoneOffset.of(value).getTotalSeconds() * 1000L);
        }
        return UtcOffset.parse(value);
    }

    private static void expand(ICalendar calendar, CalendarWindow window, ZoneId zone, boolean includeCancelled,
            List<CalendarEvent> result) {
        TimezoneInfo info = calendar.getTimezoneInfo();
        for (TimezoneAssignment assignment : new ArrayList<>(info.getTimezones())) {
            VTimezone component = assignment.getComponent();
            if (component != null) {
                TimezoneAssignment replacement = new TimezoneAssignment(new CalendarTimeZone(component), component);
                for (VEvent event : calendar.getEvents()) {
                    for (ICalProperty property : event.getProperties().values()) {
                        if (assignment.equals(info.getTimezone(property))) {
                            info.setTimezone(property, replacement);
                        }
                    }
                }
            }
        }
        Map<String, VEvent> overrides = new HashMap<>();
        for (VEvent event : calendar.getEvents()) {
            normalizeDates(event, info, zone);
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
            Instant lower = window.start().toInstant().minus(length).minus(2, ChronoUnit.DAYS);
            for (Instant occurrence : occurrences(event, first, tz, lower, window, allDay)) {
                LocalDate date = occurrence.atZone(ZoneOffset.UTC).toLocalDate();
                ZonedDateTime start = allDay ? date.atStartOfDay(zone) : zoned(occurrence, tz);
                if (!start.isBefore(window.end())) {
                    break;
                }
                String recurrence = allDay ? date.toString() : occurrence.toString();
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

    private static Set<Instant> occurrences(VEvent event, ICalDate first, TimeZone timezone, Instant lower,
            CalendarWindow window, boolean allDay) {
        Set<Instant> included = new TreeSet<>();
        // DTSTART belongs to the recurrence set even when the library omits the seed for BYSETPOS.
        included.add(occurrence(first, allDay));
        for (RecurrenceRule property : event.getProperties(RecurrenceRule.class)) {
            addRule(included, property.getValue(), first, timezone, lower, window, allDay);
        }
        for (RecurrenceDates property : event.getRecurrenceDates()) {
            property.getDates().forEach(date -> included.add(occurrence(date, allDay)));
        }
        Set<Instant> excluded = new HashSet<>();
        for (ExceptionRule property : event.getProperties(ExceptionRule.class)) {
            excluded.add(occurrence(first, allDay));
            addRule(excluded, property.getValue(), first, timezone, lower, window, allDay);
        }
        for (ExceptionDates property : event.getExceptionDates()) {
            property.getValues().forEach(date -> excluded.add(occurrence(date, allDay)));
        }
        included.removeAll(excluded);
        if (included.size() > MAX_INSTANCES) {
            throw new CalendarLimitException("Calendar expansion exceeds limit");
        }
        return included;
    }

    private static Instant occurrence(ICalDate date, boolean allDay) {
        return allDay ? localDate(date).atStartOfDay(ZoneOffset.UTC).toInstant() : date.toInstant();
    }

    private static void addRule(Set<Instant> result, Recurrence rule, ICalDate first, TimeZone timezone, Instant lower,
            CalendarWindow window, boolean allDay) {
        LocalDateTime seed = allDay ? localDate(first).atStartOfDay() : localTime(first);
        Integer count = rule.getCount();
        boolean skipCount = count != null && rule.getBySetPos().isEmpty() && fixedOffset(timezone);
        // Iterate wall times: the library otherwise normalizes DST gaps and consumes COUNT for invalid occurrences.
        var iterator = new Recurrence.Builder(rule).count(skipCount ? count : null).until((ICalDate) null).build()
                .getDateIterator(new ICalDate(Date.from(seed.toInstant(ZoneOffset.UTC))), TimeZone.getTimeZone("UTC"));
        ICalDate untilDate = rule.getUntil();
        Instant until = untilDate == null ? null
                : allDay ? occurrence(untilDate, true)
                        : untilDate.getRawComponents() != null && !untilDate.getRawComponents().isUtc()
                                ? resolveLocal(localTime(untilDate), timezone, true)
                                : untilDate.toInstant();
        if (count == null || skipCount) {
            if (skipCount && !allDay) {
                lower = lower.plus(2, ChronoUnit.DAYS);
            }
            Instant wallLower = lower.plusMillis(offset(timezone, lower));
            iterator.advanceTo(Date.from(wallLower));
        }
        int accepted = 1;
        int iterations = 0;
        Instant end = allDay ? window.end().toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                : window.end().toInstant();
        while ((count == null || skipCount || accepted < count) && iterator.hasNext()) {
            if (++iterations > MAX_INSTANCES || Thread.currentThread().isInterrupted()) {
                throw new CalendarLimitException("Calendar expansion exceeds limit");
            }
            LocalDateTime local = LocalDateTime.ofInstant(iterator.next().toInstant(), ZoneOffset.UTC);
            if (!local.isAfter(seed)) {
                continue;
            }
            Instant value = allDay ? local.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                    : resolveLocal(local, timezone, false);
            if (value == null) {
                continue;
            }
            if (!value.isBefore(end) || until != null && value.isAfter(until)) {
                break;
            }
            accepted++;
            result.add(value);
            if (result.size() > MAX_INSTANCES) {
                throw new CalendarLimitException("Calendar expansion exceeds limit");
            }
        }
    }

    private static boolean fixedOffset(TimeZone timezone) {
        if (timezone instanceof CalendarTimeZone custom) {
            return custom.offsets.size() == 1;
        }
        try {
            return timezone.toZoneId().getRules().isFixedOffset();
        } catch (DateTimeException e) {
            return false;
        }
    }

    private static LocalDateTime localTime(ICalDate date) {
        var raw = date.getRawComponents();
        if (raw == null) {
            throw new IllegalArgumentException("Calendar time has no local components");
        }
        // RFC 5545 maps leap seconds to second 59 when the date-time implementation cannot represent them.
        int second = raw.getSecond() == 60 ? 59 : raw.getSecond();
        return LocalDateTime.of(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), second);
    }

    private static @Nullable Instant resolveLocal(LocalDateTime local, TimeZone timezone, boolean explicit) {
        if (!(timezone instanceof CalendarTimeZone)) {
            try {
                ZoneId zone = timezone.toZoneId();
                if (!explicit && zone.getRules().getValidOffsets(local).isEmpty()) {
                    return null;
                }
                return local.atZone(zone).toInstant();
            } catch (DateTimeException e) {
                // Non-IANA TimeZone implementations retain the offset-based resolution path.
            }
        }
        Instant wall = local.toInstant(ZoneOffset.UTC);
        int before = timezone.getOffset(wall.minus(2, ChronoUnit.DAYS).toEpochMilli());
        int after = timezone.getOffset(wall.plus(2, ChronoUnit.DAYS).toEpochMilli());
        Set<Integer> offsets = new TreeSet<>(java.util.Comparator.reverseOrder());
        offsets.add(before);
        offsets.add(after);
        if (timezone instanceof CalendarTimeZone custom) {
            offsets.addAll(custom.offsets);
        }
        for (int offset : offsets) {
            Instant candidate = wall.minusMillis(offset);
            if (timezone.getOffset(candidate.toEpochMilli()) == offset) {
                return candidate;
            }
        }
        if (explicit && timezone instanceof CalendarTimeZone custom) {
            Integer gap = custom.offsetBeforeGap(wall);
            if (gap != null) {
                return wall.minusMillis(gap);
            }
        }
        return explicit ? wall.minusMillis(before) : null;
    }

    // VTIMEZONE DTSTART is local before the transition; lookup uses UTC after applying TZOFFSETFROM.
    // biweekly's ICalTimeZone compares UTC fields against local boundaries and also uses a 12-hour clock.
    private static final class CalendarTimeZone extends TimeZone {
        private static final long serialVersionUID = 1L;
        private final List<TransitionSeries> series = new ArrayList<>();
        private final TreeMap<Instant, Integer> transitions = new TreeMap<>();
        private final Set<Integer> offsets = new HashSet<>();
        private int initial;
        private int iterations;

        private static final class TransitionSeries {
            final DateIterator iterator;
            final int from;
            final int to;
            @Nullable
            Instant next;

            TransitionSeries(DateIterator iterator, int from, int to) {
                this.iterator = iterator;
                this.from = from;
                this.to = to;
                advance();
            }

            void advance() {
                next = iterator.hasNext() ? iterator.next().toInstant().minusMillis(from) : null;
            }
        }

        CalendarTimeZone(VTimezone component) {
            List<Observance> observances = new ArrayList<>(component.getStandardTimes());
            observances.addAll(component.getDaylightSavingsTime());
            Instant earliest = null;
            for (Observance original : observances) {
                if (original.getDateStart() == null || original.getTimezoneOffsetFrom() == null
                        || original.getTimezoneOffsetTo() == null || !original.getDateStart().getValue().hasTime()) {
                    throw new IllegalArgumentException("Invalid time zone observance");
                }
                Observance observance = original.copy();
                var start = Objects.requireNonNull(observance.getDateStart()).getValue();
                int from = Math
                        .toIntExact(Objects.requireNonNull(observance.getTimezoneOffsetFrom()).getValue().getMillis());
                int to = Math
                        .toIntExact(Objects.requireNonNull(observance.getTimezoneOffsetTo()).getValue().getMillis());
                Instant first = localTime(start).toInstant(ZoneOffset.UTC).minusMillis(from);
                if (earliest == null || first.isBefore(earliest)) {
                    earliest = first;
                    initial = from;
                }
                offsets.add(from);
                offsets.add(to);
                start.setTime(localTime(start).toInstant(ZoneOffset.UTC).toEpochMilli());
                for (ICalProperty property : observance.getProperties().values()) {
                    if (property instanceof RecurrenceDates dates) {
                        dates.getDates().forEach(
                                date -> date.setTime(localTime(date).toInstant(ZoneOffset.UTC).toEpochMilli()));
                    } else if (property instanceof ExceptionDates dates) {
                        dates.getValues().forEach(
                                date -> date.setTime(localTime(date).toInstant(ZoneOffset.UTC).toEpochMilli()));
                    } else if (property instanceof RecurrenceRule rule) {
                        ICalDate until = rule.getValue().getUntil();
                        if (until != null && until.hasTime()) {
                            normalize(until, TimeZone.getTimeZone("UTC"));
                            rule.setValue(new Recurrence.Builder(rule.getValue())
                                    .until(Date.from(until.toInstant().plusMillis(from))).build());
                        }
                    }
                }
                RecurrenceDates seed = new RecurrenceDates();
                seed.getDates().add(start);
                observance.addRecurrenceDates(seed);
                series.add(new TransitionSeries(
                        Google2445Utils.getDateIterator(observance, TimeZone.getTimeZone("UTC")), from, to));
            }
            if (earliest == null) {
                throw new IllegalArgumentException("Time zone has no observances");
            }
            setID(Objects.requireNonNull(component.getTimezoneId()).getValue());
        }

        @Override
        public int getOffset(long millis) {
            Instant instant = Instant.ofEpochMilli(millis);
            for (TransitionSeries transition : series) {
                Instant next;
                while ((next = transition.next) != null && !next.isAfter(instant)) {
                    if (++iterations > MAX_INSTANCES || Thread.currentThread().isInterrupted()) {
                        throw new CalendarLimitException("Time zone expansion exceeds limit");
                    }
                    transitions.put(next, transition.to);
                    transition.advance();
                }
            }
            var current = transitions.floorEntry(instant);
            return current == null ? initial : current.getValue();
        }

        private @Nullable Integer offsetBeforeGap(Instant wall) {
            int previous = initial;
            for (var transition : transitions.entrySet()) {
                int after = transition.getValue();
                if (after > previous && !wall.isBefore(transition.getKey().plusMillis(previous))
                        && wall.isBefore(transition.getKey().plusMillis(after))) {
                    return previous;
                }
                previous = after;
            }
            return null;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
            Instant standard = LocalDate.of(year, month + 1, day).atStartOfDay().toInstant(ZoneOffset.UTC)
                    .plusMillis(millis).minusMillis(initial);
            return getOffset(standard.toEpochMilli());
        }

        @Override
        public void setRawOffset(int offsetMillis) {
            throw new UnsupportedOperationException("Immutable calendar time zone");
        }

        @Override
        public int getRawOffset() {
            return initial;
        }

        @Override
        public boolean useDaylightTime() {
            return offsets.size() > 1;
        }

        @Override
        public boolean inDaylightTime(@Nullable Date date) {
            return getOffset(Objects.requireNonNull(date).getTime()) != initial;
        }
    }

    private static void normalizeDates(VEvent event, TimezoneInfo info, ZoneId zone) {
        for (var property : event.getProperties().values()) {
            // biweekly removes resolved TZIDs; a remaining identifier has no usable definition.
            if (property.getParameters().getTimezoneId() != null) {
                throw new IllegalArgumentException("Unresolved calendar time zone");
            }
            TimeZone timezone = timezone(info, property, zone);
            if (property instanceof DateOrDateTimeProperty date && date.getValue().hasTime()) {
                normalize(date.getValue(), timezone);
            } else if (property instanceof RecurrenceDates dates) {
                if (!dates.getPeriods().isEmpty()) {
                    throw new IllegalArgumentException("RDATE periods are not supported");
                }
                dates.getDates().forEach(value -> normalize(value, timezone));
            } else if (property instanceof ExceptionDates dates) {
                dates.getValues().forEach(value -> normalize(value, timezone));
            }
            var start = event.getDateStart();
            TimeZone recurrenceZone = start == null ? timezone : timezone(info, start, zone);
            if (property instanceof RecurrenceRule rule) {
                rule.setValue(normalizeUntil(rule.getValue(), recurrenceZone));
            } else if (property instanceof ExceptionRule rule) {
                rule.setValue(normalizeUntil(rule.getValue(), recurrenceZone));
            }
        }
    }

    private static Recurrence normalizeUntil(Recurrence rule, TimeZone timezone) {
        ICalDate until = rule.getUntil();
        if (until == null) {
            return rule;
        }
        normalize(until, timezone);
        return new Recurrence.Builder(rule).until(until).build();
    }

    private static void normalize(ICalDate date, TimeZone timezone) {
        var raw = date.getRawComponents();
        if (date.hasTime() && raw != null) {
            TimeZone effective = raw.isUtc() ? TimeZone.getTimeZone("UTC") : timezone;
            date.setTime(Objects.requireNonNull(resolveLocal(localTime(date), effective, true)).toEpochMilli());
        }
    }

    private static TimeZone timezone(TimezoneInfo info, ICalProperty property, ZoneId zone) {
        if (info.isFloating(property)) {
            return TimeZone.getTimeZone(zone);
        }
        var assignment = info.getTimezone(property);
        return assignment == null ? TimeZone.getTimeZone("UTC") : assignment.getTimeZone();
    }

    private static ZonedDateTime zoned(Instant instant, TimeZone timezone) {
        return instant.atZone(ZoneOffset.ofTotalSeconds(offset(timezone, instant) / 1000));
    }

    private static int offset(TimeZone timezone, Instant instant) {
        if (!(timezone instanceof CalendarTimeZone)) {
            try {
                return timezone.toZoneId().getRules().getOffset(instant).getTotalSeconds() * 1000;
            } catch (DateTimeException e) {
                // A custom non-IANA TimeZone can expose only its legacy offset API.
            }
        }
        return timezone.getOffset(instant.toEpochMilli());
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
        Instant shifted = Objects.requireNonNull(resolveLocal(start.toLocalDateTime().plusDays(days), timezone, true));
        long seconds = Objects.requireNonNullElse(value.getHours(), 0).longValue() * 3600
                + Objects.requireNonNullElse(value.getMinutes(), 0).longValue() * 60
                + Objects.requireNonNullElse(value.getSeconds(), 0);
        return zoned(shifted.plusSeconds(seconds), timezone);
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
