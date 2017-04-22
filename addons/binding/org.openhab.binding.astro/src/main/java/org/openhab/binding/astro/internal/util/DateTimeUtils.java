/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.util;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);
    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");

    public static final double J1970 = 2440588.0;
    public static final double MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

    /**
     * Truncates the time from the calendar object.
     */
    public static Calendar truncateToMidnight(Calendar calendar) {
        return DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH);
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
        return DateUtils.round(cal, Calendar.MINUTE);
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
     * Returns the end of day julian date from the calendar object.
     */
    public static double endOfDayDateToJulianDate(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal = DateUtils.ceiling(cal, Calendar.DATE);
        cal.add(Calendar.MILLISECOND, -1);
        return dateToJulianDate(cal);
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
        return DateUtils.truncate(cal, Calendar.MINUTE);
    }

    /**
     * Returns true, if two calendar objects are on the same day ignoring time.
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1 != null && cal2 != null && DateUtils.isSameDay(cal1, cal2);
    }

    /**
     * Returns a date object from a calendar.
     */
    public static Date getDate(Calendar calendar) {
        return calendar == null ? null : calendar.getTime();
    }

    /**
     * Returns the next Calendar from today.
     */
    public static Calendar getNext(Calendar... calendars) {
        Calendar now = Calendar.getInstance();
        Calendar next = null;
        for (Calendar calendar : calendars) {
            if (calendar.after(now) && (next == null || calendar.before(next))) {
                next = calendar;
            }
        }
        return next;
    }

    /**
     * Returns true, if cal1 is greater or equal than cal2, ignoring seconds.
     */
    public static boolean isTimeGreaterEquals(Calendar cal1, Calendar cal2) {
        Calendar truncCal1 = DateUtils.truncate(cal1, Calendar.MINUTE);
        Calendar truncCal2 = DateUtils.truncate(cal2, Calendar.MINUTE);
        return truncCal1.getTimeInMillis() >= truncCal2.getTimeInMillis();
    }

    /**
     * Applies the config to the given calendar.
     */
    public static Calendar applyConfig(Calendar cal, AstroChannelConfig config) {
        Calendar cCal = cal;
        if (config.getOffset() != null && config.getOffset() != 0) {
            Calendar cOffset = Calendar.getInstance();
            cOffset.setTime(cCal.getTime());
            cOffset.add(Calendar.MINUTE, config.getOffset());
            cCal = cOffset;
        }

        Calendar cEarliest = adjustTime(cCal, getMinutesFromTime(config.getEarliest()));
        if (cCal.before(cEarliest)) {
            return cEarliest;
        }
        Calendar cLatest = adjustTime(cCal, getMinutesFromTime(config.getLatest()));
        if (cCal.after(cLatest)) {
            return cLatest;
        }

        return cCal;
    }

    private static Calendar adjustTime(Calendar cal, int minutes) {
        if (minutes > 0) {
            Calendar cTime = Calendar.getInstance();
            cTime = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
            cTime.add(Calendar.MINUTE, minutes);
            return cTime;
        }
        return cal;
    }

    /**
     * Parses a HH:MM string and returns the minutes.
     */
    private static int getMinutesFromTime(String configTime) {
        String time = StringUtils.trimToNull(configTime);
        if (time != null) {
            try {
                if (!HHMM_PATTERN.matcher(time).matches()) {
                    throw new NumberFormatException();
                } else {
                    int hour = Integer.parseInt(StringUtils.substringBefore(time, ":"));
                    int minutes = Integer.parseInt(StringUtils.substringAfter(time, ":"));
                    return (hour * 60) + minutes;
                }
            } catch (Exception ex) {
                logger.warn(
                        "Can not parse astro channel configuration '{}' to hour and minutes, use pattern hh:mm, ignoring!",
                        time);
            }
        }
        return 0;
    }
}
