/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DateUtil} class defines common Date utility functions, which are used across the whole binding.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class DateUtil {

    public static @Nullable Date parse(@Nullable String strDate, String dateFormat) {
        if (strDate == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String format(Date date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    public static Date dateAtStartOfDay(@Nullable Date date, int addDays) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        // reset hour, minutes, seconds and millis
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (addDays != 0) {
            // add days
            cal.add(Calendar.DAY_OF_MONTH, addDays);
        }

        return cal.getTime();
    }

    public static Date dateAtStartOfDay(@Nullable Date date) {
        return dateAtStartOfDay(date, 0);
    }

    public static Date dateAtStartOfDay(int addDays) {
        return dateAtStartOfDay(null, addDays);
    }

    public static Date todayAtStartOfDay() {
        return dateAtStartOfDay(0);
    }

    public static Date tomorrowAtStartOfDay() {
        return dateAtStartOfDay(1);
    }

    public static Date setTime(@Nullable Date date, int hours, int minutes, int seconds, int millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, millis);

        return cal.getTime();
    }

    public static Date todayResetTime(boolean resetHours, boolean resetMinutes, boolean resetSeconds,
            boolean resetMillis) {
        Calendar cal = Calendar.getInstance();
        if (resetHours) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (resetMinutes) {
            cal.set(Calendar.MINUTE, 0);
        }
        if (resetSeconds) {
            cal.set(Calendar.SECOND, 0);
        }
        if (resetMillis) {
            cal.set(Calendar.MILLISECOND, 0);
        }

        return cal.getTime();
    }

    public static Date plusDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // add days
        cal.add(Calendar.DAY_OF_MONTH, days);

        return cal.getTime();
    }

    public static Date plusHours(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // add hours
        cal.add(Calendar.HOUR_OF_DAY, hours);

        return cal.getTime();
    }

    public static Date plusMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // add minutes
        cal.add(Calendar.MINUTE, minutes);

        return cal.getTime();
    }

}
