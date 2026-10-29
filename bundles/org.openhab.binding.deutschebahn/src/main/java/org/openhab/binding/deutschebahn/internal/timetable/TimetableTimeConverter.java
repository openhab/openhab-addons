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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Converts between instants and the local date/time representations used by the Timetables API.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public final class TimetableTimeConverter {

    private static final DateTimeFormatter PLAN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd", Locale.ROOT);
    private static final DateTimeFormatter PLAN_HOUR_FORMAT = DateTimeFormatter.ofPattern("HH", Locale.ROOT);
    private static final DateTimeFormatter EVENT_TIME_FORMAT = DateTimeFormatter.ofPattern("uuMMddHHmm", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private final ZoneId timeZone;

    /**
     * Creates a converter for the given timetable time zone.
     */
    public TimetableTimeConverter(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Returns the time zone used by this converter.
     */
    public ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Formats the local date used by a plan request.
     */
    public String formatPlanDate(Date time) {
        return PLAN_DATE_FORMAT.format(time.toInstant().atZone(timeZone));
    }

    /**
     * Formats the local hour used by a plan request.
     */
    public String formatPlanHour(Date time) {
        return PLAN_HOUR_FORMAT.format(time.toInstant().atZone(timeZone));
    }

    /**
     * Formats an instant as a local timetable event time.
     */
    public String formatEventTime(Date time) {
        return EVENT_TIME_FORMAT.format(time.toInstant().atZone(timeZone));
    }

    /**
     * Parses a local timetable event time into an instant.
     */
    public @Nullable Date parseEventTime(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            final Instant instant = LocalDateTime.parse(value, EVENT_TIME_FORMAT).atZone(timeZone).toInstant();
            return Date.from(instant);
        } catch (DateTimeException e) {
            return null;
        }
    }
}
