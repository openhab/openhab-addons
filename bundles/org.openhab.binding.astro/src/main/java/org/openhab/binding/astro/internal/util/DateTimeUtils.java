/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Calendar;
import java.util.regex.Pattern;

import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common used DateTime functions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DateTimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);
    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");

    private static final double J1970 = 2440588.0;
    private static final double MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

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
     * Truncates the time from the calendar object.
     */
    private static Calendar truncateToMinute(Calendar calendar) {
        Calendar cal = truncateToSecond(calendar);
        cal.set(Calendar.SECOND, 0);
        return cal;
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
     * Creates a Range object within the specified months and days. The start
     * time is midnight, the end time is end of the day.
     */
    public static Range getRange(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, startYear);
        start.set(Calendar.MONTH, startMonth);
        start.set(Calendar.DAY_OF_MONTH, startDay);
        start = truncateToMidnight(start);

        Calendar end = Calendar.getInstance();
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
    public static Calendar toCalendar(double julianDate) {
        if (Double.compare(julianDate, Double.NaN) == 0 || julianDate == 0) {
            return null;
        }
        long millis = (long) ((julianDate + 0.5 - J1970) * MILLISECONDS_PER_DAY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int second = cal.get(Calendar.SECOND);
        if (second > 30) {
            cal.add(Calendar.MINUTE, 1);
        }
        return truncateToMinute(cal);
    }

    /**
     * Returns the julian date from the calendar object.
     */
    public static double dateToJulianDate(Calendar calendar) {
        return calendar.getTimeInMillis() / MILLISECONDS_PER_DAY - 0.5 + J1970;
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
     * Returns the end of day julian date from the calendar object.
     */
    public static double endOfDayDateToJulianDate(Calendar calendar) {
        return dateToJulianDate(endOfDayDate(calendar));
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
    public static Calendar timeToCalendar(Calendar calendar, double time) {
        if (time < 0.0) {
            return null;
        }
        Calendar cal = (Calendar) calendar.clone();
        int hour = 0;
        int minute = 0;
        if (time == 24.0) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            hour = (int) time;
            minute = (int) ((time * 100) - (hour * 100));
        }
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return truncateToMinute(cal);
    }

    /**
     * Returns true, if two calendar objects are on the same day ignoring time.
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1 != null && cal2 != null && cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the next Calendar from today.
     */
    public static Calendar getNextFromToday(Calendar... calendars) {
        return getNext(Calendar.getInstance(), calendars);
    }

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
        if (next == null) {
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
        return adjustTime(cal, getMinutesFromTime(config.earliest));
    }

    public static Calendar getAdjustedLatest(Calendar cal, AstroChannelConfig config) {
        return adjustTime(cal, getMinutesFromTime(config.latest));
    }

    /**
     * Applies the config to the given calendar.
     */
    public static Calendar applyConfig(Calendar cal, AstroChannelConfig config) {
        Calendar cCal = cal;
        if (config.offset != 0) {
            Calendar cOffset = Calendar.getInstance();
            cOffset.setTime(cCal.getTime());
            cOffset.add(Calendar.MINUTE, config.offset);
            cCal = cOffset;
        }

        Calendar cEarliest = getAdjustedEarliest(cCal, config);
        if (cCal.before(cEarliest)) {
            return cEarliest;
        }
        Calendar cLatest = getAdjustedLatest(cCal, config);
        if (cCal.after(cLatest)) {
            return cLatest;
        }

        return cCal;
    }

    private static Calendar adjustTime(Calendar cal, int minutes) {
        if (minutes > 0) {
            Calendar cTime = truncateToMidnight(cal);
            cTime.add(Calendar.MINUTE, minutes);
            return cTime;
        }
        return cal;
    }

    public static Calendar createCalendarForToday(int hour, int minute) {
        return DateTimeUtils.adjustTime(Calendar.getInstance(), hour * 60 + minute);
    }

    /**
     * Parses a HH:MM string and returns the minutes.
     */
    private static int getMinutesFromTime(String configTime) {
        if (configTime != null) {
            String time = configTime.trim();
            if (!time.isEmpty()) {
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
        return 0;
    }
}
