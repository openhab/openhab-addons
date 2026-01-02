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
package org.openhab.binding.astro.internal.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common used DateTime functions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class DateTimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);
    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");

    public static final double JD_J2000 = 2451545.0; // 2000-01-01 12:00
    public static final double JD_UNIX_EPOCH = 2440587.5; // 1970-01-01 00:00 UTC
    private static final double J1970 = JD_UNIX_EPOCH + 0.5; // 1970-01-01 12:00 UTC (julian solar noon)
    private static final int JULIAN_CENTURY_DAYS = 36525; // Length of a Julian Century in days
    private static final double SECONDS_PER_DAY = 60 * 60 * 24;

    /** Constructor */
    private DateTimeUtils() {
        throw new IllegalAccessError("Non-instantiable");
    }

    /**
     * Truncates the time from the calendar object.
     */
    public static Calendar truncateToSecond(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Truncates the time from the instant object.
     */
    public static Instant truncateToSecond(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Truncates the time from the calendar object.
     */
    public static Calendar truncateToMinute(Calendar calendar) {
        Calendar cal = truncateToSecond(calendar);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }

    /**
     * Truncates the time from the instant object.
     */
    public static Instant truncateToMinute(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Truncates the time from the calendar object.
     */
    public static Calendar truncateToMidnight(Calendar calendar) {
        Calendar cal = truncateToMinute(calendar);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return cal;
    }

    /**
     * Truncates the time from the instant object.
     */
    public static Instant truncateToMidnight(Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Creates a Range object within the specified months and days. The start
     * time is midnight, the end time is end of the day.
     */
    public static Range getRange(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay,
            TimeZone zone, Locale locale) {
        Calendar end = Calendar.getInstance(zone, locale);
        Calendar start = (Calendar) end.clone();
        start.set(Calendar.YEAR, startYear);
        start.set(Calendar.MONTH, startMonth);
        start.set(Calendar.DAY_OF_MONTH, startDay);
        start = truncateToMidnight(start);

        end.set(Calendar.YEAR, endYear);
        end.set(Calendar.MONTH, endMonth);
        end.set(Calendar.DAY_OF_MONTH, endDay);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        return new Range(start, end);
    }

    /**
     * Returns a calendar object from a julian date.
     */
    @Nullable
    public static Calendar toCalendar(double julianDate, TimeZone zone, Locale locale) {
        if (Double.compare(julianDate, Double.NaN) == 0 || julianDate == 0) {
            return null;
        }
        long millis = (long) ((julianDate + 0.5 - J1970) * AstroConstants.MILLISECONDS_PER_DAY);
        Calendar cal = Calendar.getInstance(zone, locale);
        cal.setTimeInMillis(millis);
        return cal;
    }

    /**
     * Returns the julian date from the calendar object.
     */
    public static double dateToJulianDate(Calendar calendar) {
        return calendar.getTimeInMillis() / AstroConstants.MILLISECONDS_PER_DAY - 0.5 + J1970;
    }

    /**
     * Returns the midnight julian date from the calendar object.
     */
    public static double midnightDateToJulianDate(Calendar calendar) {
        return dateToJulianDate(truncateToMidnight(calendar));
    }

    /**
     * Returns the end of day from the calendar object.
     */
    public static Calendar endOfDayDate(Calendar calendar) {
        Calendar cal = truncateToMidnight(calendar);
        cal.add(Calendar.DATE, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal;
    }

    /**
     * Returns the end of day from the instant object.
     */
    public static Instant endOfDayDate(Instant instant) {
        return truncateToMidnight(instant).plus(1, ChronoUnit.DAYS).minusMillis(1);
    }

    /**
     * Returns the year of the calendar object as a decimal value.
     */
    public static double getDecimalYear(Calendar calendar) {
        return calendar.get(Calendar.YEAR)
                + (double) calendar.get(Calendar.DAY_OF_YEAR) / calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    /**
     * Converts the time (hour.minute) to a calendar object.
     */
    @Nullable
    public static Calendar timeToCalendar(Calendar calendar, double time) {
        if (time < 0.0) {
            return null;
        }
        Calendar cal = (Calendar) calendar.clone();
        int hour = 0;
        int minute = 0;
        int days = (int) time / 24;
        double remains = time % 24;
        if (days != 0) {
            cal.add(Calendar.DAY_OF_MONTH, days);
        }
        hour = (int) remains;
        minute = (int) ((remains * 100) - (hour * 100));
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return truncateToMinute(cal);
    }

    /**
     * Returns true, if two calendar objects are on the same day ignoring time.
     */
    public static boolean isSameDay(@Nullable Calendar cal1, @Nullable Calendar cal2) {
        return cal1 != null && cal2 != null && cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the next Calendar from today.
     */
    public static Calendar getNextFromToday(TimeZone zone, Locale locale, Calendar... calendars) {
        Calendar now = Calendar.getInstance(zone, locale);
        Calendar result = getNext(now, calendars);
        return result == null ? now : result;
    }

    @Nullable
    static Calendar getNext(Calendar now, Calendar... calendars) {
        Calendar next = null;
        Calendar firstSeasonOfYear = null;
        for (Calendar calendar : calendars) {
            if (firstSeasonOfYear == null || calendar.before(firstSeasonOfYear)) {
                firstSeasonOfYear = calendar;
            }
            if (calendar.after(now) && (next == null || calendar.before(next))) {
                next = calendar;
            }
        }
        if (next == null && firstSeasonOfYear != null) {
            final Calendar nextYearSeason = (Calendar) firstSeasonOfYear.clone();

            nextYearSeason.add(Calendar.YEAR, 1);
            return nextYearSeason;
        } else {
            return next;
        }
    }

    /**
     * Returns true, if cal1 is greater or equal than cal2, ignoring seconds.
     */
    public static boolean isTimeGreaterEquals(Calendar cal1, Calendar cal2) {
        Calendar truncCal1 = truncateToMinute(cal1);
        Calendar truncCal2 = truncateToMinute(cal2);
        return truncCal1.getTimeInMillis() >= truncCal2.getTimeInMillis();
    }

    public static Calendar getAdjustedEarliest(Calendar cal, AstroChannelConfig config) {
        int minutes = getMinutesFromTime(config.earliest);
        // MainUI sets earliest to 00:00 if unconfigured, which is why zero must be treated as such
        return minutes > 0 ? adjustTime(cal, minutes) : cal;
    }

    public static Instant getAdjustedEarliest(Instant instant, AstroChannelConfig config) {
        int minutes = getMinutesFromTime(config.earliest);
        // MainUI sets earliest to 00:00 if unconfigured, which is why zero must be treated as such
        return minutes > 0 ? adjustTime(instant, minutes) : instant;
    }

    public static Calendar getAdjustedLatest(Calendar cal, AstroChannelConfig config) {
        int minutes = getMinutesFromTime(config.latest);
        // MainUI sets latest to 00:00 if unconfigured, which is why zero must be treated as such
        return minutes > 0 ? adjustTime(cal, minutes) : cal;
    }

    public static Instant getAdjustedLatest(Instant instant, AstroChannelConfig config) {
        int minutes = getMinutesFromTime(config.latest);
        // MainUI sets latest to 00:00 if unconfigured, which is why zero must be treated as such
        return minutes > 0 ? adjustTime(instant, minutes) : instant;
    }

    /**
     * Applies the config to the given calendar.
     */
    public static Calendar applyConfig(Calendar cal, AstroChannelConfig config) {
        Calendar cCal = cal;
        if (config.offset != 0) {
            cCal = (Calendar) cal.clone();
            cCal.add(Calendar.MINUTE, config.offset);
        }

        int minutes = getMinutesFromTime(config.earliest);
        Calendar threshold, actual;
        // MainUI sets earliest to 00:00 if unconfigured, which is why zero must be treated as such
        if (minutes > 0) {
            if ((threshold = truncateToMidnight(cal)).equals(actual = truncateToMidnight(cCal))) {
                // Same day
                Calendar cEarliest = getAdjustedEarliest(cCal, config);
                if (cCal.before(cEarliest)) {
                    return cEarliest;
                }
            } else {
                // Previous or next day
                if (actual.before(threshold)) {
                    return getAdjustedEarliest(threshold, config);
                }
            }
        }
        minutes = getMinutesFromTime(config.latest);
        // MainUI sets latest to 00:00 if unconfigured, which is why zero must be treated as such
        if (minutes > 0) {
            if ((threshold = endOfDayDate(cal)).equals(actual = endOfDayDate(cCal))) {
                // Same day
                Calendar cLatest = getAdjustedLatest(cCal, config);
                if (cCal.after(cLatest)) {
                    return cLatest;
                }
            } else {
                // Previous or next day
                if (actual.after(threshold)) {
                    return getAdjustedLatest(threshold, config);
                }
            }
        }

        return cCal;
    }

    /**
     * Applies the config to the given calendar.
     */
    public static Instant applyConfig(Instant cal, AstroChannelConfig config) {
        Instant cCal = cal;
        if (config.offset != 0) {
            cCal = cCal.plus(config.offset, ChronoUnit.MINUTES);
        }

        int minutes = getMinutesFromTime(config.earliest);
        Instant threshold, actual;
        // MainUI sets earliest to 00:00 if unconfigured, which is why zero must be treated as such
        if (minutes > 0) {
            if ((threshold = truncateToMidnight(cal)).equals(actual = truncateToMidnight(cCal))) {
                // Same day
                Instant cEarliest = getAdjustedEarliest(cCal, config);
                if (cCal.isBefore(cEarliest)) {
                    return cEarliest;
                }
            } else {
                // Previous or next day
                if (actual.isBefore(threshold)) {
                    return getAdjustedEarliest(threshold, config);
                }
            }
        }
        minutes = getMinutesFromTime(config.latest);
        // MainUI sets latest to 00:00 if unconfigured, which is why zero must be treated as such
        if (minutes > 0) {
            if ((threshold = endOfDayDate(cal)).equals(actual = endOfDayDate(cCal))) {
                // Same day
                Instant cLatest = getAdjustedLatest(cCal, config);
                if (cCal.isAfter(cLatest)) {
                    return cLatest;
                }
            } else {
                // Previous or next day
                if (actual.isAfter(threshold)) {
                    return getAdjustedLatest(threshold, config);
                }
            }
        }

        return cCal;
    }

    static Calendar adjustTime(Calendar cal, int minutes) {
        if (minutes >= 0) {
            Calendar cTime = truncateToMidnight(cal);
            cTime.add(Calendar.MINUTE, minutes);
            return cTime;
        }
        return cal;
    }

    static Instant adjustTime(Instant instant, int minutes) {
        if (minutes >= 0) {
            return truncateToMidnight(instant).plus(minutes, ChronoUnit.MINUTES);
        }
        return instant;
    }

    public static double toJulianCenturies(double jd) {
        return (jd - JD_J2000) / JULIAN_CENTURY_DAYS;
    }

    public static Calendar createCalendarForToday(int hour, int minute, TimeZone zone, Locale locale) {
        return DateTimeUtils.adjustTime(Calendar.getInstance(zone, locale), hour * 60 + minute);
    }

    /**
     * Parses a HH:MM string and returns hours and minutes in minutes.
     *
     * @return The number of minutes from midnight, or {@code -1} if the string couldn't be parsed.
     */
    static int getMinutesFromTime(@Nullable String configTime) {
        if (configTime != null) {
            String time = configTime.trim();
            if (!time.isBlank()) {
                try {
                    if (!HHMM_PATTERN.matcher(time).matches()) {
                        throw new NumberFormatException();
                    } else {
                        String[] elements = time.split(":");
                        int hour = Integer.parseInt(elements[0]);
                        int minutes = Integer.parseInt(elements[1]);
                        return (hour * 60) + minutes;
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.warn(
                            "Can not parse astro channel configuration '{}' to hour and minutes, use pattern hh:mm, ignoring!",
                            time);
                }
            }
        }
        return -1;
    }

    public static Instant jdToInstant(double jd) {
        double secondsFromEpoch = (jd - JD_UNIX_EPOCH) * SECONDS_PER_DAY;

        long epochSeconds = (long) Math.floor(secondsFromEpoch);
        long nanos = Math.round((secondsFromEpoch - epochSeconds) * 1_000_000_000L);

        // correct if rounded is above 1 second
        if (nanos == 1_000_000_000L) {
            epochSeconds++;
            nanos = 0;
        }

        return Instant.ofEpochSecond(epochSeconds, nanos);
    }

    public static Instant atMidnightOfFirstMonthDay(Instant instant, TimeZone zone) {
        return instant.atZone(zone.toZoneId()).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
    }
}
