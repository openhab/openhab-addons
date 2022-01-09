/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code DateUtil} class defines common date utility functions used across the whole binding.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public final class DateUtil {

    private static final String RANGE_FORMAT = "%s/%s";

    /**
     * Parses a local date contained in the given string, using the given pattern.
     *
     * @param str
     *            the string to be parsed (can be {@code null})
     * @param pattern
     *            the pattern to be used to parse the given string
     *
     * @return a {@link LocalDate} object containing the parsed date
     *
     * @throws {@link DateTimeParseException}
     *             if the string cannot be parsed to a local date
     */
    public static LocalDate parseDate(@Nullable String str, String pattern) {
        if (str == null) {
            throw new DateTimeParseException("date string is null", "<null>", 0);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(str, dtf);
    }

    /**
     * Parses a local date and time contained in the given string, using the given pattern.
     *
     * @param str
     *            the string to be parsed (can be {@code null})
     * @param pattern
     *            the pattern to be used to parse the given string
     *
     * @return a {@link LocalDateTime} object containing the parsed date and time
     *
     * @throws {@link DateTimeParseException}
     *             if the string cannot be parsed to a local date and time
     */
    public static LocalDateTime parseLocalTime(@Nullable String str, String pattern) {
        if (str == null) {
            throw new DateTimeParseException("time string is null", "<null>", 0);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(str, dtf);
    }

    /**
     * Parses a date and time with timezone contained in the given string, using the given pattern.
     *
     * @param str
     *            the string to be parsed (can be {@code null})
     * @param pattern
     *            the pattern to be used to parse the given string
     *
     * @return a {@link ZonedDateTime} object containing the parsed date and time with timezone
     *
     * @throws {@link DateTimeParseException}
     *             if the string cannot be parsed to a date and time with timezone
     */
    public static ZonedDateTime parseZonedTime(@Nullable String str, String pattern) {
        if (str == null) {
            throw new DateTimeParseException("zoned string is null", "<null>", 0);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return ZonedDateTime.parse(str, dtf);
    }

    /**
     * Returns a date at given days after today and at start of day in the given timezone.
     *
     * @param days
     *            the number of days to be added ({@code 0} means today)
     * @param zoneId
     *            the identifier of the timezone to be applied
     *
     * @return a {@link ZonedDateTime} object containing the date and time with timezone
     */
    public static ZonedDateTime getZonedStartOfDay(int days, ZoneId zoneId) {
        return LocalDate.now().plusDays(days).atStartOfDay(zoneId);
    }

    /**
     * Returns a string representing the given local date and time object, using the given format pattern.
     *
     * @param date
     *            the local date and time object to be formatted
     * @param pattern
     *            the format pattern to be applied
     *
     * @return a string representing the local date and time object
     *
     * @throws {@link DateTimeException}
     *             if an error occurs during printing
     */
    public static String format(LocalDateTime date, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return date.format(dtf);
    }

    /**
     * Returns a string representing the given date and time with timezone object, using the given format pattern.
     *
     * @param date
     *            the date and time with timezone object to be formatted
     * @param pattern
     *            the format pattern to be applied
     *
     * @return a string representing the date and time with timezone object
     *
     * @throws {@link DateTimeException}
     *             if an error occurs during printing
     */
    public static String format(ZonedDateTime date, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return date.format(dtf);
    }

    /**
     * Returns a string representing the range between two local date and time objects, using the given format pattern.
     * The range itself is returned as {@code <date1>/<date2>}.
     *
     * @param date1
     *            the first local date and time object in range
     * @param date2
     *            the second local date and time object in range
     * @param pattern
     *            the format pattern to be applied
     *
     * @return a string representing the range between the two local date and time objects
     *
     * @throws {@link DateTimeException}
     *             if an error occurs during printing
     */
    public static String formatRange(LocalDateTime date1, LocalDateTime date2, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return String.format(RANGE_FORMAT, date1.format(dtf), date2.format(dtf));
    }

    /**
     * Returns a string representing the range between two date and time with timezone objects, using the given format
     * pattern.
     * The range itself is returned as {@code <date1>/<date2>}.
     *
     * @param date1
     *            the first date and time with timezone object in range
     * @param date2
     *            the second date and time with timezone object in range
     * @param pattern
     *            the format pattern to be applied
     *
     * @return a string representing the range between the two date and time with timezone objects
     *
     * @throws {@link DateTimeException}
     *             if an error occurs during printing
     */
    public static String formatRange(ZonedDateTime date1, ZonedDateTime date2, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return String.format(RANGE_FORMAT, date1.format(dtf), date2.format(dtf));
    }
}
