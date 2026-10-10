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

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

/**
 * Canonical ordering and clock-dependent selection are independent of the output limit.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Time-zone aware current and next selection
 */
@NonNullByDefault
public final class CalendarEventSelection {
    private CalendarEventSelection() {
    }

    public static Result select(List<CalendarEvent> events, int maximumEvents) {
        return select(events, maximumEvents, ZoneId.systemDefault());
    }

    public static Result select(List<CalendarEvent> events, int maximumEvents, ZoneId zone) {
        List<CalendarEvent> sorted = sort(events, zone);
        return new Result(sorted.stream().limit(Math.max(1, maximumEvents)).toList(), sorted.size() > maximumEvents);
    }

    public static List<CalendarEvent> sort(List<CalendarEvent> events, ZoneId zone) {
        return events.stream().sorted(Comparator.comparing((CalendarEvent e) -> start(e, zone))
                .thenComparing(CalendarEvent::allDay).thenComparing(e -> end(e, zone)).thenComparing(CalendarEvent::uid)
                .thenComparing(e -> Objects.toString(e.recurrenceId(), "")).thenComparing(CalendarEvent::instanceId))
                .toList();
    }

    public static Instant start(CalendarEvent e, ZoneId zone) {
        return e.allDay() ? Objects.requireNonNull(e.allDayStart()).atStartOfDay(zone).toInstant()
                : Objects.requireNonNull(e.start()).toInstant();
    }

    public static Instant end(CalendarEvent e, ZoneId zone) {
        return e.allDay() ? Objects.requireNonNull(e.allDayEnd()).atStartOfDay(zone).toInstant()
                : Objects.requireNonNull(e.end()).toInstant();
    }

    public static boolean overlaps(CalendarEvent e, CalendarWindow window, ZoneId zone) {
        Instant start = start(e, zone), end = end(e, zone);
        return start.equals(end)
                ? !start.isBefore(window.start().toInstant()) && start.isBefore(window.end().toInstant())
                : start.isBefore(window.end().toInstant()) && end.isAfter(window.start().toInstant());
    }

    public static @Nullable CalendarEvent current(List<CalendarEvent> sorted, Instant now, ZoneId zone) {
        return sorted.stream().filter(e -> !start(e, zone).isAfter(now) && end(e, zone).isAfter(now)).findFirst()
                .orElse(null);
    }

    public static @Nullable CalendarEvent next(List<CalendarEvent> sorted, Instant now, ZoneId zone) {
        return sorted.stream()
                .filter(e -> start(e, zone).isAfter(now) || start(e, zone).equals(now) && end(e, zone).equals(now))
                .findFirst().orElse(null);
    }

    public record Result(List<CalendarEvent> events, boolean truncated) {
    }
}
