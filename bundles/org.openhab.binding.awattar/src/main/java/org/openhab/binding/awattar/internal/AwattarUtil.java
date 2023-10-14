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
package org.openhab.binding.awattar.internal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Some utility methods
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public class AwattarUtil {

    public static long getMillisToNextMinute(int mod, TimeZoneProvider timeZoneProvider) {
        long now = Instant.now().toEpochMilli();
        ZonedDateTime dt = ZonedDateTime.now(timeZoneProvider.getTimeZone()).truncatedTo(ChronoUnit.MINUTES);
        int min = dt.getMinute();
        int offset = min % mod;
        offset = offset == 0 ? mod : offset;
        dt = dt.plusMinutes(offset);
        return dt.toInstant().toEpochMilli() - now;
    }

    public static ZonedDateTime getCalendarForHour(int hour, ZoneId zone) {
        return ZonedDateTime.now(zone).truncatedTo(ChronoUnit.DAYS).plus(hour, ChronoUnit.HOURS);
    }

    public static DateTimeType getDateTimeType(long time, TimeZoneProvider tz) {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), tz.getTimeZone()));
    }

    public static QuantityType<Time> getDuration(long millis) {
        long minutes = millis / 60000;
        return QuantityType.valueOf(minutes, Units.MINUTE);
    }

    public static String formatDate(long date, ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), zoneId).toString();
    }

    public static String getHourFrom(long timestamp, ZoneId zoneId) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);
        return String.format("%02d", zdt.getHour());
    }
}
