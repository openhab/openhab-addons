package org.openhab.binding.awattar.internal;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class aWATTarUtil {

    private static final Logger logger = LoggerFactory.getLogger(aWATTarUtil.class);

    public static long getMillisToNextMinute(int mod) {

        long now = new Date().getTime();
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        int min = cal.get(Calendar.MINUTE);
        int offset = min % mod;
        offset = offset == 0 ? mod : offset;
        cal.add(Calendar.MINUTE, offset);
        long result = cal.getTimeInMillis() - now;
        logger.trace("Now: {}, mod: {}, Target Time: {}, difference: {}", now, mod, cal.toZonedDateTime().toString(),
                result);

        return result;
    }

    public static GregorianCalendar getCalendarForHour(int hour) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar;
    }

    public static DateTimeType getDateTimeType(long time, TimeZoneProvider tz) {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), tz.getTimeZone()));
    }

    public static String getDuration(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        return String.format("%02d:%02d", hours, minutes);
    }

    public static String formatDate(GregorianCalendar date) {
        return date.toZonedDateTime().toString();
    }

    public static String formatDate(long date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        return formatDate(c);
    }

    public static String getHourFrom(long timestamp) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(timestamp);
        return String.format("%02d", c.get(Calendar.HOUR_OF_DAY));
    }
}
